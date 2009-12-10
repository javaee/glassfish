@echo off

REM
REM Copyright 1997-2008 Sun Microsystems, Inc.  All rights reserved.
REM Use is subject to license terms.
REM

java -cp "%~dp0..\modules\jaxb.jar;%~dp0..\modules\webservices.jar" com.sun.tools.ws.WsImport "%*"
