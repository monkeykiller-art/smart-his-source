param([switch]$SkipBuild)

$ErrorActionPreference = 'Stop'
$projectRoot = Split-Path -Parent $PSScriptRoot
$envFile = Join-Path $projectRoot '.env'
$modules = @('his-auth','his-patient','his-clinical','his-operations','his-gateway')
$runDir = Join-Path $projectRoot '.run'

function Find-JavaExecutable {
    if ($env:JAVA_HOME) {
        $configured = Join-Path $env:JAVA_HOME 'bin\java.exe'
        if (Test-Path -LiteralPath $configured) { return $configured }
    }
    $toolchainRoot = Join-Path (Split-Path -Parent $projectRoot) 'work\toolchain\jdk'
    if (Test-Path -LiteralPath $toolchainRoot) {
        $match = Get-ChildItem -LiteralPath $toolchainRoot -Directory |
            Where-Object { Test-Path -LiteralPath (Join-Path $_.FullName 'bin\java.exe') } |
            Sort-Object Name -Descending | Select-Object -First 1
        if ($match) { return (Join-Path $match.FullName 'bin\java.exe') }
    }
    $java = Get-Command java.exe -ErrorAction SilentlyContinue
    if ($java) { return $java.Source }
    throw '未找到 Java。请设置 JAVA_HOME，或确认项目 work\toolchain\jdk 下存在 JDK。'
}

$javaExecutable = Find-JavaExecutable

function Find-MavenExecutable {
    if ($env:MAVEN_HOME) {
        $configured = Join-Path $env:MAVEN_HOME 'bin\mvn.cmd'
        if (Test-Path -LiteralPath $configured) { return $configured }
    }
    $toolchainRoot = Join-Path (Split-Path -Parent $projectRoot) 'work\toolchain'
    if (Test-Path -LiteralPath $toolchainRoot) {
        $match = Get-ChildItem -LiteralPath $toolchainRoot -Directory |
            Where-Object { Test-Path -LiteralPath (Join-Path $_.FullName 'bin\mvn.cmd') } |
            Sort-Object Name -Descending | Select-Object -First 1
        if ($match) { return (Join-Path $match.FullName 'bin\mvn.cmd') }
    }
    $maven = Get-Command mvn.cmd -ErrorAction SilentlyContinue
    if ($maven) { return $maven.Source }
    throw '未找到 Maven。请设置 MAVEN_HOME，或确认项目 work\toolchain 下存在 Maven。'
}

$mavenExecutable = Find-MavenExecutable

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
if (-not [Environment]::GetEnvironmentVariable('SPRING_DATASOURCE_HIKARI_MAXIMUM_POOL_SIZE', 'Process')) {
    [Environment]::SetEnvironmentVariable('SPRING_DATASOURCE_HIKARI_MAXIMUM_POOL_SIZE', '4', 'Process')
}
if (-not [Environment]::GetEnvironmentVariable('SPRING_DATASOURCE_HIKARI_MINIMUM_IDLE', 'Process')) {
    [Environment]::SetEnvironmentVariable('SPRING_DATASOURCE_HIKARI_MINIMUM_IDLE', '0', 'Process')
}

docker info *> $null
if ($LASTEXITCODE -ne 0) { throw 'Docker Desktop 未运行。请启动 Docker Desktop 后重试。' }

Push-Location $projectRoot
try {
    docker compose up -d --wait
    if ($LASTEXITCODE -ne 0) { throw 'Docker Compose 基础环境启动失败。' }
    if (-not $SkipBuild) {
        & $mavenExecutable package -DskipTests
        if ($LASTEXITCODE -ne 0) { throw 'Maven 构建失败。' }
    }

    New-Item -ItemType Directory -Force -Path $runDir | Out-Null
    Get-ChildItem -LiteralPath $runDir -Filter '*.pid' -File -ErrorAction SilentlyContinue | ForEach-Object {
        $oldPid = 0
        if ([int]::TryParse((Get-Content -LiteralPath $_.FullName -ErrorAction SilentlyContinue), [ref]$oldPid)) {
            Stop-Process -Id $oldPid -Force -ErrorAction SilentlyContinue
        }
    }
    $serviceProcesses = @{}
    foreach ($module in $modules) {
        $jar = Get-ChildItem -LiteralPath (Join-Path $projectRoot "$module\target") -Filter '*.jar' |
            Where-Object { $_.Name -notlike '*.original' } | Select-Object -First 1
        if (-not $jar) { throw "未找到 $module 的可执行 JAR" }
        $stdout = Join-Path $runDir "$module.log"
        $stderr = Join-Path $runDir "$module-error.log"
        $process = Start-Process $javaExecutable -ArgumentList @('-jar', $jar.FullName) -WorkingDirectory $projectRoot -WindowStyle Hidden -RedirectStandardOutput $stdout -RedirectStandardError $stderr -PassThru
        Set-Content -LiteralPath (Join-Path $runDir "$module.pid") -Value $process.Id
        $serviceProcesses[$module] = $process.Id
    }

    $pending = @{}
    foreach ($module in $serviceProcesses.Keys) { $pending[$module] = $serviceProcesses[$module] }
    $startupDeadline = (Get-Date).AddMinutes(2)
    while ($pending.Count -gt 0 -and (Get-Date) -lt $startupDeadline) {
        foreach ($module in @($pending.Keys)) {
            $processId = $pending[$module]
            if (-not (Get-Process -Id $processId -ErrorAction SilentlyContinue)) {
                throw "服务 $module 启动失败，请检查 $runDir\$module.log"
            }
            $logPath = Join-Path $runDir "$module.log"
            if (Test-Path -LiteralPath $logPath) {
                $started = Select-String -LiteralPath $logPath -Pattern 'Started .*Application' -Quiet
                if ($started) { $pending.Remove($module) }
            }
        }
        if ($pending.Count -gt 0) { Start-Sleep -Seconds 2 }
    }
    if ($pending.Count -gt 0) {
        throw "服务启动超时：$($pending.Keys -join ', ')。请检查 $runDir"
    }
} finally {
    Pop-Location
}

Write-Host 'Smart HIS 已启动：http://localhost:8080'
Write-Host "运行日志：$runDir"
