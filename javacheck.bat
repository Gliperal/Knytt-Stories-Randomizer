@echo off
java -version > NUL 2>&1
if %errorlevel% NEQ 0 (
	echo You must install Java to run this program.
	set /p "tmp="
	exit /b 1
)
set JAVA_VERSION=0
for /f "tokens=1,2" %%i in ('java --version') do (
	if "%%i" == "java" set JAVA_VERSION=%%j
)
for /f "delims=." %%i in ("%JAVA_VERSION%") do (
	if %%i LSS 20 (
		echo Warning: This program may not work as intended on older versions of Java.
	)
)
