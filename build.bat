@echo off
echo Compiling CodeBattle Royale...
javac -encoding UTF-8 -d build src/com/codebattle/*.java
if %ERRORLEVEL% equ 0 (
    echo Build Successful!
) else (
    echo Build Failed!
)
