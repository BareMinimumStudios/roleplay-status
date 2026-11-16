param([ValidateSet('both', 'fabric', 'neoforge')][string]$Loader = 'both')
$ErrorActionPreference = 'Stop'
$previousLoader = $env:RELEASE_LOADER
Push-Location (Split-Path -Parent $PSScriptRoot)
try {
    $task = if ($Loader -eq 'both') { 'build' } else { ":${Loader}:build" }
    & .\gradlew.bat $task --console=plain
    if ($LASTEXITCODE -ne 0) { throw 'Build failed' }
    $env:RELEASE_LOADER = if ($Loader -eq 'both') { $null } else { $Loader }
    & python scripts/verify_release.py
    if ($LASTEXITCODE -ne 0) { throw 'Release verification failed' }
} finally {
    $env:RELEASE_LOADER = $previousLoader
    Pop-Location
}
