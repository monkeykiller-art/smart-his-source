$ErrorActionPreference = 'Stop'

$repositoryRoot = Split-Path -Parent $PSScriptRoot
$planPath = Join-Path $repositoryRoot 'docs\IMPROVEMENT_PLAN.md'

if (-not (Test-Path -LiteralPath $planPath -PathType Leaf)) {
    throw "Improvement plan is missing: $planPath"
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

Write-Output 'Improvement plan validation passed.'
