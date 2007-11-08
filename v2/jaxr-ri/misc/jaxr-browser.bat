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

echo off

rem ----- Save Environment Variables That May Change ------------------------

set _CLASSPATH=%CLASSPATH%

rem ----- Verify Required Environment Variables ---------------------

if not "%JAVA_HOME%" == "" goto gotJavaHome
echo You must set JAVA_HOME to point at your Java Development Kit installation
goto cleanup
:gotJavaHome

rem Get remaining unshifted command line arguments and save them in the
set CMD_LINE_ARGS=
:setArgs
if ""%1""=="""" goto doneSetArgs
set CMD_LINE_ARGS=%CMD_LINE_ARGS% %1
shift
goto setArgs
:doneSetArgs

set JAXR_HOME=..
set JAXR_LIB=..\lib

set CLASSPATH=%JAXR_LIB%\jaxb-api.jar;%JAXR_LIB%\jaxb-impl.jar;%JAXR_LIB%\jaxb-libs.jar;%JAXR_LIB%\jaxb-xjc.jar;%JAXR_LIB%\relaxngDatatype.jar;%JAXR_LIB%\xsdlib.jar;%JAXR_LIB%\xercesImpl.jar;%JAXR_LIB%\jaxr-api.jar;%JAXR_LIB%\mail.jar;%JAXR_LIB\activation.jar;%JAXR_LIB%\jaxr-impl.jar;%JAXR_LIB%\FastInfoset.jar;%JAXR_LIB%\saaj-api.jar;%JAXR_LIB%\saaj-impl.jar;%JAXR_LIB%\jaxp-api.jar;%JAXR_LIB%\dom.jar;%JAXR_LIB%\sax.jar;%JAXR_HOME%\samples\jaxr-browser\jaxr-browser.jar

set ARG1=%1%

if "%1%"=="help" set ARG1="help_windows"

%JAVA_HOME%\bin\java -Dorg.apache.commons.logging.log=org.apache.commons.logging.impl.SimpleLog -Dorg.apache.commons.logging.simplelog.defaultlog=warn RegistryBrowser %ARG1% %2% %3% %4%

:cleanup
set CLASSPATH=%_CLASSPATH%
set _CLASSPATH=
:finish

