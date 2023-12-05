@echo off
java -jar KSRandomizer.jar restore
if errorlevel 1 (
	echo Press return to exit
	set /p "tmp="
)
