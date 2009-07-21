@echo off

REM
REM Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
REM Use is subject to license terms.
REM

setlocal
DIRNAME=%~dp0
AS_INSTALL=%DIRNAME%\..\
AS_INSTALL_LIB=%AS_INSTALL%/lib
AS_INSTALL_MOD=%AS_INSTALL%/modules
CONFIG_HOME=%AS_INSTALL%/config
call "%CONFIG_HOME%\asenv.bat"

REM override relative path from asenv.bat
AS_INSTALL=%DIRNAME%\..\

set Path=%AS_INSTALL%\bin;%PATH%

REM note: $AS_DEF_DOMAINS_PATH is relative to config dir
"%AS_JAVA%\bin\java" -Dcom.sun.aas.utool.LogLevel="INFO" -Dcom.sun.aas.domainRoot="%DIRNAME%\%AS_DEF_DOMAINS_PATH%"  -Dcom.sun.aas.java.home="%AS_JAVA%" -jar "%AS_INSTALL_MOD%/upgrade-tool.jar" %*
endlocal
