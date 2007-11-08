@echo off

REM
REM Copyright 1997-2007 Sun Microsystems, Inc.  All rights reserved.
REM Use is subject to license terms.
REM


REM   This batch file should be run under <S1AS_INSTALL_ROOT>\bin

setlocal
call ..\config\asenv.bat

set _JAVA=%AS_JAVA%\bin\java
set LIB=%AS_INSTALL%\lib
set IMQLIB=%AS_INSTALL%\imq\lib
set JHELP=%AS_JHELP%

set usage=Usage: %0 [/help] [/userdir dir]
set userdir=

if "%1" == "/help" goto prtusage
if "%1" == "-help" goto prtusage
if "%1" == "help" goto prtusage

if "%1" == "/userdir" goto userdir
if "%1" == "-userdir" goto userdir
goto exe

:userdir
if "%2" =="" goto prtusage

:exe
echo %_JAVA% -Dcom.sun.aas.installRoot="%AS_INSTALL%" -classpath %LIB%\appserv-assemblytool.jar;%IMQLIB%\activation.jar;%JHELP%\jhall.jar;%LIB%\appserv-admin.jar;%LIB%\appserv-cmp.jar;%LIB%\appserv-rt.jar;%LIB%\javaee.jar;%LIB%\appserv-ext.jar;%LIB%\deployhelp.jar com.sun.enterprise.tools.deployment.main.Main  %1 %2

%_JAVA% -Dcom.sun.aas.installRoot="%AS_INSTALL%" -classpath %LIB%\appserv-assemblytool.jar;%IMQLIB%\activation.jar;%JHELP%\jhall.jar;%LIB%\appserv-admin.jar;%LIB%\appserv-cmp.jar;%LIB%\appserv-rt.jar;%LIB%\javaee.jar;%LIB%\appserv-ext.jar;%LIB%\deployhelp.jar com.sun.enterprise.tools.deployment.main.Main %1 %2

goto end

:prtusage
echo %usage%

:end
set _JAVA=
set LIB=
set JHELP=
endlocal




