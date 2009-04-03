@echo off

REM
REM Copyright 1997-2009 Sun Microsystems, Inc. All rights reserved.
REM Use is subject to license terms.
REM

setlocal

set AS_INSTALL=%~dp0..
set AS_INSTALL_LIB=%AS_INSTALL%\modules

java  -Xms24m -Xmx96m  -cp "%AS_INSTALL_LIB%\common-util.jar;%AS_INSTALL_LIB%\cmp-utility.jar;%AS_INSTALL_LIB%\cmp-support-ejb.jar;%AS_INSTALL_LIB%\cmp-ejb-mapping.jar;%AS_INSTALL_LIB%\dbschema-repackaged.jar;%CLASSPATH%" com.sun.jdo.spi.persistence.support.ejb.util.CaptureSchemaWrapper %*
