@echo off

REM
REM Copyright 1997-2009 Sun Microsystems, Inc.  All rights reserved.
REM Use is subject to license terms.
REM

java -Dorg.osgi.framework.storage="%TMPDIR\verifier-cache\" -Dorg.jvnet.hk2.osgimain.autostartBundles=osgi-adapter.jar -cp "%~dp0..\modules\glassfish.jar;%~dp0..\modules\verifier.jar" com.sun.enterprise.tools.verifier.VerifierOSGiMain "%*"
