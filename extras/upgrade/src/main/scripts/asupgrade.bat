@echo off

REM
REM Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
REM Use is subject to license terms.
REM

setlocal
AS_INSTALL=%~dp0..\
AS_INSTALL_LIB=%AS_INSTALL%/lib
AS_INSTALL_MOD=%AS_INSTALL%/modules
CONFIG_HOME=%AS_INSTALL%/config
call "%CONFIG_HOME%\asenv.bat"

REM override relative path from asenv.bat
AS_INSTALL=%~dp0..\

set Path=%AS_INSTALL%\bin;%PATH%

REM note: $AS_DEF_DOMAINS_PATH is relative to config dir
"%AS_JAVA%\bin\java" -Dcom.sun.aas.utool.LogLevel="INFO" -Dcom.sun.aas.installRoot="%AS_INSTALL%" -Dcom.sun.aas.domainRoot="%AS_INSTALL%\config\%AS_DEF_DOMAINS_PATH%" -Dcom.sun.aas.instanceRoot="%AS_INSTALL%" -Dcom.sun.aas.configRoot="%AS_CONFIG%" -Dcom.sun.aas.java.home="%AS_JAVA%" -cp "%AS_INSTALL_MOD%/admin-cli.jar";"%AS_INSTALL_MOD%/admin-cli.jar";"%AS_INSTALL_MOD%/kernel.jar";"%AS_INSTALL_MOD%/upgrade.jar";"%AS_INSTALL_LIB%/javahelp-2.0.02.jar"  com.sun.enterprise.tools.upgrade.UpgradeToolMain %*
endlocal