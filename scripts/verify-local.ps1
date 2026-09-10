[CmdletBinding()]
param(
    [string]$JavaHome = $env:JAVA_HOME,
    [string]$MavenHome = $env:MAVEN_HOME,
    [string]$MavenRepository,
    [switch]$RequireEmptyMavenRepository
)

$ErrorActionPreference = "Stop"
$projectRoot = Split-Path -Parent $PSScriptRoot
$workspaceRoot = Split-Path -Parent $projectRoot

function Find-ToolHome {
    param(
        [string]$ConfiguredHome,
        [string]$SearchRoot,
        [string]$Executable
    )

    if ($ConfiguredHome -and (Test-Path (Join-Path $ConfiguredHome $Executable))) {
        return (Resolve-Path $ConfiguredHome).Path
    }

    if (Test-Path $SearchRoot) {
        $match = Get-ChildItem -Path $SearchRoot -Directory |
            Where-Object { Test-Path (Join-Path $_.FullName $Executable) } |
            Sort-Object Name -Descending |
            Select-Object -First 1
        if ($match) {
            return $match.FullName
        }
    }

    return $null
}

$JavaHome = Find-ToolHome `
    -ConfiguredHome $JavaHome `
    -SearchRoot (Join-Path $workspaceRoot "work\toolchain\jdk") `
    -Executable "bin\java.exe"
if (-not $JavaHome) {
    throw "Java 21 was not found. Set JAVA_HOME or pass -JavaHome."
}

$MavenHome = Find-ToolHome `
    -ConfiguredHome $MavenHome `
    -SearchRoot (Join-Path $workspaceRoot "work\toolchain") `
    -Executable "bin\mvn.cmd"
if (-not $MavenHome) {
    throw "Maven 3.9.16 was not found. Set MAVEN_HOME or pass -MavenHome."
}

if (-not $MavenRepository) {
    $MavenRepository = Join-Path $workspaceRoot "work\maven-repo"
}

if ($RequireEmptyMavenRepository -and (Test-Path $MavenRepository)) {
    $existingItem = Get-ChildItem -Force -Path $MavenRepository | Select-Object -First 1
    if ($existingItem) {
        throw "Maven repository must be empty: $MavenRepository"
    }
}
New-Item -ItemType Directory -Force -Path $MavenRepository | Out-Null

$env:JAVA_HOME = $JavaHome
$env:MAVEN_HOME = $MavenHome
$env:PATH = "$JavaHome\bin;$MavenHome\bin;$env:PATH"

$previousErrorActionPreference = $ErrorActionPreference
$ErrorActionPreference = "Continue"
$javaVersion = & (Join-Path $JavaHome "bin\java.exe") -version 2>&1
$javaExitCode = $LASTEXITCODE
$ErrorActionPreference = $previousErrorActionPreference
if ($javaExitCode -ne 0 -or ($javaVersion -join "`n") -notmatch 'version "21\.') {
    throw "Smart HIS requires Java 21. Detected:`n$($javaVersion -join "`n")"
}

$maven = Join-Path $MavenHome "bin\mvn.cmd"
Write-Host "JAVA_HOME=$JavaHome"
Write-Host "MAVEN_HOME=$MavenHome"
Write-Host "MAVEN_REPOSITORY=$MavenRepository"

& $maven -B -ntp "-Dmaven.repo.local=$MavenRepository" clean verify
exit $LASTEXITCODE
