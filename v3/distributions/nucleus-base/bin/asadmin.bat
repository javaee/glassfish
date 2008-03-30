@echo off

REM
REM Copyright 1997-2008 Sun Microsystems, Inc.  All rights reserved.
REM Use is subject to license terms.
REM

java -DGlassFish.Platform=%GlassFish_Platform% -jar "%~dp0..\modules\admin-cli-10.0-SNAPSHOT.jar" %*

