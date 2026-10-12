@REM Maven wrapper runner
@echo off
setlocal
where mvn >nul 2>nul
if %ERRORLEVEL% equ 0 (
  mvn %*
  exit /b %ERRORLEVEL%
)

for /f "delims=" %%i in ('dir /b /s "%USERPROFILE%\.m2\wrapper\mvn.cmd" 2^>nul') do (
  "%%i" %*
  exit /b %ERRORLEVEL%
)

for /f "delims=" %%i in ('dir /b /s "%USERPROFILE%\.m2\wrapper\dists\apache-maven-*\bin\mvn.cmd" 2^>nul') do (
  "%%i" %*
  exit /b %ERRORLEVEL%
)

echo ERROR: Maven not found on PATH or in %USERPROFILE%\.m2\wrapper.
exit /b 1
