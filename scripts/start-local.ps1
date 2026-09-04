param([switch]$SkipBuild)

$ErrorActionPreference = 'Stop'
$projectRoot = Split-Path -Parent $PSScriptRoot
$envFile = Join-Path $projectRoot '.env'
$modules = @('his-auth','his-patient','his-clinical','his-resource','his-operations','his-collaboration','his-pharma','his-cdss','his-drg','his-emergency','his-platform','his-gateway')
$runDir = Join-Path $projectRoot '.run'

if (-not (Test-Path -LiteralPath $envFile)) {
    throw '缺少 .env。请复制 .env.example 为 .env，并填写安全的本地密钥。'
}

Get-Content -LiteralPath $envFile | ForEach-Object {
    $line = $_.Trim()
    if ($line -and -not $line.StartsWith('#')) {
        $pair = $line.Split('=', 2)
        if ($pair.Count -eq 2 -and $pair[1]) {
            [Environment]::SetEnvironmentVariable($pair[0], $pair[1], 'Process')
        }
    }
}

$required = @('POSTGRES_PASSWORD','NACOS_AUTH_TOKEN','NACOS_AUTH_IDENTITY_KEY','NACOS_AUTH_IDENTITY_VALUE','HIS_JWT_SECRET','NACOS_USERNAME','NACOS_PASSWORD')
foreach ($name in $required) {
    if (-not [Environment]::GetEnvironmentVariable($name, 'Process')) { throw "缺少环境变量 $name" }
}

if (-not [Environment]::GetEnvironmentVariable('DB_USER', 'Process')) {
    [Environment]::SetEnvironmentVariable('DB_USER', 'his', 'Process')
}
if (-not [Environment]::GetEnvironmentVariable('DB_PASSWORD', 'Process')) {
    [Environment]::SetEnvironmentVariable('DB_PASSWORD', [Environment]::GetEnvironmentVariable('POSTGRES_PASSWORD', 'Process'), 'Process')
}

docker info *> $null
if ($LASTEXITCODE -ne 0) { throw 'Docker Desktop 未运行。请启动 Docker Desktop 后重试。' }

Push-Location $projectRoot
try {
    docker compose up -d --wait
    if (-not $SkipBuild) {
        mvn package -DskipTests
        if ($LASTEXITCODE -ne 0) { throw 'Maven 构建失败。' }
    }

    New-Item -ItemType Directory -Force -Path $runDir | Out-Null
    foreach ($module in $modules) {
        $jar = Get-ChildItem -LiteralPath (Join-Path $projectRoot "$module\target") -Filter '*.jar' |
            Where-Object { $_.Name -notlike '*.original' } | Select-Object -First 1
        if (-not $jar) { throw "未找到throw $module 的可执行 JAR" }
        $stdout = Join-Path $runDir "$module.log"
        $stderr = Join-Path $runDir "$module-error.log"
        $process = Start-Process java -ArgumentList @('-jar', $jar.FullName) -WorkingDirectory $projectRoot -WindowStyle Hidden -RedirectStandardOutput $stdout -RedirectStandardError $stderr -PassThru
        Set-Content -LiteralPath (Join-Path $runDir "$module.pid") -Value $process.Id
    }
} finally {
    Pop-Location
}

Write-Host 'Smart HIS 已启动：http://localhost:8080'
Write-Host "运行日志：$runDir"
