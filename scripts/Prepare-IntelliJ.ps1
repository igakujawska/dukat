# Tryb nauki: PostgreSQL w Dockerze, backend z Run w IntelliJ, Angular z npm start.
[CmdletBinding()]
param()
$ErrorActionPreference = 'Stop'
$projectRoot = Split-Path -Parent $PSScriptRoot
Set-Location -LiteralPath $projectRoot

if (!(Test-Path -LiteralPath '.env')) {
    throw 'Brak .env. Najpierw przygotuj konfiguracje zgodnie z README.md.'
}
docker info --format '{{.ServerVersion}}' | Out-Null
if ($LASTEXITCODE -ne 0) { throw 'Uruchom Docker Desktop i ponow probe.' }
docker compose config --quiet
if ($LASTEXITCODE -ne 0) { throw 'Popraw konfiguracje Compose/.env przed przelaczeniem trybu.' }

# Zatrzymujemy tylko kontenery tego projektu. Baza i jej wolumen pozostaja.
Write-Host 'TRYB INTELLIJ: zatrzymuje backend i frontend z Dockera.'
docker compose stop backend frontend
if ($LASTEXITCODE -ne 0) { throw 'Nie udalo sie zatrzymac kontenerow aplikacji.' }
# Ustawienie dotyczy tez kontenerow utworzonych przez starsza wersje Compose.
$applicationContainers = @(docker compose ps --all --quiet backend frontend)
if ($LASTEXITCODE -ne 0) { throw 'Nie udalo sie odczytac kontenerow aplikacji.' }
if ($applicationContainers.Count -gt 0) {
    docker update --restart=no @applicationContainers | Out-Null
    if ($LASTEXITCODE -ne 0) { throw 'Nie udalo sie wylaczyc autostartu kontenerow aplikacji.' }
}
docker compose up -d --wait --wait-timeout 90 db
if ($LASTEXITCODE -ne 0) { throw 'PostgreSQL nie jest gotowy. Sprawdz: docker compose logs --tail=50 db' }

$settings = @{}
Get-Content -LiteralPath '.env' | ForEach-Object {
    if ($_ -match '^([A-Z_]+)=(.*)$') { $settings[$Matches[1]] = $Matches[2] }
}
$backendPort = if ($settings['SERVER_PORT']) { [int]$settings['SERVER_PORT'] } else { 8082 }
foreach ($port in @($backendPort, 4200)) {
    $listeners = Get-NetTCPConnection -State Listen -LocalPort $port -ErrorAction SilentlyContinue
    foreach ($listener in $listeners) {
        $processInfo = Get-Process -Id $listener.OwningProcess -ErrorAction SilentlyContinue
        throw "Port $port zajmuje juz $($processInfo.ProcessName), PID $($listener.OwningProcess). Zatrzymaj poprzedni Run w IntelliJ lub npm start przez Ctrl+C. Potem ponow to polecenie."
    }
}
Write-Host "Gotowe. Port $backendPort jest wolny dla Run w IntelliJ; port 4200 dla Angulara."
Write-Host '1. IntelliJ: uruchom DemoappApplication (katalog roboczy = glowny folder projektu).'
Write-Host '2. Terminal w glownym folderze: npm --prefix dukat-ui start'
Write-Host '3. Przegladarka: http://localhost:4200'
