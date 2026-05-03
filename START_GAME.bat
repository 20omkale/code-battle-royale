@echo off
title CodeBattle Royale Launcher
color 0A

echo ==========================================
echo       LAUNCHING CODEBATTLE ROYALE
echo ==========================================
echo [*] Initializing Game Engine...

:: Compile silently
if not exist build mkdir build
javac -encoding UTF-8 -d build src/com/codebattle/*.java

if %ERRORLEVEL% neq 0 (
    color 0C
    echo [-] Engine Initialization Failed! Please check your Java installation.
    pause
    exit /b
)

echo [+] Engine Ready. Starting Matchmaking Server...
:: Start server minimized in background. 
start /MIN "CodeBattle Server" java -cp build com.codebattle.GameServer

:: Wait 2 seconds to give the server time to bind the port
timeout /t 2 /nobreak > nul

echo [+] Launching Game Client...
:: Start the actual game UI silently
start "" javaw -cp build com.codebattle.GameClient

:: Close this launcher window immediately
exit
