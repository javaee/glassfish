REM 
REM  The contents of this file are subject to the terms 
REM  of the Common Development and Distribution License 
REM  (the License).  You may not use this file except in
REM  compliance with the License.
REM  
REM  You can obtain a copy of the license at 
REM  https://glassfish.dev.java.net/public/CDDLv1.0.html or
REM  glassfish/bootstrap/legal/CDDLv1.0.txt.
REM  See the License for the specific language governing 
REM  permissions and limitations under the License.
REM  
REM  When distributing Covered Code, include this CDDL 
REM  Header Notice in each file and include the License file 
REM  at glassfish/bootstrap/legal/CDDLv1.0.txt.  
REM  If applicable, add the following below the CDDL Header, 
REM  with the fields enclosed by brackets [] replaced by
REM  you own identifying information: 
REM  "Portions Copyrighted [year] [name of copyright owner]"
REM  
REM  Copyright 2007 Sun Microsystems, Inc. All rights reserved.
REM 


@echo off

REM convience bat file to build with

set _CLASSPATH=%CLASSPATH%

if "%JAVA_HOME%" == "" goto nojavahome
set JAVACMD=%JAVA_HOME%\bin\java

:nojavahome


rem if %JAVACMD% == "" goto usage:

rem set JAVA_BINDIR=`dirname %JAVACMD%`

rem set JAVA_HOME=%JAVA_BINDIR%\..

goto classpath


:classpath


set CLASSPATH=%CLASSPATH%;%JAVA_HOME%\lib\tools.jar;%JAXR_HOME%\lib\ant.jar;%JAXR_HOME%\lib\jaxp.jar;%JAXR_HOME%\lib\parser.jar;%JAXR_HOME%\lib\mail.jar;%JAXR_HOME%\lib\junit.jar

goto next


:next

%JAVACMD% -classpath %CLASSPATH% org.apache.tools.ant.Main %1 %2 %3 %4 %5 %6 %7 %8 %9

goto finish

usage:
echo "Cannot find JAVA. Please set your PATH."
exit 1

:finish
:clean

rem clean up classpath after

set CLASSPATH=%_CLASSPATH%

set _CLASSPATH=


