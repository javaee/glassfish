@echo off
REM Copyright 1997-2008 Sun Microsystems, Inc.  All rights reserved.
REM Use is subject to license terms.
REM Always use JDK 1.6 or higher
REM Depends on Java from ..\config\asenv.bat
setlocal
call "%~dp0..\config\asenv.bat" 
if "%AS_JAVA%x" == "x" goto UsePath
set JAVA="%AS_JAVA%\bin\java"
goto run
:UsePath
set JAVA=java
:run
%JAVA% -jar "%~dp0..\modules\admin-cli.jar" %*
