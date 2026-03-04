@echo off
echo Checking for executable JAR...
if not exist "target\jmkvpropedit-1.5.2.jar" (
    echo JAR not found.
    echo.
    echo [ERROR] Maven not found in PATH or JAR not built.
    echo.
    echo 1. Install Maven and ensure it is in your system PATH.
    echo 2. OR If using Visual Studio Code:
    echo    a. Open the "Maven" view ^(side bar^).
    echo    b. Expand "Lifecycle" and run "package".
    echo 3. OR If using Eclipse/IntelliJ, run "Maven install".
    echo.
    echo Once the build finishes successfully in VS Code, run this script again.
    echo.
    pause
    exit /b
)

echo Launching JMkvpropedit...
start javaw -jar target\jmkvpropedit-1.5.2.jar
