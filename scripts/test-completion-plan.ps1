$ErrorActionPreference = 'Stop'

$repositoryRoot = Split-Path -Parent $PSScriptRoot
$planPath = Join-Path $repositoryRoot 'PROJECT_COMPLETION_PLAN.md'

if (-not (Test-Path -LiteralPath $planPath -PathType Leaf)) {
    throw 'PROJECT_COMPLETION_PLAN.md does not exist.'
}

$content = Get-Content -LiteralPath $planPath -Raw -Encoding UTF8
$requiredMarkers = @(
    '2026-09-14',
    '625',
    '18',
    '23',
    'PostgreSQL 16',
    'clean verify',
    'MVP',
    'ICD-10',
    'RBAC',
    'OpenAPI',
    'OpenTelemetry',
    '657.69 kB',
    'docker compose stop'
)

foreach ($marker in $requiredMarkers) {
    if (-not $content.Contains($marker)) {
        throw "Completion plan is missing required marker: $marker"
    }
}

$requiredModules = @(
    'his-auth', 'his-patient', 'his-clinical', 'his-resource',
    'his-operations', 'his-pharma', 'his-platform', 'his-cdss',
    'his-emergency', 'his-collaboration', 'his-drg'
)

foreach ($module in $requiredModules) {
    if (-not $content.Contains($module)) {
        throw "Completion plan does not cover module: $module"
    }
}

$stageHeadingCount = ([regex]::Matches($content, '^### P[0-7]', 'Multiline')).Count
if ($stageHeadingCount -ne 8) {
    throw "Expected eight delivery stages, found $stageHeadingCount."
}

$acceptanceMarker = -join @(
    [char]0x9A8C,
    [char]0x6536,
    [char]0x6807,
    [char]0x51C6,
    [char]0xFF1A
)
$acceptanceHeadingCount = ([regex]::Matches($content, [regex]::Escape($acceptanceMarker))).Count
if ($acceptanceHeadingCount -ne 8) {
    throw "Every delivery stage must define acceptance criteria; found $acceptanceHeadingCount."
}

$milestoneCount = ([regex]::Matches($content, '\| M[0-7] ')).Count
if ($milestoneCount -ne 8) {
    throw "Expected eight delivery milestones, found $milestoneCount."
}

$sectionHeadingCount = ([regex]::Matches($content, '^## ', 'Multiline')).Count
if ($sectionHeadingCount -ne 9) {
    throw "Expected nine top-level plan sections, found $sectionHeadingCount."
}

Write-Host 'PROJECT_COMPLETION_PLAN.md structure validation passed.'
