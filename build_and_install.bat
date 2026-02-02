@echo off
echo ========================================
echo Building UPI Money Tracker
echo ========================================
echo.
echo This will build and install the app on your connected device.
echo.

cd /d "%~dp0"

echo Cleaning previous builds...
call gradlew.bat clean

echo.
echo Building and installing APK...
call gradlew.bat installDebug --info --stacktrace > build_log.txt 2>&1

echo.
echo Build complete! Check build_log.txt for details.
echo.
pause
