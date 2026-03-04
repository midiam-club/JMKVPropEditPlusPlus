@echo off
setlocal

set "MAVEN_VERSION=3.9.6"
set "MAVEN_HOME=%USERPROFILE%\.m2\wrapper\dists\apache-maven-%MAVEN_VERSION%\apache-maven-%MAVEN_VERSION%"
set "MVN_CMD=%MAVEN_HOME%\bin\mvn.cmd"
set "MAVEN_ZIP_URL=https://repo.maven.apache.org/maven2/org/apache/maven/apache-maven/%MAVEN_VERSION%/apache-maven-%MAVEN_VERSION%-bin.zip"
set "MAVEN_ZIP=%USERPROFILE%\.m2\wrapper\dists\apache-maven-%MAVEN_VERSION%\apache-maven-%MAVEN_VERSION%-bin.zip"
set "MAVEN_DIST_DIR=%USERPROFILE%\.m2\wrapper\dists\apache-maven-%MAVEN_VERSION%"

if exist "%MVN_CMD%" goto runMaven

echo Maven not found. Downloading Maven %MAVEN_VERSION%...
if not exist "%MAVEN_DIST_DIR%" mkdir "%MAVEN_DIST_DIR%"

echo Downloading from %MAVEN_ZIP_URL%...
powershell -Command "$ProgressPreference = 'SilentlyContinue'; Invoke-WebRequest -Uri '%MAVEN_ZIP_URL%' -OutFile '%MAVEN_ZIP%' -UseBasicParsing"
if errorlevel 1 (
    echo Failed to download Maven.
    exit /b 1
)

echo Extracting Maven...
powershell -Command "Expand-Archive -Path '%MAVEN_ZIP%' -DestinationPath '%MAVEN_DIST_DIR%' -Force"
if errorlevel 1 (
    echo Failed to extract Maven.
    exit /b 1
)

echo Maven installed to %MAVEN_HOME%

:runMaven
"%MVN_CMD%" %*
