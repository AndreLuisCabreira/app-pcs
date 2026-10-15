param(
    [switch]$Execute,
    [switch]$Replace
)

$ErrorActionPreference = 'Stop'

$projectDirectory = Split-Path -Parent $PSScriptRoot
$userProfileDirectory = [Environment]::GetFolderPath('UserProfile')

& (Join-Path $PSScriptRoot 'compile.ps1')

$javaExtension = Get-ChildItem -LiteralPath (Join-Path $userProfileDirectory '.vscode\extensions') -Directory |
    Where-Object { $_.Name -like 'redhat.java-*-win32-x64' } |
    Sort-Object Name -Descending |
    Select-Object -First 1
$java = Get-ChildItem -LiteralPath $javaExtension.FullName -Recurse -Filter 'java.exe' |
    Select-Object -First 1 -ExpandProperty FullName
$postgresDriver = Get-ChildItem -LiteralPath (Join-Path $userProfileDirectory '.m2\repository\org\postgresql\postgresql') `
    -Recurse -Filter 'postgresql-42.7.13.jar' |
    Select-Object -First 1 -ExpandProperty FullName
$mysqlDriver = Get-ChildItem -LiteralPath (Join-Path $userProfileDirectory '.m2\repository\com\mysql\mysql-connector-j') `
    -Recurse -Filter 'mysql-connector-j-*.jar' |
    Select-Object -First 1 -ExpandProperty FullName
$poolJars = @(
    (Join-Path $userProfileDirectory '.m2\repository\com\zaxxer\HikariCP\7.0.2\HikariCP-7.0.2.jar'),
    (Join-Path $userProfileDirectory '.m2\repository\org\slf4j\slf4j-api\2.0.18\slf4j-api-2.0.18.jar'),
    (Join-Path $userProfileDirectory '.m2\repository\org\slf4j\slf4j-nop\2.0.18\slf4j-nop-2.0.18.jar')
)
$classPath = (@(
    (Join-Path $projectDirectory 'target\classes'),
    $postgresDriver,
    $mysqlDriver
) + $poolJars) -join [IO.Path]::PathSeparator

$arguments = @('-cp', $classPath, 'DatabaseTransfer')
if ($Execute) { $arguments += '--execute' }
if ($Replace) { $arguments += '--replace' }

Push-Location $projectDirectory
try {
    & $java $arguments
    if ($LASTEXITCODE -ne 0) {
        throw "A migração falhou com o código $LASTEXITCODE."
    }
} finally {
    Pop-Location
}
