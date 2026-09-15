$ErrorActionPreference = 'Stop'

$repositoryRoot = Split-Path -Parent $PSScriptRoot
$planPath = Join-Path $repositoryRoot 'PROJECT_COMPLETION_PLAN.md'

if (-not (Test-Path -LiteralPath $planPath -PathType Leaf)) {
    throw 'PROJECT_COMPLETION_PLAN.md does not exist.'
}

$content = Get-Content -LiteralPath $planPath -Raw -Encoding UTF8
$requiredMarkers = @(
    '2026-09-15',
    'PostgreSQL 16',
    'clean verify',
    'npm test',
    'npm run lint',
    'npm run build'
)

foreach ($marker in $requiredMarkers) {
    if (-not $content.Contains($marker)) {
        throw "Completion plan is missing required marker: $marker"
    }
}

$requiredModules = @(
    'his-common', 'his-gateway', 'his-auth', 'his-patient',
    'his-clinical', 'his-operations', 'his-migration-tests',
    'smart-his-frontend'
)

foreach ($module in $requiredModules) {
    if (-not $content.Contains($module)) {
        throw "Completion plan does not cover module: $module"
    }
}

$stageHeadingCount = ([regex]::Matches($content, '^### M[0-4]', 'Multiline')).Count
if ($stageHeadingCount -ne 5) {
    throw "Expected five delivery stages, found $stageHeadingCount."
}

$acceptanceMarker = -join @(
    [char]0x9A8C,
    [char]0x6536,
    [char]0x6807,
    [char]0x51C6,
    [char]0xFF1A
)
$acceptanceHeadingCount = ([regex]::Matches($content, '^' + [regex]::Escape($acceptanceMarker), 'Multiline')).Count
if ($acceptanceHeadingCount -ne 5) {
    throw "Every delivery stage must define acceptance criteria; found $acceptanceHeadingCount."
}

Write-Host 'PROJECT_COMPLETION_PLAN.md basic outpatient scope validation passed.'
