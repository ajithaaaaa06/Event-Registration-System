#!/bin/bash
# Compile and run without VS Code (needs JDK 11+ on PATH).
cd "$(dirname "$0")"
mkdir -p bin
javac -encoding UTF-8 -d bin -cp "lib/*" $(find src -name "*.java") && java -cp "bin:lib/*" com.eventticketing.Main
