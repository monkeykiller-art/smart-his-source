$ErrorActionPreference = 'Stop'

$repositoryRoot = Split-Path -Parent $PSScriptRoot
$checklistPath = Join-Path $repositoryRoot 'USER_TERMINAL_CHECKLIST.md'

if (-not (Test-Path -LiteralPath $checklistPath -PathType Leaf)) {
    throw 'USER_TERMINAL_CHECKLIST.md does not exist.'
}

$content = Get-Content -LiteralPath $checklistPath -Raw -Encoding UTF8
$requiredMarkers = @(
    '只复制标有“执行命令”的代码块',
    '确认当前使用PowerShell',
    '进入项目并确认目录',
    '检查Git状态',
    '查看具体修改',
    '配置当前终端使用Java 21',
    '后端Java改动验证',
    '前端改动验证',
    '文档改动验证',
    'PostgreSQL与Flyway迁移验证',
    '启动前端并人工检查',
    '检查敏感信息和无关文件',
    '暂存本次任务文件',
    '检查准备提交的内容',
    '创建独立Commit',
    '提交后复核',
    '失败时收集信息',
    '给Agent的结果反馈模板'
)

foreach ($marker in $requiredMarkers) {
    if (-not $content.Contains($marker)) {
        throw "User terminal checklist is missing required marker: $marker"
    }
}

$requiredCommands = @(
    'Get-Location',
    'git status',
    'git diff --staged',
    'java -version',
    '.\mvnw.cmd clean verify',
    'npm test',
    'npm run lint',
    'npm run build',
    'docker version',
    'Remove-Item Env:REQUIRE_MIGRATION_TESTS',
    'Test-Path Env:REQUIRE_MIGRATION_TESTS',
    'git commit -m',
    'git log -1 --oneline'
)

foreach ($command in $requiredCommands) {
    if (-not $content.Contains($command)) {
        throw "User terminal checklist is missing required command: $command"
    }
}

$headingCount = ([regex]::Matches($content, '^## [0-9]+\.', 'Multiline')).Count
if ($headingCount -lt 20) {
    throw "Expected at least 20 numbered checklist sections, found $headingCount."
}

if ($content.Length -lt 7500) {
    throw 'User terminal checklist is not detailed enough.'
}

Write-Host 'USER_TERMINAL_CHECKLIST.md validation passed.'
