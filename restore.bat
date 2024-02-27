@echo off
call javacheck.bat
if errorlevel 1 exit /b
java -jar KSRandomizer.jar --restore --exit=none
echo Press return to exit
set /p "tmp="
