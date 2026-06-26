#!/usr/bin/env sh
PRG="$0"
while [ -h "$PRG" ]; do ls=`ls -ld "$PRG"`; link=`expr "$ls" : '.*-> \(.*\)'`; if expr "$link" : '/.*' > /dev/null; then PRG="$link"; else PRG=`dirname "$PRG"`"/$link"; fi; done
APP_HOME=`cd "`dirname "$PRG"`" && pwd`
CLASSPATH=$APP_HOME/gradle/wrapper/gradle-wrapper.jar
exec "${JAVA_HOME}/bin/java" -classpath "$CLASSPATH" org.gradle.wrapper.GradleWrapperMain "$@"
