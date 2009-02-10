@echo off
REM set up references to paths, jars, we'll need

echo Windows jcmd batch file

REM set up classpath
set USUAL_JAR=jcmd.jar
set _CLASSPATH=%USUAL_JAR%

REM main routine to invoke
set _MAIN=com.sun.cli.jcmd.JCmdMain
set _CLASSLOADER=com.sun.cli.jcmd.framework.FrameworkClassLoader


java -ea -cp %_CLASSPATH% -Djava.system.class.loader=%_CLASSLOADER% %_MAIN% boot meta %*%


