@echo off

REM
REM Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
REM Use is subject to license terms.
REM

setlocal
set DIRNAME=%~dp0
set AS_INSTALL=%DIRNAME%\..
set AS_INSTALL_LIB=%AS_INSTALL%\lib
set AS_INSTALL_MOD=%AS_INSTALL%\modules
set CONFIG_HOME=%AS_INSTALL%\config

call "%CONFIG_HOME%\asenv.bat"

set AS_JAVA=%JAVA_HOME%

"%AS_JAVA%\bin\java" -Dcom.sun.aas.domainRoot="%DIRNAME%\%AS_DEF_DOMAINS_PATH%"  -Dcom.sun.aas.java.home="%AS_JAVA%" -jar "%AS_INSTALL_MOD%/upgrade-tool.jar" %*
endlocal
