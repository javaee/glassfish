@echo off

REM
REM Copyright (c) 1997, 2010 Oracle and/or its affiliates, Inc. All rights reserved.
REM Use is subject to license terms.
REM

java -Dcom.sun.aas.domainRoot="%~dp0..\domains" -jar "%~dp0..\modules\upgrade-tool.jar" %*
