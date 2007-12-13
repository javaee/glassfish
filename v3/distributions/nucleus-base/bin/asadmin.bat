@echo off

setlocal
set AS_INSTALL=%~dp0\..

java -Dcom.sun.aas.installRoot=%AS_INSTALL% -cp "%AS_INSTALL%\lib\admin-cli-10.0-SNAPSHOT.jar";"%AS_INSTALL%\lib\cli-framework-10.0-SNAPSHOT.jar";"%AS_INSTALL%\lib\glassfish-10.0-SNAPSHOT.jar" com.sun.enterprise.admin.cli.Main %*
endlocal