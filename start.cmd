@echo off
setlocal

set JAVA_HOME=C:\Program Files\Java\jdk1.8.0_151
set PATH=%JAVA_HOME%\bin;%PATH%
set PORT=8080
set URL=http://localhost:%PORT%/



REM Launch Spring Boot in background
start "QualityExceptionServer" /B cmd /c ".\mvnw.cmd spring-boot:run > app.log 2>&1"


set /a TRIED=0
:WAIT_LOOP
set /a TRIED+=1
if %TRIED% GTR 90 (
    echo [Timeout] Server did not start within 90s. Check app.log
    pause
    exit /b 1
)

powershell -NoProfile -Command "$ok = $false; try { $c = New-Object System.Net.Sockets.TcpClient; $iar = $c.BeginConnect('127.0.0.1', %PORT%, $null, $null); $ok = $iar.AsyncWaitHandle.WaitOne(1000) -and $c.Connected; $c.Close() } catch {}; if ($ok) { exit 0 } else { exit 1 }" >nul 2>&1
if %ERRORLEVEL% EQU 0 goto READY

REM Print progress every 5 seconds (no modulo, just a simple counter)
if %TRIED% EQU 5  echo   waiting 5s...
if %TRIED% EQU 15 echo   waiting 15s...
if %TRIED% EQU 30 echo   waiting 30s...
if %TRIED% EQU 60 echo   waiting 60s...
goto WAIT_LOOP

:READY


start "" "%URL%"

echo.
echo http://localhost:%PORT%/
echo.
pause
