@REM Root Maven runner redirecting to backend/
@echo off
setlocal
cd /d "%~dp0backend"
call mvnw.cmd %*
exit /b %ERRORLEVEL%
