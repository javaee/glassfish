REM
REM The contents of this file are subject to the terms 
REM of the Common Development and Distribution License 
REM (the License).  You may not use this file except in
REM compliance with the License.
REM 
REM You can obtain a copy of the license at 
REM https://glassfish.dev.java.net/public/CDDLv1.0.html or
REM glassfish/bootstrap/legal/CDDLv1.0.txt.
REM See the License for the specific language governing 
REM permissions and limitations under the License.
REM 
REM When distributing Covered Code, include this CDDL 
REM Header Notice in each file and include the License file 
REM at glassfish/bootstrap/legal/CDDLv1.0.txt.  
REM If applicable, add the following below the CDDL Header, 
REM with the fields enclosed by brackets [] replaced by
REM you own identifying information: 
REM "Portions Copyrighted [year] [name of copyright owner]"
REM 
REM Copyright 2006 Sun Microsystems, Inc. All rights reserved.
REM

REM
REM Copyright 2004-2005 Sun Microsystems, Inc.  All rights reserved.
REM Use is subject to license terms.
REM

@echo off
REM convience bat file to build with

set MYCP=.\lib\ant.jar;.\lib\jaxp-api.jar;.\lib\dom.jar;.\lib\sax.jar;.\lib\xercesImpl.jar;.\lib\xalan.jar;%JAVA_HOME%\lib\tools.jar

if "%CLASSPATH%" == "" goto noclasspath

rem else
set _CLASSPATH=%CLASSPATH%
set CLASSPATH=%CLASSPATH%;%mycp%
goto next

:noclasspath
set _CLASSPATH=
set CLASSPATH=%mycp%
goto next

:next

%JAVA_HOME%\bin\java org.apache.tools.ant.Main -emacs %1 %2 %3 %4 %5 %6 %7 %8 %9

:clean

rem clean up classpath after
set CLASSPATH=%_CLASSPATH%
set _CLASSPATH=
