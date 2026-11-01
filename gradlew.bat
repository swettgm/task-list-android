@echo off
rem Offline-prepared bootstrap launcher; no official wrapper JAR is bundled.
setlocal
powershell.exe -NoLogo -NoProfile -ExecutionPolicy Bypass -File "%~dp0gradle\bootstrap.ps1" %*
exit /b %ERRORLEVEL%
