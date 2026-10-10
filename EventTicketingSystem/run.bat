@echo off
cd /d "%~dp0"
if not exist bin mkdir bin
dir /s /b src\*.java > sources.txt
javac -encoding UTF-8 -d bin -cp "lib/*" @sources.txt
del sources.txt
if errorlevel 1 (
  pause
  exit /b 1
)
java -cp "bin;lib/*" com.eventticketing.Main
