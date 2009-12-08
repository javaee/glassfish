@echo off

REM
REM Copyright 1997-2008 Sun Microsystems, Inc.  All rights reserved.
REM Use is subject to license terms.
REM

java $WSIMPORT_OPTS -Djava.endorsed.dirs="%~dp0..\modules\endorsed" -cp "%~dp0..\modules\webservices-osgi.jar;%~dp0..\modules\jaxb-osgi.jar" com.sun.tools.ws.WsImport %*
