#!/bin/sh
#
# Gradle wrapper script
#

if [ -z "$JAVA_HOME" ]; then
    echo "Error: JAVA_HOME is not set."
    exit 1
fi

exec "$JAVA_HOME/bin/java" -cp "gradle/wrapper/gradle-wrapper.jar" org.gradle.wrapper.GradleWrapperMain "$@"
