@echo off
java -jar KSRandomizer.jar
if errorlevel 1 (
	echo Press return to exit
	set /p "tmp="
)
