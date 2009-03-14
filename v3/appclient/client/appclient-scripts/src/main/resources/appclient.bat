@echo off
rem/*
rem * To change this template, choose Tools | Templates
rem * and open the template in the editor.
rem *
rem * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
rem *
rem * Copyright 1997-2009 Sun Microsystems, Inc. All rights reserved.
rem *
rem * The contents of this file are subject to the terms of either the GNU
rem * General Public License Version 2 only ("GPL") or the Common Development
rem * and Distribution License("CDDL") (collectively, the "License").  You
rem * may not use this file except in compliance with the License. You can obtain
rem * a copy of the License at https://glassfish.dev.java.net/public/CDDL+GPL.html
rem * or glassfish/bootstrap/legal/LICENSE.txt.  See the License for the specific
rem * language governing permissions and limitations under the License.
rem *
rem * When distributing the software, include this License Header Notice in each
rem * file and include the License file at glassfish/bootstrap/legal/LICENSE.txt.
rem * Sun designates this particular file as subject to the "Classpath" exception
rem * as provided by Sun in the GPL Version 2 section of the License file that
rem * accompanied this code.  If applicable, add the following below the License
rem * Header, with the fields enclosed by brackets [] replaced by your own
rem * identifying information: "Portions Copyrighted [year]
rem * [name of copyright owner]"
rem *
rem * Contributor(s):
rem *
rem * If you wish your version of this file to be governed by only the CDDL or
rem * only the GPL Version 2, indicate your decision by adding "[Contributor]
rem * elects to include this software in this distribution under the [CDDL or GPL
rem * Version 2] license."  If you don't indicate a single choice of license, a
rem * recipient has the option to distribute your version of this file under
rem * either the CDDL, the GPL Version 2 or to extend the choice of license to
rem * its licensees as provided above.  However, if you add GPL Version 2 code
rem * and therefore, elected the GPL Version 2 license, then the option applies
rem * only if the new code is made subject to such option by the copyright
rem * holder.
rem */
setlocal enableextensions enabledelayedexpansion

rem Create a java command based on the user's input.  The client to run can
rem be specified in several ways.
rem
rem -client x.jar
rem a/b/c.class (path to a .class file)
rem -client some-directory (rare, but possible)
rem -mainClass pkg.MyMain (with no preceding -client; uses normal classpath)
rem -jar x.jar (conventional java command syntax)
rem a.b.MyMain (conventional java command syntax)
rem
rem Further, the appclient command may contain other java command
rem options, such as property settings (-Dmy.color=blue), etc., which
rem are passed through to the generated java command line as long as they
rem precede the expression which determines the app client class to execute, just
rem as with VM options on a normal java command.
rem
rem If possible, the script launches the app client's main class directly,
rem without launching the app client command processor first.  This happens
rem if the user specifies a JAR (using -client or -jar) or a main class
rem (using -mainclass with no preceding -client, or just by specifying the
rem main class name).  In other cases, for example
rem if the user specifies a directory or a class file, the script launches
rem the app client command processor which will in turn launch the app client's
rem main class.  Note that in these later cases the appclient container will
rem display a user-provided splash screen as soon as possible but it cannot do
rem so using the fast feature that is built in to the java launcher.
rem
set AS_INSTALL=%~dp0..
set AS_INSTALL_MOD=%AS_INSTALL%\modules
set envFile="%AS_INSTALL%\config\asenv.bat"
if EXIST %envFile% %envFile%

rem Record the default ACC config file if possible
call :recordACCArg -configxml %AS_ACC_CONFIG%

set accJar="%AS_INSTALL_MOD%\gf-client.jar"

rem Avoid constructs like set a=%a%OtherStuff%.
rem This does not work because Windows (by default) substitutes variables when it parses the
rem statement, not when it executes it.  Instead use set a=!a!%OtherStuff% with
rem delayed expansion turned on.

rem mainClassIdent indicates whether the script has processed options and
rem arguments to identify which main class to execute.  It can be undefined.  It
rem can have the value "tentative" which means we have a candidate main class
rem from a -client option but it could be overridden by a later -client appearance.
rem And it can have the value "final."
rem
set jvmArgs=
set accArgs=
set appArgs=

rem The state variable "expecting" records which special keyword,
rem if any, was just processed and therefore what value we expect next.
set expecting=

rem The environment variable extraACCArgsAsAppArgs will cause the script to
rem place ACC arguments that appear once the main class has been specified
rem as client arguments rather than redefinitions of the ACC arguments.  For
rem example, without this setting turned on the command
rem
rem appclient -client x.jar -client y.jar
rem
rem will execute y.jar and ignore x.jar.  With the setting turned on the
rem ACC will launch x.jar and -client y.jar will be the first application
rem passed to the app client.
rem set extraACCArgsAsAppArgs=true

rem Process each command line element

set ACCArgType=ACC
set JVMArgType=JVM

call :processArgs %*

if NOT "%APPCPATH%"=="" (
    set accArgs=!accArgs!,appcpath="%APPCPATH%"
)

echo java -javaagent:%accJar%=mode=acscript%accArgs%,%accMainArgs% ^
    -Djava.system.class.loader=org.glassfish.appclient.client.acc.ACCClassLoader ^
    %jvmArgs% %jvmMainArgs% ^
    %appArgs%

goto :EOF

:processArgs
rem    for /f "tokens=1*" %%c in ("%*") do (
    for %%c in (%*) do (
        set x=%%c
rem        set remainingArgs=%%d

rem Assume this will be an arg to the app client until proven otherwise.
rem Possible types are APP, ACC, and JVM.
     set argType=APP

rem case !x! in
        set matched=
        if NOT DEFINED matched for %%b in (-classpath -cp -jar) do if x%%b==x!x! (
            set matched=true
            set expecting=!x!
            set expectingArgType=JVM
        )

rem The next case must refer to all valid ACC options that expect a value
        if NOT DEFINED matched for %%b in (-client -mainclass -name -xml -configxml -user -password -passwordfile -server) do if x%%b==x!x! (
            set matched=true
            set expecting=!x!
            set expectingArgType=ACC
        )

rem The next case must refer to all valid ACC options that expect no value
        if NOT DEFINED matched for %%b in (-textauth -noappinvoke) do if x%%b==x!x! (
            set matched=true
            call :recordACCArg !x!
            set expecting=
        )

rem if the argument starts with a - sign
        if NOT DEFINED matched if "!x:~0,1!"=="-" (
            set matched=true
            call :recordNonACCOption !x!
            set expecting=
        )

        if NOT DEFINED matched (
            set matched=true
            if DEFINED expecting (
                call :record!expectingArgType!Arg !expecting! !x!
            ) else (
                call :recordLoneArg !x!
            )
            set expecting=
        )

rem     esac
rem    echo At end of loop, c is %%c and d is %%d
rem    if NOT x!remainingArgs!==x call :processArgs !remainingArgs!
    )
goto :EOF
rem
rem
rem

:recordAPPArg
    set appArgs=!appArgs! %1
    goto :EOF

:recordClientArg
    if "%mainClassIdent%"=="final" goto :treatAsAppArg
rem if NOT DEFINED mainClassIdent OR x%mainClassIdent%==xtentative (
        dir/b/a:d %1 1>nul 2>nul
        if not ERRORLEVEL 1 (
            rem client is a dir
            set jvmMainArgs=-jar %accJar%
            set accMainArgs=client=dir=%1
        ) else (
            rem client is NOT a dir.  Record the client info as a JAR
            rem if not already recorded.

            rem client is NOT a dir
            set jvmMainArgs=-jar %1
            set accMainArgs=client=jar=%1
        )
        set mainClassIdent=tentative
rem ) else (
:treatAsAppArg
        call :recordAPPArg -client
        call :recordAPPArg %1
rem )
    goto :EOF

:recordACCArg
    if "%ACCArgType%"=="APP" (
        for %%a in (%*) do call :recordAPPArg %%a
    ) else (
        if x%1==x-client (
            call :recordClientArg %2
        ) else (
            set accArgs=!accArgs!,arg=%1
            if NOT x%2==x set accArgs=!accArgs!,arg="%2"
        )
    )
    goto :EOF

:recordMainClass
    set _tmp=%2
    if x%1==x-jar (
        set jvmMainArgs=-jar %2
        set accMainArgs=client=jar=%2
        set mainClassIdent=final
    ) else if x%1==x-client (
        set jvmMainArgs=-jar %2
        set accMainArgs=client=jar=%2
        set mainClassIdent=tentative
    ) else if x%_tmp:~-6%==x.class (
        set jvmMainArgs=-jar %accJar%
        set accMainArgs=client=classfile=%2
        set mainClassIdent=final
    ) else (
        set jvmMainArgs=%1
        set accMainArgs=client=class=%1
        set mainClassIdent=final
    )

rem Change the ACC and JVM arg types now that we have identified the main class.

    if DEFINED extraACCArgsAsAppArgs (
        set ACCArgType=APP
    )
    set JVMArgType=APP

    goto :EOF

:recordJVMArg
    if "%JVMArgType%"=="APP" (
        for %%a in (%*) do call :recordAPPArg %%a
    ) else (
        if x%1==x-jar (
            call :recordMainClass -jar %2
        ) else (
            for %%a in (%*) do set jvmArgs=!jvmArgs! %%a
        )
    )
    goto :EOF

:recordNonACCOption
    if NOT DEFINED mainClassIdent (
        call :recordJVMArg %1
    ) else (
        call :recordAPPArg %1
    )
    goto :EOF

:recordLoneArg
    if NOT DEFINED mainClassIdent (
        call :recordMainClass %1
    ) else (
        call :recordAPPArg %1
    )
    goto :EOF
