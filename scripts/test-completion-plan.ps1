$ErrorActionPreference = 'Stop'

$repositoryRoot = Split-Path -Parent $PSScriptRoot
$planPath = Join-Path $repositoryRoot 'PROJECT_COMPLETION_PLAN.md'

if (-not (Test-Path -LiteralPath $planPath -PathType Leaf)) {
    throw 'PROJECT_COMPLETION_PLAN.md does not exist.'
}

$content = Get-Content -LiteralPath $planPath -Raw -Encoding UTF8
$requiredMarkers = @(
    'smart-his-source',
    '625',
    'Testcontainers PostgreSQL',
    'clean verify',
    'OpenAPI',
    'OpenTelemetry',
    'R0',
    'R1',
    'R2',
    'R3',
    'R4',
    'R5'
)

foreach ($marker in $requiredMarkers) {
    if (-not $content.Contains($marker)) {
        throw "Completion plan is missing required marker: $marker"
    }
}

$requiredModules = @(
    'his-auth',
    'his-patient',
    'his-clinical',
    'his-resource',
    'his-operations',
    'his-pharma',
    'his-platform',
    'his-cdss',
    'his-emergency',
    'his-collaboration',
    'his-drg'
)

foreach ($module in $requiredModules) {
    if (-not $content.Contains($module)) {
        throw "Completion plan does not cover module: $module"
    }
}

$stageHeadingCount = ([regex]::Matches($content, '^### .*R[0-5]', 'Multiline')).Count
if ($stageHeadingCount -ne 6) {
    throw "Expected six delivery stages, found $stageHeadingCount."
}

$acceptanceMarker = -join @(
    [char]0x9A8C,
    [char]0x6536,
    [char]0x6807,
    [char]0x51C6,
    [char]0xFF1A
)
$acceptanceHeadingCount = ([regex]::Matches($content, [regex]::Escape($acceptanceMarker))).Count
if ($acceptanceHeadingCount -lt 6) {
    throw 'Every delivery stage must define acceptance criteria.'
}

Write-Host 'PROJECT_COMPLETION_PLAN.md structure validation passed.'
