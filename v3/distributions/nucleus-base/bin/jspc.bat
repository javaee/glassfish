@echo off

REM
REM Copyright 1997-2008 Sun Microsystems, Inc.  All rights reserved.
REM Use is subject to license terms.
REM

java -cp "%~dp0..\modules\javax.javaee-@VERSION@.jar;%~dp0..\modules\ant-@VERSIO
N@.jar;%~dp0..\modules\@EL_IMPL@;%~dp0..\modules\@JSP_IMPL@" org.apache.jasper.JspC -classpath "%~dp0..\modules\web\@JSTL_IMPL@;%~dp0..\modules\web\@JSF_IMPL@ "%*"

