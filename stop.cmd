@echo off
echo Stopping Quality Exception Handler...
taskkill /F /FI "WINDOWTITLE eq QualityExceptionServer*" 2>nul
taskkill /F /IM java.exe /T 2>nul
echo Stopped.
timeout /t 2 >nul
