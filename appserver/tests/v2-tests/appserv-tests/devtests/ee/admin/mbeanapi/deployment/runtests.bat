@echo off
setlocal
set base=../../../../../..
set cp1=%base%/publish/JDK1.4_DBG.OBJ/admin-core/mbeanapi/lib/mbeanapi.jar
set cp2=%base%/publish/JDK1.4_DBG.OBJ/jmx/lib/jmxri.jar
set cp3=%base%/publish/JDK1.4_DBG.OBJ/rjmx-ri/jmxremote.jar
set cp4=%base%/appserv-tests/devtests/ee/admin/mbeanapi/deployment/build
set cp=%cp1%;%cp2%;%cp3%;%cp4%

java -cp %cp% -ea   com.sun.enterprise.admin.mbeanapi.deployment.DeploymentTestsAuto


