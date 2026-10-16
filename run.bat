@echo off
title EduTrack - Academic Platform and Web Server
echo ========================================================
echo   Starting EduTrack University Platform and Web Server
echo ========================================================
echo.

echo Compiling Java source files...
node build.js
if errorlevel 1 (
    echo Compilation failed.
    pause
    exit /b 1
)

echo Starting Java Backend and Web Server...
java -cp bin edutrack.Main
pause
