# Bootstrap launcher for Windows. Downloads the fixed Gradle version and
# verifies the archive against Gradle's official SHA-256 before execution.
param([Parameter(ValueFromRemainingArguments = $true)][string[]]$GradleArguments)
$ErrorActionPreference = 'Stop'
$GradleVersion = '8.11.1'
$ProjectDirectory = Split-Path -Parent $PSScriptRoot
$CacheRoot = if ($env:GRADLE_USER_HOME) { $env:GRADLE_USER_HOME } else { Join-Path $env:USERPROFILE '.gradle' }
$BootstrapDirectory = Join-Path $CacheRoot "task-list-bootstrap\$GradleVersion"
$GradleLauncher = Join-Path $BootstrapDirectory "gradle-$GradleVersion\bin\gradle.bat"

if (-not (Test-Path $GradleLauncher)) {
    New-Item -ItemType Directory -Force -Path $BootstrapDirectory | Out-Null
    $Archive = Join-Path $BootstrapDirectory "gradle-$GradleVersion-bin.zip"
    $DownloadUrl = "https://services.gradle.org/distributions/gradle-$GradleVersion-bin.zip"
    Write-Host "Downloading Gradle $GradleVersion; this first run requires internet access."
    Invoke-WebRequest -UseBasicParsing -Uri "$DownloadUrl.sha256" -OutFile "$Archive.sha256.part"
    Invoke-WebRequest -UseBasicParsing -Uri $DownloadUrl -OutFile "$Archive.part"
    $ExpectedHash = (Get-Content -Raw "$Archive.sha256.part").Trim().ToLowerInvariant()
    if ($ExpectedHash -notmatch '^[0-9a-f]{64}$') { throw 'Invalid official Gradle SHA-256 response.' }
    $ActualHash = (Get-FileHash -Algorithm SHA256 "$Archive.part").Hash.ToLowerInvariant()
    if ($ExpectedHash -ne $ActualHash) { throw 'Gradle checksum mismatch; refusing to execute the download.' }
    Move-Item -Force "$Archive.part" $Archive
    Move-Item -Force "$Archive.sha256.part" "$Archive.sha256"
    Expand-Archive -Force -Path $Archive -DestinationPath $BootstrapDirectory
}

Push-Location $ProjectDirectory
try {
    & $GradleLauncher @GradleArguments
    exit $LASTEXITCODE
} finally {
    Pop-Location
}
