@echo off
setlocal

REM Compile JavaFX GUI classes. Requires JavaFX on the classpath (JDK 8-10) or module path (JDK 11+).
REM For JDK 11+: set JAVAFX_HOME to the JavaFX SDK folder (e.g. C:\javafx-sdk-21) then run this script.

if not exist bin mkdir bin

echo Compiling JavaFX GUI sources...
javac -d bin -cp "bin;lib\sqlite-jdbc.jar" ^
  app\FxHelper.java ^
  app\LibraryManagementApp.java ^
  app\LoginScene.java ^
  app\RegisterScene.java ^
  app\StudentScene.java ^
  app\LibrarianScene.java ^
  app\AdminScene.java

if errorlevel 1 (
  echo.
  echo JavaFX not found. Your JDK does not include JavaFX.
  echo Use run-web.bat or run-console.bat to run the app without JavaFX.
  exit /b 1
)

echo JavaFX build OK.
exit /b 0
