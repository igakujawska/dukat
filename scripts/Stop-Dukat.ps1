[CmdletBinding()]
param()
$ErrorActionPreference = 'Stop'
Set-Location -LiteralPath (Split-Path -Parent $PSScriptRoot)
# stop zachowuje kontenery i wolumen. Nigdy nie dodawaj -v do polecen down.
docker compose stop
if ($LASTEXITCODE -ne 0) { throw 'Nie udalo sie zatrzymac kontenerow.' }
Write-Host 'Kontenery zatrzymane. Dane pozostaly w wolumenie PostgreSQL.'
