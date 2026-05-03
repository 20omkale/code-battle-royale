@echo off
title CodeBattle Royale - Command Center
:menu
cls
echo ============================================
echo   CODEBATTLE ROYALE - COMMAND CENTER
echo ============================================
echo.
echo   [1] Compile Project
echo   [2] Start Server
echo   [3] Start Client
echo   [4] Start Server + 2 Clients (Quick Play)
echo   [5] Exit
echo.
set /p choice="Enter choice: "

if "%choice%"=="1" goto compile
if "%choice%"=="2" goto server
if "%choice%"=="3" goto client
if "%choice%"=="4" goto quickplay
if "%choice%"=="5" exit
goto menu

:compile
echo Compiling...
javac -encoding UTF-8 -d build src/com/codebattle/GameMessage.java src/com/codebattle/GameServer.java src/com/codebattle/GameClient.java
if %ERRORLEVEL% equ 0 (echo BUILD SUCCESS!) else (echo BUILD FAILED!)
pause
goto menu

:server
echo Starting Server...
start "CodeBattle Server" java -cp build com.codebattle.GameServer
goto menu

:client
echo Starting Client...
start "CodeBattle Client" java -cp build com.codebattle.GameClient
goto menu

:quickplay
echo Compiling...
javac -encoding UTF-8 -d build src/com/codebattle/GameMessage.java src/com/codebattle/GameServer.java src/com/codebattle/GameClient.java
echo Starting Server...
start "CodeBattle Server" java -cp build com.codebattle.GameServer
timeout /t 2 >nul
echo Starting Client 1...
start "Client 1" java -cp build com.codebattle.GameClient
timeout /t 1 >nul
echo Starting Client 2...
start "Client 2" java -cp build com.codebattle.GameClient
echo.
echo All launched! Join from both clients and press Start Game.
pause
goto menu
