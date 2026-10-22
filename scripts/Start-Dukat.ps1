#do odpanlenia Docker albo własnie IntelliJ + ng serve, nie oba
[CmdletBinding()]
param()
$ErrorActionPreference = 'Stop'
$projectRoot = Split-Path -Parent $PSScriptRoot
Set-Location -LiteralPath $projectRoot
Write-Host 'TRYB DOCKER: ten skrypt uruchamia tez backend i frontend.'
Write-Host 'Do pracy z Run w IntelliJ uzywamy zamiast niego: .\scripts\Prepare-IntelliJ.ps1'
if (!(Test-Path -LiteralPath '.env')) { & (Join-Path $PSScriptRoot 'Initialize-Dukat.ps1') }
docker info --format '{{.ServerVersion}}' | Out-Null
if ($LASTEXITCODE -ne 0) { throw 'Uruchom Docker Desktop i ponow probe.' }
$settings = @{}
Get-Content -LiteralPath '.env' | ForEach-Object { if ($_ -match '^([A-Z_]+)=(.*)$') { $settings[$Matches[1]] = $Matches[2] } }
$backendPort = if ($settings['SERVER_PORT']) { [int]$settings['SERVER_PORT'] } else { 8082 }
$dbPort = if ($settings['DB_PORT']) { [int]$settings['DB_PORT'] } else { 5433 }
foreach ($item in @(@{Port=$backendPort;Service='backend'},@{Port=4200;Service='frontend'},@{Port=$dbPort;Service='db'})) {
    $running = docker compose ps --status running -q $item.Service
    if (!$running) {
        $listeners = Get-NetTCPConnection -State Listen -LocalPort $item.Port -ErrorAction SilentlyContinue
        foreach ($listener in $listeners) {
            $processInfo = Get-Process -Id $listener.OwningProcess -ErrorAction SilentlyContinue
            throw "Port $($item.Port) zajety przez $($processInfo.ProcessName), PID $($listener.OwningProcess). Zatrzymaj ten proces w jego terminalu/IntelliJ i uruchom ponownie. Skrypt nie zabija procesow."
        }
    }
}
docker compose up -d --build --wait --wait-timeout 240
if ($LASTEXITCODE -ne 0) { throw 'Start nie powiodl sie. Sprawdz: docker compose logs --tail=80 backend' }
Write-Host 'Dukat: http://localhost:4200 | Swagger: http://localhost:8082/swagger-ui/index.html'
