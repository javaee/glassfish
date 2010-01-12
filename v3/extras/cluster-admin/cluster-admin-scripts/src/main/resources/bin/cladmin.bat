@echo off
REM Copyright 2010 Sun Microsystems, Inc.  All rights reserved.
REM Use is subject to license terms.
REM
REM Always use JDK 1.6 or higher
REM Depends on Java from ..\glassfish\config\asenv.bat

VERIFY OTHER 2>nul
setlocal ENABLEEXTENSIONS ENABLEDELAYEDEXPANSION
if ERRORLEVEL 0 goto ok
echo "Unable to enable extensions"
exit /B 1
:ok

set BIN_DIR=%~dp0
set INSTALL_DIR=%BIN_DIR%..\glassfish
set LIB_DIR=%INSTALL_DIR%\lib
call "%INSTALL_DIR%\config\asenv.bat"
if "%AS_JAVA%x" == "x" goto UsePath
set JAVA="%AS_JAVA%\bin\java"
goto run
:UsePath
set JAVA=java
:run


REM Get the list of hosts from input parameter --target
for /F "tokens=* delims= " %%G in (
    '"%JAVA% -cp %LIB_DIR%\cladmin.jar com.sun.enterprise.deployment.util.ProcessHostsOption %*"'
) do set hostportlist=%%G

REM Get the host list from environment variable AS_TARGET
if "%hostportlist%x" == "x" (
    if "%AS_TARGET%x" neq "x" (
        for /F "tokens=* delims= " %%G in (
            '"%JAVA% -cp %LIB_DIR%\cladmin.jar com.sun.enterprise.deployment.util.GetHostPort %AS_TARGET%"'
        ) do set hostportlist=%%G
    )
)


REM Not a cladmin command. Delegate to asadmin
if "%hostportlist%x" == "x" (
    %BIN_DIR%\asadmin %*
    goto end    
) 


REM Filter out --target, host and port arguments
for /F "tokens=* delims= " %%K in (
    '"%JAVA% -cp %LIB_DIR%\cladmin.jar com.sun.enterprise.deployment.util.OptionsFilter %*"'
) do set arglist=%%K

REM Run asadmin against each of the host
for %%O in (%hostportlist%) do (
    for /F "tokens=* delims= " %%I in (
        '"%JAVA% -cp %LIB_DIR%\cladmin.jar com.sun.enterprise.deployment.util.GetHost %%O"'
    ) do set host=%%I

    for /F "tokens=* delims= " %%I in (
        '"%JAVA% -cp %LIB_DIR%\cladmin.jar com.sun.enterprise.deployment.util.GetPort %%O"'
    ) do set port=%%I

    set arguments=!port! !arglist!
    set arguments=--port !arguments!
    set arguments=!host! !arguments!
    set arguments=--host !arguments!

	
    echo.    
    echo.^>^> Executing command for instance !host!
    call %BIN_DIR%\asadmin !arguments!

    if !ERRORLEVEL! == 0 (
         if NOT "!successlist!x" == "x" (
             set successlist=!host!, !successlist!
         ) else ( set successlist=!host! )
    ) else (
        if NOT "!failurelist!x" == "x" (
            set failurelist=!host!, !failurelist!
        ) else ( set failurelist=!host! )
    )
)


REM Display summary message
echo.
REM Display success message, if any.
if NOT "!successlist!x" == "x" (
    echo.
    echo.^>^> Command executed successfully for the following instances:
    echo.!successlist!
)

REM Display failure message, if any.
if NOT "!failurelist!x" == "x" (
    echo.
    echo.^>^> Command failed for the following instances:
    echo.!failurelist!
)


:end
