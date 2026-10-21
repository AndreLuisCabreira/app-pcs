$ErrorActionPreference = 'Stop'

function Get-IntraTechJdk {
    $candidates = @()
    if ($env:JAVA_HOME) { $candidates += $env:JAVA_HOME }
    $command = Get-Command javac -ErrorAction SilentlyContinue
    if ($command) { $candidates += Split-Path -Parent (Split-Path -Parent $command.Source) }
    $extensions = Join-Path ([Environment]::GetFolderPath('UserProfile')) '.vscode\extensions'
    if (Test-Path -LiteralPath $extensions) {
        $candidates += Get-ChildItem -LiteralPath $extensions -Directory -Filter 'redhat.java-*' |
            ForEach-Object { Get-ChildItem -LiteralPath $_.FullName -Recurse -File -Filter 'javac.exe' } |
            ForEach-Object { Split-Path -Parent (Split-Path -Parent $_.FullName) }
    }
    foreach ($candidate in @($candidates | Select-Object -Unique)) {
        $compiler = Join-Path $candidate 'bin\javac.exe'
        if (-not (Test-Path -LiteralPath $compiler)) { continue }
        $version = (& $compiler -version 2>&1 | Out-String)
        if ($LASTEXITCODE -eq 0 -and $version -match 'javac\s+(\d+)' -and [int]$Matches[1] -ge 21) {
            return $candidate
        }
    }
    throw 'JDK 21 ou superior não encontrado. Configure JAVA_HOME ou adicione javac ao PATH.'
}
