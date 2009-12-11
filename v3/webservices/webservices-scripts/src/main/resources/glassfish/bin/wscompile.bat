@echo off

REM
REM Copyright 1997-2008 Sun Microsystems, Inc.  All rights reserved.
REM Use is subject to license terms.
REM

java %WSCOMPILE_OPTS% -Djava.endorsed.dirs="%~dp0..\modules\endorsed" -cp "%~dp0..\modules\webservices-osgi.jar;%~dp0..\modules\jaxb-osgi.jar;%~dp0..\modules\mail.jar;%JAVA_HOME%/lib/tools.jar" com.sun.xml.rpc.tools.wscompile.Main %*
