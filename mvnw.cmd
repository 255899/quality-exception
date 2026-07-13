@echo off
setlocal

set "ERROR_CODE=0"

if "%JAVA_HOME%"=="" (
  echo Error: JAVA_HOME is not set.
  echo Please set JAVA_HOME to your JDK installation directory.
  exit /b 1
)

if not exist "%JAVA_HOME%\bin\java.exe" (
  echo Error: "%JAVA_HOME%\bin\java.exe" not found.
  exit /b 1
)

set "WRAPPER_DIR=%~dp0.mvn\wrapper"
set "WRAPPER_JAR=%WRAPPER_DIR%\maven-wrapper.jar"
set "WRAPPER_LAUNCHER=org.apache.maven.wrapper.MavenWrapperMain"

set "WRAPPER_URL=https://repo.maven.apache.org/maven2/org/apache/maven/wrapper/maven-wrapper/3.2.0/maven-wrapper-3.2.0.jar"
for /f "usebackq tokens=1,2 delims==" %%A in ("%WRAPPER_DIR%\maven-wrapper.properties") do (
  if "%%A"=="wrapperUrl" set "WRAPPER_URL=%%B"
)

if not exist "%WRAPPER_JAR%" (
  echo Downloading Maven Wrapper from %WRAPPER_URL%
  powershell -NoProfile -ExecutionPolicy Bypass -Command "$p='%WRAPPER_JAR%'; $u='%WRAPPER_URL%'; [Net.ServicePointManager]::SecurityProtocol=[Net.SecurityProtocolType]::Tls12; (New-Object Net.WebClient).DownloadFile($u, $p)"
  if not exist "%WRAPPER_JAR%" (
    echo Failed to download Maven Wrapper.
    exit /b 1
  )
)

"%JAVA_HOME%\bin\java.exe" %JAVA_OPTS% -Dmaven.multiModuleProjectDirectory="%~dp0." -classpath "%WRAPPER_JAR%" %WRAPPER_LAUNCHER% %*

set "ERROR_CODE=%ERRORLEVEL%"
endlocal & set "ERROR_CODE=%ERROR_CODE%"
exit /b %ERROR_CODE%
