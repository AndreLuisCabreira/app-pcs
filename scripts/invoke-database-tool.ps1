param(
    [Parameter(Mandatory)]
    [ValidateSet('DatabaseConnectionCheck', 'DatabaseMigrationLauncher')]
    [string]$MainClass
)

$ErrorActionPreference = 'Stop'

$projectDirectory = Split-Path -Parent $PSScriptRoot
$userProfileDirectory = [Environment]::GetFolderPath('UserProfile')

& (Join-Path $PSScriptRoot 'compile.ps1')

$javaExtension = Get-ChildItem -LiteralPath (Join-Path $userProfileDirectory '.vscode\extensions') -Directory |
    Where-Object { $_.Name -like 'redhat.java-*-win32-x64' } |
    Sort-Object Name -Descending |
    Select-Object -First 1

if ($null -eq $javaExtension) {
    throw 'A extensão Java do VS Code não foi encontrada.'
}

$java = Get-ChildItem -LiteralPath $javaExtension.FullName -Recurse -Filter 'java.exe' |
    Select-Object -First 1 -ExpandProperty FullName
$postgresDriver = Get-ChildItem -LiteralPath (Join-Path $userProfileDirectory '.m2\repository\org\postgresql\postgresql') `
    -Recurse -Filter 'postgresql-42.7.13.jar' -ErrorAction SilentlyContinue |
    Select-Object -First 1 -ExpandProperty FullName
$mysqlDriver = Get-ChildItem -LiteralPath (Join-Path $userProfileDirectory '.m2\repository\com\mysql\mysql-connector-j') `
    -Recurse -Filter 'mysql-connector-j-*.jar' -ErrorAction SilentlyContinue |
    Select-Object -First 1 -ExpandProperty FullName
$poolJars = @(
    (Join-Path $userProfileDirectory '.m2\repository\com\zaxxer\HikariCP\7.0.2\HikariCP-7.0.2.jar'),
    (Join-Path $userProfileDirectory '.m2\repository\org\slf4j\slf4j-api\2.0.18\slf4j-api-2.0.18.jar'),
    (Join-Path $userProfileDirectory '.m2\repository\org\slf4j\slf4j-nop\2.0.18\slf4j-nop-2.0.18.jar')
)

$dependencies = @($postgresDriver, $mysqlDriver) + $poolJars
$missingDependencies = @($dependencies | Where-Object { -not $_ -or -not (Test-Path -LiteralPath $_) })
if ($missingDependencies.Count -gt 0) {
    throw 'Drivers ou bibliotecas de conexão não foram encontrados. Execute scripts\install-dependencies.ps1.'
}

$classPath = (@((Join-Path $projectDirectory 'target\classes')) + $dependencies) -join [IO.Path]::PathSeparator
$configurationFile = Join-Path $projectDirectory '.env'
$javaArguments = @(
    '-Dfile.encoding=UTF-8',
    "-Dintratech.config=$configurationFile",
    '-cp',
    $classPath,
    $MainClass
)

Push-Location $projectDirectory
try {
    & $java $javaArguments
    if ($LASTEXITCODE -ne 0) {
        throw "A ferramenta $MainClass falhou com o código $LASTEXITCODE."
    }
} finally {
    Pop-Location
}
