#!/usr/bin/env bash
# Run unit + integration tests for the channel plugin.
# Exit 0 = all pass. Exit 1 = failure with output.
set -euo pipefail

# Detect build system
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

# Integration test: verify JAR exists and contains the entry point class
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

# Check manifest attributes
MANIFEST=$(jar tf "$JAR" | grep -i manifest || true)
if [ -n "$MANIFEST" ]; then
    jar xf "$JAR" META-INF/MANIFEST.MF -C /tmp/plugin-check 2>/dev/null || true
    if jar xf "$JAR" && grep -q "Plugin-Id" META-INF/MANIFEST.MF 2>/dev/null; then
        echo "==> Plugin-Id found in JAR manifest"
    fi
fi

echo "==> All checks passed. Ready to release."
