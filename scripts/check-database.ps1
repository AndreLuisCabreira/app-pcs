$ErrorActionPreference = 'Stop'

& (Join-Path $PSScriptRoot 'invoke-database-tool.ps1') -MainClass DatabaseConnectionCheck
