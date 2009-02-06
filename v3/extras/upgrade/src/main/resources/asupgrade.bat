@echo off

REM
REM Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
REM Use is subject to license terms.
REM

setlocal
AS_INSTALL=%~dp0..\
AS_INSTALL_LIB=%AS_INSTALL%/modules
CONFIG_HOME=%AS_INSTALL%/config
call "%CONFIG_HOME%\asenv.bat"
set Path=%AS_INSTALL%\bin;%PATH%
"%AS_JAVA%\bin\java" -Dcom.sun.aas.utool.LogLevel="INFO" -Dcom.sun.aas.installRoot="%AS_INSTALL%" -Dcom.sun.aas.domainRoot="%AS_DEF_DOMAINS_PATH%" -Dcom.sun.aas.instanceRoot="%AS_INSTALL%" -Dcom.sun.aas.configRoot="%AS_CONFIG%" -Dcom.sun.aas.java.home="%AS_JAVA%" -cp "%AS_INSTALL_LIB%/admin-cli.jar";"%AS_INSTALL_LIB%/admin-cli.jar";"%AS_INSTALL_LIB%/kernel.jar";"%AS_INSTALL_LIB%/upgrade.jar"  com.sun.enterprise.tools.upgrade.UpgradeToolMain %*
endlocal