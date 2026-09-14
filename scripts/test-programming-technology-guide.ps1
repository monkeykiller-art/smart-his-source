$ErrorActionPreference = 'Stop'

$repositoryRoot = Split-Path -Parent $PSScriptRoot
$guidePath = Join-Path $repositoryRoot 'PROGRAMMING_TECHNOLOGY_GUIDE.md'

if (-not (Test-Path -LiteralPath $guidePath -PathType Leaf)) {
    throw 'PROGRAMMING_TECHNOLOGY_GUIDE.md does not exist.'
}

$content = Get-Content -LiteralPath $guidePath -Raw -Encoding UTF8
$requiredMarkers = @(
    'Git 版本管理',
    'Java 21 编程基础',
    'Maven 与多模块工程',
    'Spring Boot 后端开发',
    'HTTP、REST 与接口契约',
    'PostgreSQL 与 SQL',
    'JavaScript 与 TypeScript',
    'React 前端开发',
    '自动化测试',
    'Docker 与容器',
    'CI/CD、Linux 与部署',
    '安全与医疗数据保护',
    '如何与 Agent 协作',
    '推荐学习顺序',
    '技能验收清单'
)

foreach ($marker in $requiredMarkers) {
    if (-not $content.Contains($marker)) {
        throw "Programming technology guide is missing required marker: $marker"
    }
}

$requiredCommands = @(
    'git status',
    'git commit',
    '.\mvnw.cmd clean verify',
    'npm test',
    'npm run lint',
    'npm run build',
    'docker version'
)

foreach ($command in $requiredCommands) {
    if (-not $content.Contains($command)) {
        throw "Programming technology guide is missing required command: $command"
    }
}

$headingCount = ([regex]::Matches($content, '^## [0-9]+\.', 'Multiline')).Count
if ($headingCount -lt 20) {
    throw "Expected at least 20 numbered guide sections, found $headingCount."
}

if ($content.Length -lt 10000) {
    throw 'Programming technology guide is not detailed enough.'
}

Write-Host 'PROGRAMMING_TECHNOLOGY_GUIDE.md validation passed.'
