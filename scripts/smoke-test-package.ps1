$ErrorActionPreference = 'Stop'

$projectDirectory = Split-Path -Parent $PSScriptRoot
$executable = Join-Path $projectDirectory 'target\package-output\IntraTech PC Builder\IntraTech PC Builder.exe'

if (-not (Test-Path -LiteralPath $executable)) {
    throw 'Executável empacotado não encontrado. Execute scripts\package-windows.ps1 primeiro.'
}

$process = Start-Process -FilePath $executable -WorkingDirectory (Split-Path -Parent $executable) -WindowStyle Hidden -PassThru
try {
    Start-Sleep -Seconds 5
    $process.Refresh()
    if ($process.HasExited) {
        throw "O aplicativo encerrou durante a inicialização com o código $($process.ExitCode)."
    }
    Write-Host 'O executável empacotado iniciou e permaneceu ativo durante o teste.'
} finally {
    $process.Refresh()
    if (-not $process.HasExited) {
        Stop-Process -Id $process.Id -Force
    }
}
