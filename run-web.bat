@echo off
setlocal

call build.bat
if errorlevel 1 exit /b 1

echo.
echo Starting Web UI on http://localhost:8080 ...
java -cp "bin;lib\sqlite-jdbc.jar" WebUiServer

