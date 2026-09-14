$ErrorActionPreference = 'Stop'

$scriptPath = Join-Path $PSScriptRoot 'start-local.ps1'
if (-not (Test-Path -LiteralPath $scriptPath -PathType Leaf)) {
    throw 'start-local.ps1 is missing.'
}

$bytes = [IO.File]::ReadAllBytes($scriptPath)
if ($bytes.Length -lt 3 -or $bytes[0] -ne 0xEF -or $bytes[1] -ne 0xBB -or $bytes[2] -ne 0xBF) {
    throw 'start-local.ps1 must use a UTF-8 BOM for Windows PowerShell 5.1 compatibility.'
}

$tokens = $null
$parseErrors = $null
[System.Management.Automation.Language.Parser]::ParseFile(
    $scriptPath,
    [ref]$tokens,
    [ref]$parseErrors
) | Out-Null
if ($parseErrors.Count -gt 0) {
    throw ('start-local.ps1 has parse errors: ' + ($parseErrors.Message -join '; '))
}

$content = [IO.File]::ReadAllText($scriptPath)
$requiredFragments = @(
    "Join-Path `$projectRoot '.env'",
    'docker info',
    'docker compose up -d --wait',
    "throw 'Docker Compose",
    '-WindowStyle Hidden',
    "-Pattern 'Started .*Application' -Quiet",
    'Get-Process -Id $processId',
    '.AddMinutes(2)',
    'Join-Path $runDir "$module.pid"'
)
foreach ($fragment in $requiredFragments) {
    if (-not $content.Contains($fragment)) {
        throw "start-local.ps1 is missing required behavior: $fragment"
    }
}

Write-Output 'start-local.ps1 compatibility validation passed.'
