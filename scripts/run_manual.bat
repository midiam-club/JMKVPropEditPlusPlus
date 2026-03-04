@echo off
if not exist build mkdir build
if not exist build\res mkdir build\res
if not exist build\io\github\brunorex\resources mkdir build\io\github\brunorex\resources
xcopy /s /y src\res\*.png build\res\ >nul
xcopy /s /y src\io\github\brunorex\resources\*.properties build\io\github\brunorex\resources\ >nul
echo Compiling...
javac -encoding UTF-8 -sourcepath src -cp "lib\ini4j\ini4j-0.5.4.jar;lib\commons-io\commons-io-2.11.0.jar" -d build src\io\github\brunorex\JMkvpropedit.java
if %errorlevel% neq 0 (
    echo Compilation failed!
    pause
    exit /b %errorlevel%
)
echo Running...
java -cp "build;lib\ini4j\ini4j-0.5.4.jar;lib\commons-io\commons-io-2.11.0.jar" io.github.brunorex.JMkvpropedit
pause
