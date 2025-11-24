#!/bin/bash
# TeleRTX Startup Script
# This script starts TeleRTX and suppresses harmless reflection warnings

# Find the JAR file
JAR_FILE=$(ls target/telertx-*-jar-with-dependencies.jar 2>/dev/null | head -1)

if [ -z "$JAR_FILE" ]; then
    echo "Error: TeleRTX JAR file not found in target/"
    echo "Please build the project first: mvn clean package"
    exit 1
fi

# Run with reflection warnings suppressed
java --add-opens java.base/java.lang=ALL-UNNAMED -jar "$JAR_FILE"
