# Uruchom raz z folderu projektu: .\scripts\Initialize-Dukat.ps1
[CmdletBinding()]
param()
$ErrorActionPreference = 'Stop'
$projectRoot = Split-Path -Parent $PSScriptRoot
$envFile = Join-Path $projectRoot '.env'
if (Test-Path -LiteralPath $envFile) {
    Write-Host '.env juz istnieje. Zachowano dotychczasowe hasla i klucz JWT.'
    exit 0
}
function New-LocalSecret([int]$Length = 48) {
    $bytes = New-Object byte[] $Length
    $generator = [System.Security.Cryptography.RandomNumberGenerator]::Create()
    try { $generator.GetBytes($bytes) } finally { $generator.Dispose() }
    return [Convert]::ToBase64String($bytes)
}
$dbSecret = New-LocalSecret 24
$jwtSecret = New-LocalSecret 64
$profileSecret = New-LocalSecret 18
$content = @"
DB_HOST=localhost
DB_PORT=5433
DB_NAME=dukat_db
DB_USER=dukatuser
DB_PASSWORD=$dbSecret
SERVER_PORT=8082
JWT_SECRET=$jwtSecret
DUKAT_BOOTSTRAP_USERNAME=user
DUKAT_BOOTSTRAP_PASSWORD=$profileSecret
DUKAT_BOOTSTRAP_EMAIL=user@dukat.local
"@
[IO.File]::WriteAllText($envFile, $content, (New-Object Text.UTF8Encoding $false))
Write-Host 'Utworzono ignorowany przez Git plik .env. Haslo profilu lokalnego jest w DUKAT_BOOTSTRAP_PASSWORD.'
Write-Host 'Dla juz istniejacego wolumenu ustaw DB_USER i DB_PASSWORD zgodne z dotychczasowa baza przed startem.'
