param(
    [switch]$SkipJdkDownload
)

$ErrorActionPreference = 'Stop'

$projectDirectory = Split-Path -Parent $PSScriptRoot
$targetDirectory = Join-Path $projectDirectory 'target'
$inputDirectory = Join-Path $targetDirectory 'package-input'
$outputDirectory = Join-Path $targetDirectory 'package-output'
$distDirectory = Join-Path $projectDirectory 'dist'
$appName = 'IntraTech PC Builder'
$appVersion = '1.8.1'
$appImageDirectory = Join-Path $outputDirectory $appName
$archivePath = Join-Path $distDirectory "IntraTech-PC-Builder-$appVersion-windows-x64.zip"
$userProfileDirectory = [Environment]::GetFolderPath('UserProfile')

function Assert-ProjectChildPath {
    param([Parameter(Mandatory)][string]$Path)

    $projectRoot = [IO.Path]::GetFullPath($projectDirectory).TrimEnd('\') + '\'
    $resolved = [IO.Path]::GetFullPath($Path)
    if (-not $resolved.StartsWith($projectRoot, [StringComparison]::OrdinalIgnoreCase)) {
        throw "Caminho fora do projeto: $resolved"
    }
}

function Find-PackagingJdk {
    $candidates = @()
    if ($env:JAVA_HOME) {
        $candidates += $env:JAVA_HOME
    }
    $candidates += Get-ChildItem -LiteralPath (Join-Path $projectDirectory '.tools') -Directory -Recurse -ErrorAction SilentlyContinue |
        Select-Object -ExpandProperty FullName
    $candidates += Get-ChildItem -LiteralPath (Join-Path $userProfileDirectory '.vscode\extensions') -Directory -Filter 'redhat.java-*' -ErrorAction SilentlyContinue |
        Select-Object -ExpandProperty FullName

    foreach ($candidate in $candidates) {
        $jpackage = Get-ChildItem -LiteralPath $candidate -Recurse -Filter 'jpackage.exe' -ErrorAction SilentlyContinue |
            Select-Object -First 1 -ExpandProperty FullName
        if ($jpackage) {
            return Split-Path -Parent (Split-Path -Parent $jpackage)
        }
    }
    return $null
}

foreach ($path in @($inputDirectory, $outputDirectory, $distDirectory, $archivePath)) {
    Assert-ProjectChildPath -Path $path
}

$jdkHome = Find-PackagingJdk
if (-not $jdkHome) {
    if ($SkipJdkDownload) {
        throw 'JDK 21 com jpackage não encontrado. Execute scripts\install-packaging-jdk.ps1.'
    }
    & (Join-Path $PSScriptRoot 'install-packaging-jdk.ps1')
    $jdkHome = Find-PackagingJdk
}
if (-not $jdkHome) {
    throw 'Não foi possível localizar jpackage.exe depois da instalação do JDK.'
}

$jar = Join-Path $jdkHome 'bin\jar.exe'
$jpackage = Join-Path $jdkHome 'bin\jpackage.exe'
if (-not (Test-Path -LiteralPath $jar) -or -not (Test-Path -LiteralPath $jpackage)) {
    throw "O JDK selecionado não contém jar.exe e jpackage.exe: $jdkHome"
}

Write-Host '1/5 Compilando a aplicação...'
& (Join-Path $PSScriptRoot 'compile.ps1')

foreach ($directory in @($inputDirectory, $outputDirectory)) {
    if (Test-Path -LiteralPath $directory) {
        Remove-Item -LiteralPath $directory -Recurse -Force
    }
    New-Item -ItemType Directory -Force -Path $directory | Out-Null
}
New-Item -ItemType Directory -Force -Path $distDirectory | Out-Null
if (Test-Path -LiteralPath $archivePath) {
    Remove-Item -LiteralPath $archivePath -Force
}

Write-Host '2/5 Criando o JAR da aplicação...'
$applicationJar = Join-Path $inputDirectory 'intratech-pc-builder.jar'
& $jar --create --file $applicationJar --main-class DesktopLauncher -C (Join-Path $targetDirectory 'classes') .
if ($LASTEXITCODE -ne 0) {
    throw "A criação do JAR falhou com o código $LASTEXITCODE."
}

Write-Host '3/5 Copiando as bibliotecas necessárias...'
$dependencyPatterns = @(
    (Join-Path $userProfileDirectory '.m2\repository\com\mysql\mysql-connector-j\9.7.0\mysql-connector-j-9.7.0.jar'),
    (Join-Path $userProfileDirectory '.m2\repository\org\postgresql\postgresql\42.7.13\postgresql-42.7.13.jar'),
    (Join-Path $userProfileDirectory '.m2\repository\com\zaxxer\HikariCP\7.0.2\HikariCP-7.0.2.jar'),
    (Join-Path $userProfileDirectory '.m2\repository\org\slf4j\slf4j-api\2.0.18\slf4j-api-2.0.18.jar'),
    (Join-Path $userProfileDirectory '.m2\repository\org\slf4j\slf4j-nop\2.0.18\slf4j-nop-2.0.18.jar')
)
$dependencyPatterns += Get-ChildItem -LiteralPath (Join-Path $userProfileDirectory '.m2\repository\org\openjfx') -Recurse -Filter '*.jar' -ErrorAction SilentlyContinue |
    Where-Object { $_.FullName -match '[\\/]21\.0\.8[\\/]' -and $_.Name -notmatch '-(sources|javadoc)\.jar$' } |
    Select-Object -ExpandProperty FullName
$dependencyPatterns += Get-ChildItem -LiteralPath (Join-Path $userProfileDirectory '.m2\repository\com\fasterxml\jackson\core') -Recurse -Filter '*.jar' -ErrorAction SilentlyContinue |
    Where-Object { $_.FullName -match '[\\/](2\.21\.6|2\.21)[\\/]' -and $_.Name -notmatch '-(sources|javadoc)\.jar$' } |
    Select-Object -ExpandProperty FullName

$dependencies = @($dependencyPatterns | Where-Object { Test-Path -LiteralPath $_ } | Sort-Object -Unique)
if ($dependencies.Count -lt 12) {
    throw "Bibliotecas insuficientes para empacotar: foram encontradas $($dependencies.Count)."
}
foreach ($dependency in $dependencies) {
    Copy-Item -LiteralPath $dependency -Destination $inputDirectory -Force
}

Write-Host '4/5 Gerando a aplicação portátil com Java incluído...'
$runtimeModules = @(
    'java.base', 'java.desktop', 'java.logging', 'java.management', 'java.naming',
    'java.net.http', 'java.prefs', 'java.security.jgss', 'java.sql',
    'java.transaction.xa', 'java.xml', 'jdk.charsets', 'jdk.crypto.ec',
    'jdk.localedata', 'jdk.unsupported'
) -join ','

& $jpackage `
    --type app-image `
    --dest $outputDirectory `
    --input $inputDirectory `
    --name $appName `
    --app-version $appVersion `
    --vendor 'IntraTech' `
    --description 'Montagem e análise de computadores' `
    --main-jar (Split-Path -Leaf $applicationJar) `
    --main-class DesktopLauncher `
    --add-modules $runtimeModules `
    --java-options '-Dfile.encoding=UTF-8' `
    --java-options '-Dintratech.config=$APPDIR/../.env'
if ($LASTEXITCODE -ne 0) {
    throw "O jpackage falhou com o código $LASTEXITCODE."
}

Copy-Item -LiteralPath (Join-Path $projectDirectory '.env.example') -Destination (Join-Path $appImageDirectory '.env.example') -Force
Copy-Item -LiteralPath (Join-Path $projectDirectory 'DISTRIBUICAO.md') -Destination (Join-Path $appImageDirectory 'LEIA-ME.md') -Force

$accidentalEnv = Get-ChildItem -LiteralPath $appImageDirectory -Recurse -Force -File |
    Where-Object { $_.Name -eq '.env' }
if ($accidentalEnv) {
    throw 'A validação de segurança encontrou um arquivo .env dentro do pacote.'
}

Write-Host '5/5 Compactando o ZIP...'
Compress-Archive -LiteralPath $appImageDirectory -DestinationPath $archivePath -CompressionLevel Optimal

$archive = Get-Item -LiteralPath $archivePath
Write-Host ''
Write-Host 'Build portátil concluída.'
Write-Host "Pasta: $appImageDirectory"
Write-Host "ZIP:   $($archive.FullName)"
Write-Host ("Tamanho: {0:N1} MB" -f ($archive.Length / 1MB))
