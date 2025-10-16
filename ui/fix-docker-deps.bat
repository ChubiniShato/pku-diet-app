@echo off
REM PKU Diet App UI - Docker Dependencies Fix Script (Windows)
REM This script resolves package.json and package-lock.json conflicts
REM and ensures all dependencies are properly installed for Docker builds

echo 🔧 Fixing Docker dependency conflicts for PKU Diet App UI...
echo.

REM Check if we're in the right directory
if not exist "package.json" (
    echo ❌ Error: package.json not found. Please run this script from the ui\ directory.
    pause
    exit /b 1
)

echo 📦 Removing old package-lock.json...
if exist "package-lock.json" del package-lock.json

echo 📦 Installing all dependencies (including dev dependencies for build)...
call npm install

echo 🔍 Checking Vite installation...
call npm list vite >nul 2>&1
if errorlevel 1 (
    echo ❌ Vite not found in dependencies
    pause
    exit /b 1
)

echo.
echo ✅ Dependencies fixed and verified!
echo.
echo 🚀 You can now run:
echo    cd ..
echo    docker compose --profile fullstack up -d
echo.
echo 📋 Next steps:
echo    1. Monitor build: docker compose logs -f ui
echo    2. Check status: docker compose ps
echo    3. Test app: curl http://localhost/health
echo    4. Open app: http://localhost
echo.
echo 🎉 Ready to build!

pause
