@echo off
REM
REM Copyright 1997-2008 Sun Microsystems, Inc.  All rights reserved.
REM Use is subject to license terms.
REM

set AS_INSTALL_LIB=%~dp0..\modules
set ANT_LIB=%AS_INSTALL_LIB%\ant-@VERSION@.jar
set SERVLET_API=%AS_INSTALL_LIB%\@SERVLET_API@
set JSP_API=%AS_INSTALL_LIB%\@JSP_API@
set JSP_IMPL=%AS_INSTALL_LIB%\@JSP_IMPL@
set EL_IMPL=%AS_INSTALL_LIB%\@EL_IMPL@
set JSTL_API=%AS_INSTALL_LIB%\@JSTL_API@
set JSTL_IMPL=%AS_INSTALL_LIB%\@JSTL_IMPL@
set JSF_API=%AS_INSTALL_LIB%\@JSF_API@
set JSF_IMPL=%AS_INSTALL_LIB%\@JSF_IMPL@

java -cp "%SERVLET_API%;%JSP_API%;%JSTL_API%;%JSF_API%;%ANT_LIB%;%EL_IMPL%;%JSP_IMPL%" org.apache.jasper.JspC -classpath "%JSTL_IMPL%;%JSF_IMPL%" %*
