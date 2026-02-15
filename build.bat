@echo off
setlocal

if not exist bin mkdir bin

echo Compiling Java sources into bin\ ...
REM Exclude JavaFX app files (compile them with build-fx.bat when JavaFX is available)
javac -d bin ^
  app\AppContext.java ^
  app\LibraryManagementSystem.java ^
  app\WebUiServer.java ^
  models\*.java ^
  services\*.java ^
  stores\*.java ^
  db\*.java ^
  util\*.java ^
  web\*.java

if errorlevel 1 (
  echo.
  echo Build failed.
  exit /b 1
)

echo.
echo Build OK.
exit /b 0

