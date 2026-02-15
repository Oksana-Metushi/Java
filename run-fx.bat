@echo off
setlocal

call build.bat
if errorlevel 1 exit /b 1

call build-fx.bat
if errorlevel 1 (
  echo.
  echo JavaFX is not available on your JDK. Use one of these instead:
  echo   .\run-web.bat    - open http://localhost:8080 in your browser
  echo   .\run-console.bat - text menu in the terminal
  echo.
  echo To use the desktop GUI: install JDK 8, or download JavaFX SDK and set JAVAFX_HOME.
  exit /b 1
)

echo.
echo Starting JavaFX GUI...
echo Demo logins: student1/student123 ^| librarian1/lib123 ^| admin/admin123
echo.

REM Run from project root so assets path works (file:assets/images/logo.png)
REM JDK 8: JavaFX is included. JDK 11+: use --module-path and --add-modules if JavaFX is installed.
java -cp "bin;lib\sqlite-jdbc.jar" LibraryManagementApp

if errorlevel 1 (
  echo.
  echo If you see "ClassNotFoundException: javafx..." then you need JavaFX.
  echo - JDK 8-10: JavaFX is included.
  echo - JDK 11+: Download JavaFX from https://gluonhq.com/products/javafx/ and run with:
  echo   java --module-path "path-to-javafx-sdk\lib" --add-modules javafx.controls,javafx.graphics,javafx.fxml -cp "bin;lib\sqlite-jdbc.jar" LibraryManagementApp
  exit /b 1
)
