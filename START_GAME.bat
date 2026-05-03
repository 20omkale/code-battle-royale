@echo off
title CodeBattle Royale Command Center
color 0A

:menu
cls
echo ==========================================
echo       CODEBATTLE ROYALE - COMMAND CENTER
echo ==========================================
echo 1. Compile Source Code
echo 2. Start Game Server (Host)
echo 3. Start Game Client (Player)
echo 4. Quick Play (Compile + Start Server + 2 Clients)
echo 5. Exit
echo ==========================================
set /p choice="Enter your choice (1-5): "

if "%choice%"=="1" goto compile
if "%choice%"=="2" goto server
if "%choice%"=="3" goto client
if "%choice%"=="4" goto quickplay
if "%choice%"=="5" goto eof
goto menu

:compile
echo [*] Compiling Source Code...
javac -encoding UTF-8 src/com/codebattle/*.java
if %ERRORLEVEL% equ 0 (
    echo [+] Compilation Successful!
) else (
    echo [-] Compilation Failed!
)
pause
goto menu

:server
echo [*] Starting Server...
java -cp src com.codebattle.GameServer
pause
goto menu

:client
echo [*] Starting Client...
java -cp src com.codebattle.GameClient
goto menu

:quickplay
echo [*] Running Quick Play Setup...
javac -encoding UTF-8 src/com/codebattle/*.java
if %ERRORLEVEL% equ 0 (
    start "CodeBattle Server" java -cp src com.codebattle.GameServer
    timeout /t 2 /nobreak > nul
    start "CodeBattle Client 1" java -cp src com.codebattle.GameClient
    start "CodeBattle Client 2" java -cp src com.codebattle.GameClient
) else (
    echo [-] Compilation Failed. Cannot start game.
)
pause
goto menu
