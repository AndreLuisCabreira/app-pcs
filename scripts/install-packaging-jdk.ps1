$ErrorActionPreference = 'Stop'

$projectDirectory = Split-Path -Parent $PSScriptRoot
$toolsDirectory = Join-Path $projectDirectory '.tools'
$downloadDirectory = Join-Path $toolsDirectory 'downloads'
$archivePath = Join-Path $downloadDirectory 'temurin-jdk21-windows-x64.zip'
$jdkDirectory = Join-Path $toolsDirectory 'jdk-21'
$apiUrl = 'https://api.adoptium.net/v3/assets/latest/21/hotspot?architecture=x64&image_type=jdk&os=windows&vendor=eclipse'

function Assert-ProjectChildPath {
    param([Parameter(Mandatory)][string]$Path)

    $projectRoot = [IO.Path]::GetFullPath($projectDirectory).TrimEnd('\') + '\'
    $resolved = [IO.Path]::GetFullPath($Path)
    if (-not $resolved.StartsWith($projectRoot, [StringComparison]::OrdinalIgnoreCase)) {
        throw "Caminho fora do projeto: $resolved"
    }
}

Assert-ProjectChildPath -Path $toolsDirectory
Assert-ProjectChildPath -Path $downloadDirectory
Assert-ProjectChildPath -Path $archivePath
Assert-ProjectChildPath -Path $jdkDirectory

$existingJpackage = Get-ChildItem -LiteralPath $jdkDirectory -Recurse -Filter 'jpackage.exe' -ErrorAction SilentlyContinue |
    Select-Object -First 1 -ExpandProperty FullName
if ($existingJpackage) {
    Write-Host "JDK de empacotamento já disponível: $(Split-Path -Parent (Split-Path -Parent $existingJpackage))"
    exit 0
}

New-Item -ItemType Directory -Force -Path $downloadDirectory | Out-Null

Write-Host 'Consultando a versão mais recente do Eclipse Temurin JDK 21...'
$release = Invoke-RestMethod -Uri $apiUrl -Headers @{ Accept = 'application/json' }
$package = $release[0].binary.package
if (-not $package.link -or -not $package.checksum) {
    throw 'A API do Adoptium não retornou o download e o checksum esperados.'
}

Write-Host "Baixando $($package.name)..."
Invoke-WebRequest -Uri $package.link -OutFile $archivePath -UseBasicParsing

$actualChecksum = (Get-FileHash -LiteralPath $archivePath -Algorithm SHA256).Hash
if ($actualChecksum -ne $package.checksum) {
    throw 'O checksum SHA-256 do JDK não corresponde ao publicado pelo Adoptium.'
}

if (Test-Path -LiteralPath $jdkDirectory) {
    Remove-Item -LiteralPath $jdkDirectory -Recurse -Force
}
New-Item -ItemType Directory -Force -Path $jdkDirectory | Out-Null
Expand-Archive -LiteralPath $archivePath -DestinationPath $jdkDirectory -Force

$jpackage = Get-ChildItem -LiteralPath $jdkDirectory -Recurse -Filter 'jpackage.exe' |
    Select-Object -First 1 -ExpandProperty FullName
if (-not $jpackage) {
    throw 'O JDK foi extraído, mas jpackage.exe não foi encontrado.'
}

Remove-Item -LiteralPath $archivePath -Force
Write-Host "JDK de empacotamento instalado em: $(Split-Path -Parent (Split-Path -Parent $jpackage))"
