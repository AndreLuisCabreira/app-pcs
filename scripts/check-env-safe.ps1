$ErrorActionPreference = 'Stop'

$projectDirectory = Split-Path -Parent $PSScriptRoot
$environmentFile = Join-Path $projectDirectory '.env'

if (-not (Test-Path -LiteralPath $environmentFile)) {
    throw 'Arquivo .env não encontrado.'
}

foreach ($line in Get-Content -LiteralPath $environmentFile) {
    $clean = $line.Trim()
    if (-not $clean -or $clean.StartsWith('#') -or -not $clean.Contains('=')) {
        continue
    }

    $parts = $clean.Split('=', 2)
    $name = $parts[0].Trim()
    $value = $parts[1].Trim().Trim('"')
    $type = if ($value.StartsWith('sb_secret_')) {
        'CHAVE_SECRETA_SUPABASE'
    } elseif ($value.StartsWith('sb_publishable_')) {
        'CHAVE_PUBLICAVEL_SUPABASE'
    } elseif ($value.StartsWith('jdbc:postgresql://')) {
        'URL_JDBC_POSTGRESQL'
    } elseif ($value.StartsWith('jdbc:mysql://')) {
        'URL_JDBC_MYSQL'
    } elseif ([string]::IsNullOrWhiteSpace($value)) {
        'VAZIO'
    } else {
        'PREENCHIDO'
    }

    [PSCustomObject]@{ Variavel = $name; Tipo = $type }
}
