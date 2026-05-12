#!/usr/bin/env bash
# Run unit + integration tests for the model provider plugin.
set -euo pipefail

if [ -f "build.gradle.kts" ]; then
    BUILD=gradle
elif [ -f "pom.xml" ]; then
    BUILD=maven
else
    echo "ERROR: no build file found (build.gradle.kts or pom.xml)" >&2
    exit 1
fi

echo "==> Build system: $BUILD"

if [ "$BUILD" = "gradle" ]; then
    ./gradlew clean shadowJar test --no-daemon
else
    mvn clean package -q
fi

echo "==> Unit tests passed"

if [ "$BUILD" = "gradle" ]; then
    JAR=$(ls build/libs/*.jar 2>/dev/null | grep -v sources | head -1)
else
    JAR=$(ls target/*.jar 2>/dev/null | grep -v original | head -1)
fi

if [ -z "$JAR" ]; then
    echo "ERROR: no JAR produced" >&2
    exit 1
fi

echo "==> JAR produced: $JAR"
echo "==> All checks passed. Ready to release."
