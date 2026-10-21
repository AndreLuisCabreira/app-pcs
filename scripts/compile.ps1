$ErrorActionPreference = 'Stop'

$projectDirectory = Split-Path -Parent $PSScriptRoot
$outputDirectory = Join-Path $projectDirectory 'target\classes'
$userProfileDirectory = [Environment]::GetFolderPath('UserProfile')

. (Join-Path $PSScriptRoot 'java-tools.ps1')
$jdkHome = Get-IntraTechJdk
$javac = Join-Path $jdkHome 'bin\javac.exe'
$maven = Get-Command mvn -ErrorAction SilentlyContinue
if ($maven) {
    $env:JAVA_HOME = $jdkHome
    & $maven.Source -B -f (Join-Path $projectDirectory 'pom.xml') compile
    if ($LASTEXITCODE -ne 0) { throw 'A compilação Maven falhou.' }
    return
}

$mavenRepository = Join-Path $userProfileDirectory '.m2\repository\com\mysql\mysql-connector-j'
$connector = Get-ChildItem -LiteralPath $mavenRepository -Recurse -Filter 'mysql-connector-j-*.jar' -ErrorAction SilentlyContinue |
    Sort-Object FullName -Descending |
    Select-Object -First 1 -ExpandProperty FullName

if (-not $connector) {
    throw 'MySQL Connector/J não foi encontrado no repositório Maven local.'
}

$postgresRepository = Join-Path $userProfileDirectory '.m2\repository\org\postgresql\postgresql'
$postgresConnector = Get-ChildItem -LiteralPath $postgresRepository -Recurse -Filter 'postgresql-42.7.13.jar' -ErrorAction SilentlyContinue |
    Select-Object -First 1 -ExpandProperty FullName

if (-not $postgresConnector) {
    throw 'PostgreSQL JDBC 42.7.13 não foi encontrado. Execute scripts\install-dependencies.ps1.'
}

$javaFxRepository = Join-Path $userProfileDirectory '.m2\repository\org\openjfx'
$javaFxJars = @(Get-ChildItem -LiteralPath $javaFxRepository -Recurse -Filter '*.jar' -ErrorAction SilentlyContinue |
    Where-Object { $_.FullName -match '[\\/]21\.0\.8[\\/]' } |
    Select-Object -ExpandProperty FullName)

if ($javaFxJars.Count -lt 3) {
    throw 'JavaFX 21.0.8 não foi encontrado. Recarregue o pom.xml como projeto Maven.'
}

$jacksonRepository = Join-Path $userProfileDirectory '.m2\repository\com\fasterxml\jackson\core'
$jacksonJars = @(Get-ChildItem -LiteralPath $jacksonRepository -Recurse -Filter '*.jar' -ErrorAction SilentlyContinue |
    Where-Object { $_.FullName -match '[\\/](2\.21\.6|2\.21)[\\/]' } |
    Select-Object -ExpandProperty FullName)

if ($jacksonJars.Count -lt 3) {
    throw 'Jackson 2.21 não foi encontrado. Execute scripts\install-dependencies.ps1.'
}

$poolJars = @(
    (Join-Path $userProfileDirectory '.m2\repository\com\zaxxer\HikariCP\7.0.2\HikariCP-7.0.2.jar'),
    (Join-Path $userProfileDirectory '.m2\repository\org\slf4j\slf4j-api\2.0.18\slf4j-api-2.0.18.jar'),
    (Join-Path $userProfileDirectory '.m2\repository\org\slf4j\slf4j-nop\2.0.18\slf4j-nop-2.0.18.jar')
)
if ($poolJars.Where({ -not (Test-Path -LiteralPath $_) }).Count -gt 0) {
    throw 'HikariCP/SLF4J não foram encontrados. Execute scripts\install-dependencies.ps1.'
}
$classPath = (@($connector, $postgresConnector) + $javaFxJars + $jacksonJars + $poolJars) -join [IO.Path]::PathSeparator

$resolvedProjectDirectory = [IO.Path]::GetFullPath($projectDirectory)
$resolvedOutputDirectory = [IO.Path]::GetFullPath($outputDirectory)
if (-not $resolvedOutputDirectory.StartsWith(
        $resolvedProjectDirectory + [IO.Path]::DirectorySeparatorChar,
        [StringComparison]::OrdinalIgnoreCase
    )) {
    throw 'O diretório de compilação está fora do projeto.'
}
if (Test-Path -LiteralPath $resolvedOutputDirectory) {
    Remove-Item -LiteralPath $resolvedOutputDirectory -Recurse -Force
}
New-Item -ItemType Directory -Force -Path $outputDirectory | Out-Null

$sourceFiles = Get-ChildItem -LiteralPath (Join-Path $projectDirectory 'src') -Recurse -Filter '*.java' |
    Select-Object -ExpandProperty FullName

& $javac -encoding UTF-8 -cp $classPath -d $outputDirectory $sourceFiles
if ($LASTEXITCODE -ne 0) {
    throw "A compilação falhou com o código $LASTEXITCODE."
}

$resourcesDirectory = Join-Path $projectDirectory 'resources'
Copy-Item -Path (Join-Path $resourcesDirectory '*') -Destination $outputDirectory -Recurse -Force

Write-Host "Compilação concluída: $outputDirectory"
