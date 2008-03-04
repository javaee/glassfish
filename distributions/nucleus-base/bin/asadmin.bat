@echo off

setlocal
set AS_INSTALL=%~dp0\..
set AS_INSTALL_LIB=%AS_INSTALL%\modules

java -Dcom.sun.aas.installRoot=%AS_INSTALL% -cp "%AS_INSTALL_LIB%\admin-cli-10.0-SNAPSHOT.jar";"%AS_INSTALL_LIB%\cli-framework-10.0-SNAPSHOT.jar";"%AS_INSTALL_LIB%\glassfish-10.0-SNAPSHOT.jar" com.sun.enterprise.admin.cli.Main %*
endlocal
