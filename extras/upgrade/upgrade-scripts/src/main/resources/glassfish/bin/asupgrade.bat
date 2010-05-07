@echo off

REM
REM Copyright (c) 1997, 2010 Oracle and/or its affiliates, Inc. All rights reserved.
REM Use is subject to license terms.
REM

set AS_INSTALL=%~dp0..
set AS_INSTALL_LIB=%AS_INSTALL%\lib
set AS_INSTALL_MOD=%AS_INSTALL%\modules
set CONFIG_HOME=%AS_INSTALL%\config

call "%CONFIG_HOME%\asenv.bat"

java -Dcom.sun.aas.domainRoot="%~dp0%AS_DEF_DOMAINS_PATH%" -jar "%AS_INSTALL_MOD%/upgrade-tool.jar" %*
