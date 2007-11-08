@echo off

REM
REM The contents of this file are subject to the terms 
REM of the Common Development and Distribution License 
REM (the License).  You may not use this file except in
REM compliance with the License.
REM 
REM You can obtain a copy of the license at 
REM https://glassfish.dev.java.net/public/CDDLv1.0.html or
REM glassfish/bootstrap/legal/CDDLv1.0.txt.
REM See the License for the specific language governing 
REM permissions and limitations under the License.
REM 
REM When distributing Covered Code, include this CDDL 
REM Header Notice in each file and include the License file 
REM at glassfish/bootstrap/legal/CDDLv1.0.txt.  
REM If applicable, add the following below the CDDL Header, 
REM with the fields enclosed by brackets [] replaced by
REM you own identifying information: 
REM "Portions Copyrighted [year] [name of copyright owner]"
REM 
REM Copyright 2006 Sun Microsystems, Inc. All rights reserved.
REM


REM
REM Copyright 2004-2005 Sun Microsystems, Inc.  All rights reserved.
REM Use is subject to license terms.
REM

REM set up references to paths, jars, we'll need
REM _JMXRI should point to the jar file for the JMX Reference Implementation version 1.2
REM _JMXREMOTE should point to the jar file for the JSR 160 jar file, including the TCP connector

set _BASEDIR=D:/s1as8/publish/JDK1.4_DBG.OBJ
set _JMXRI=%_BASEDIR%/jmx/lib/jmxri.jar
set _JMXREMOTE=%_BASEDIR%/jmx-remote/rjmx-impl/lib/rjmx-impl.jar

REM set up classpath
set _CLASSPATH=jmxadmin.jar\;%_JMXRI%\;%_JMXREMOTE%

REM main routine to invoke
set _MAIN=com.sun.cli.jmx.cmd.JMXAdminMain



java -ea -cp %_CLASSPATH% %_MAIN% %*%


