echo off
REM set up references to paths, jars, we'll need
REM _JMXRI should point to the jar file for the JMX Reference Implementation version 1.2
REM _JMXREMOTE should point to the jar file for the JSR 160 jar file, including the TCP connector
echo Windows jmxcmd batch file


set D=.
set JMXCMD_JAR=%D%\jmxcmd.jar
set OPTIONAL_JARS=%D%\jmxcmd-optional.jar
set USER_JARS=

REM optional jars
set JMXREMOTE_OPTIONAL=%D%\jmxremote_optional.jar
set JAVAX77_JAR=%D%\javax77.jar
set SUPPORT_JARS=%JMXREMOTE_OPTIONAL%;%JAVAX77_JAR%

set _CLASSPATH=%USER_JARS%;%JMXCMD_JAR%;%OPTIONAL_JARS%;%SUPPORT_JARS%

echo _CLASSPATH= %_CLASSPATH%

set _MAIN=com.sun.cli.jcmd.JCmdMain
java -ea -cp %_CLASSPATH% %_MAIN% boot --name=jmxcmd -c com.sun.cli.jmxcmd.JMXCmdCmdMgr %*%


