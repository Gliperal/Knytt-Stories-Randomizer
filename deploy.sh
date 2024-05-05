#!/bin/bash
#KSRandomizer.jar
#	eclipse: File > Export...
#	Runnable JAR file
#	Extract required libraries into generated JAR
capture_regex="([[:digit:]]+\.[[:digit:]]+\.[[:digit:]]+)"
version_regex_1="VERSION_NUMBER.*=.*\"$capture_regex\""
version_regex_2="=+[[:space:]]+VERSION[[:space:]]+$capture_regex[[:space:]]+=+"
while read line; do
	if [[ $line =~ $version_regex_1 ]]
	then
		ver="${BASH_REMATCH[1]}"
		break
	fi
done < src/core/Main.java
while read line; do
	if [[ $line =~ $version_regex_2 ]]
	then
		ver2="${BASH_REMATCH[1]}"
		break
	fi
done < changelog.txt
if [ -z "$ver" ]; then
	echo "Could not find version number in Main.java"
	exit
fi
if [ -z "$ver2" ]; then
	echo "Could not find version number in changelog.txt"
	exit
fi
if ! [ "$ver" = "$ver2" ]; then
	echo "Version number mismatch! Found $ver in Main.java and $ver2 in changelog.txt"
	exit
fi
echo Detected version number: $ver
ksdir="Knytt Stories Randomizer $ver"
randodir="$ksdir/Randomizer $ver"
mkdir "$ksdir"
mkdir "$randodir"
mkdir "$randodir/resources"
cp ks\ files/* README.txt "$ksdir"
cp changelog.txt javacheck.bat KSRandomizer.bat KSRandomizer.jar LICENSE.txt restore.bat reroll.bat "$randodir"
cp resources/ObjectClasses.txt resources/Presets.txt "$randodir/resources"
zip -qr "KSRandomizer v$ver.zip" "$ksdir"
#rm -rf "$ksdir"
# or move to 3rd Party Tools for local play
#KSRuns/WebContent/resources/index.html
#KSRuns/WebContent/resources/KSRandomizer vA.B.C.zip
#FileZilla/PuTTY both of those up to KSRuns
#read -p "Enter destination: " dir

