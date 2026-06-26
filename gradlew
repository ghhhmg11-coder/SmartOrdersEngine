#!/usr/bin/env sh
APP_HOME=$(cd "$(dirname "$0")" && pwd)
CLASSPATH="$APP_HOME/gradle/wrapper/gradle-wrapper.jar"
if [ -z "$JAVA_HOME" ]; then
  JAVACMD=java
else
  JAVACMD="$JAVA_HOME/bin/java"
fi
exec "$JAVACMD" -classpath "$CLASSPATH" org.gradle.wrapper.GradleWrapperMain "$@"
