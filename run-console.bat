@echo off
setlocal

call build.bat
if errorlevel 1 exit /b 1

echo.
java -cp "bin;lib\sqlite-jdbc.jar" LibraryManagementSystem

