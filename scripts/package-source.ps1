param([string]$OutputPath)
$ErrorActionPreference = 'Stop'
$projectDirectory = Split-Path -Parent $PSScriptRoot
if (-not $OutputPath) { $OutputPath = Join-Path $projectDirectory 'dist\IntraTech-source.zip' }
$OutputPath = [IO.Path]::GetFullPath($OutputPath)
New-Item -ItemType Directory -Force -Path (Split-Path -Parent $OutputPath) | Out-Null
if (Test-Path -LiteralPath $OutputPath) { throw 'O destino já existe. Escolha outro nome para preservar a cópia anterior.' }
Add-Type -AssemblyName System.IO.Compression.FileSystem
$zip = [IO.Compression.ZipFile]::Open($OutputPath, [IO.Compression.ZipArchiveMode]::Create)
try {
    Get-ChildItem -LiteralPath $projectDirectory -Recurse -Force -File | ForEach-Object {
        $relative = $_.FullName.Substring($projectDirectory.Length + 1).Replace('\','/')
        $private = $relative -match '(^|/)(\.git|\.tools|target|dist|\.vscode)(/|$)' -or
            ($_.Name -like '.env*' -and $_.Name -ne '.env.example') -or
            $_.Extension -eq '.log' -or $_.Name -like '*backup*.json' -or $_.FullName -eq $OutputPath
        if (-not $private) {
            [IO.Compression.ZipFileExtensions]::CreateEntryFromFile($zip, $_.FullName, 'app-pcs-main/' + $relative) | Out-Null
        }
    }
} finally { $zip.Dispose() }
Write-Host "Projeto fonte exportado: $OutputPath"
Write-Host 'Arquivos de ambiente privados e saídas de build foram excluídos da seleção. Revise outros arquivos privados antes de publicar.'
