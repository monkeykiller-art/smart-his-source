$ErrorActionPreference = 'Stop'

$repositoryRoot = Split-Path -Parent $PSScriptRoot
$planPath = Join-Path $repositoryRoot 'docs\IMPROVEMENT_PLAN.md'
$developerPlanPath = Join-Path $repositoryRoot 'IMPROVEMENT_PLAN.md'

if (-not (Test-Path -LiteralPath $planPath -PathType Leaf)) {
    throw "Improvement plan is missing: $planPath"
}

if (-not (Test-Path -LiteralPath $developerPlanPath -PathType Leaf)) {
    throw "Developer improvement plan is missing: $developerPlanPath"
}

$content = Get-Content -LiteralPath $planPath -Raw -Encoding UTF8
if ($content -notmatch '(?m)^# Smart HIS .+$') {
    throw 'The document title is missing.'
}

foreach ($sectionNumber in 1..7) {
    if ($content -notmatch "(?m)^## $sectionNumber\.") {
        throw "Required numbered section is missing: $sectionNumber"
    }
}

$requiredRepositories = @(
    'https://github.com/openmrs/openmrs-core',
    'https://github.com/ciyex-org/ciyex',
    'https://github.com/tntlinking-opensource/openhis',
    'https://github.com/HospitalRun/hospitalrun',
    'https://github.com/openemr/openemr'
)

foreach ($repositoryUrl in $requiredRepositories) {
    if (-not $content.Contains($repositoryUrl)) {
        throw "Reference repository is missing: $repositoryUrl"
    }
}

if ($content -match '(?im)\b(TODO|TBD)\b') {
    throw 'Improvement plan contains unfinished TODO or TBD markers.'
}

$developerContent = Get-Content -LiteralPath $developerPlanPath -Raw -Encoding UTF8
if ($developerContent -notmatch '(?m)^# Smart HIS .+$') {
    throw 'The developer plan title is missing.'
}

if ([regex]::Matches($developerContent, '(?m)^## ').Count -lt 10) {
    throw 'The developer plan must contain at least ten top-level sections.'
}

$requiredEngineeringTopics = @(
    'Maven Wrapper',
    'Testcontainers',
    'OpenAPI',
    'Flyway',
    'Outbox',
    'OpenTelemetry',
    'GitHub Actions'
)

foreach ($topic in $requiredEngineeringTopics) {
    if (-not $developerContent.Contains($topic)) {
        throw "Required engineering topic is missing: $topic"
    }
}

if ($developerContent -match '(?im)\b(TODO|TBD)\b') {
    throw 'Developer improvement plan contains unfinished TODO or TBD markers.'
}

Write-Output 'Improvement plans validation passed.'
