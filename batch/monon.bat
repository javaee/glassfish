@echo off
setlocal
set level=HIGH
if NOT "%1Z"=="Z" set level=%1
echo ***************   BEFORE *****************
call asadmin get server.monitoring-service.module-monitoring-levels
call asadmin enable-monitoring --modules deployment=%level%:connector-connection-pool=%level%:connector-service=%level%:ejb-container=%level%:http-service=%level%:jdbc-connection-pool=%level%:jms-service=%level%:jvm=%level%:orb=%level%:thread-pool=%level%:transaction-service=%level%:web-container=%level%:jersey=%level%:jpa=HIGH=%level%:security=%level%:web-services-container=%level% 
echo ***************   AFTER  *****************
call asadmin get server.monitoring-service.module-monitoring-levels
endlocal
goto :EOF



REM OLD STUFF

call asadmin set configs.config.server-config.monitoring-service.module-monitoring-levels.connector-connection-pool=HIGH
call asadmin set configs.config.server-config.monitoring-service.module-monitoring-levels.connector-service=HIGH
call asadmin set configs.config.server-config.monitoring-service.module-monitoring-levels.ejb-container=HIGH
call asadmin set configs.config.server-config.monitoring-service.module-monitoring-levels.http-service=HIGH
call asadmin set configs.config.server-config.monitoring-service.module-monitoring-levels.jdbc-connection-pool=HIGH
call asadmin set configs.config.server-config.monitoring-service.module-monitoring-levels.jms-service=HIGH
call asadmin set configs.config.server-config.monitoring-service.module-monitoring-levels.jvm=HIGH
call asadmin set configs.config.server-config.monitoring-service.module-monitoring-levels.orb=HIGH
call asadmin set configs.config.server-config.monitoring-service.module-monitoring-levels.thread-pool=HIGH
call asadmin set configs.config.server-config.monitoring-service.module-monitoring-levels.transaction-service=HIGH
call asadmin set configs.config.server-config.monitoring-service.module-monitoring-levels.web-container=HIGH 
call asadmin set configs.config.server-config.monitoring-service.module-monitoring-levels.jersey=HIGH
call asadmin set configs.config.server-config.monitoring-service.module-monitoring-levels.jpa=HIGH
call asadmin set configs.config.server-config.monitoring-service.module-monitoring-levels.security=HIGH
call asadmin set configs.config.server-config.monitoring-service.module-monitoring-levels.web-services-container=HIGH

call asadmin set configs.config.c1-config.monitoring-service.module-monitoring-levels.connector-connection-pool=HIGH
call asadmin set configs.config.c1-config.monitoring-service.module-monitoring-levels.connector-service=HIGH
call asadmin set configs.config.c1-config.monitoring-service.module-monitoring-levels.ejb-container=HIGH
call asadmin set configs.config.c1-config.monitoring-service.module-monitoring-levels.http-service=HIGH
call asadmin set configs.config.c1-config.monitoring-service.module-monitoring-levels.jdbc-connection-pool=HIGH
call asadmin set configs.config.c1-config.monitoring-service.module-monitoring-levels.jms-service=HIGH
call asadmin set configs.config.c1-config.monitoring-service.module-monitoring-levels.jvm=HIGH
call asadmin set configs.config.c1-config.monitoring-service.module-monitoring-levels.orb=HIGH
call asadmin set configs.config.c1-config.monitoring-service.module-monitoring-levels.thread-pool=HIGH
call asadmin set configs.config.c1-config.monitoring-service.module-monitoring-levels.transaction-service=HIGH
call asadmin set configs.config.c1-config.monitoring-service.module-monitoring-levels.web-container=HIGH 
call asadmin set configs.config.c1-config.monitoring-service.module-monitoring-levels.jersey=HIGH
call asadmin set configs.config.c1-config.monitoring-service.module-monitoring-levels.jpa=HIGH
call asadmin set configs.config.c1-config.monitoring-service.module-monitoring-levels.security=HIGH
call asadmin set configs.config.c1-config.monitoring-service.module-monitoring-levels.web-services-container=HIGH


call asadmin set configs.config.i1-config.monitoring-service.module-monitoring-levels.connector-connection-pool=HIGH
call asadmin set configs.config.i1-config.monitoring-service.module-monitoring-levels.connector-service=HIGH
call asadmin set configs.config.i1-config.monitoring-service.module-monitoring-levels.ejb-container=HIGH
call asadmin set configs.config.i1-config.monitoring-service.module-monitoring-levels.http-service=HIGH
call asadmin set configs.config.i1-config.monitoring-service.module-monitoring-levels.jdbc-connection-pool=HIGH
call asadmin set configs.config.i1-config.monitoring-service.module-monitoring-levels.jms-service=HIGH
call asadmin set configs.config.i1-config.monitoring-service.module-monitoring-levels.jvm=HIGH
call asadmin set configs.config.i1-config.monitoring-service.module-monitoring-levels.orb=HIGH
call asadmin set configs.config.i1-config.monitoring-service.module-monitoring-levels.thread-pool=HIGH
call asadmin set configs.config.i1-config.monitoring-service.module-monitoring-levels.transaction-service=HIGH
call asadmin set configs.config.i1-config.monitoring-service.module-monitoring-levels.web-container=HIGH 
call asadmin set configs.config.11-config.monitoring-service.module-monitoring-levels.jersey=HIGH
call asadmin set configs.config.11-config.monitoring-service.module-monitoring-levels.jpa=HIGH
call asadmin set configs.config.11-config.monitoring-service.module-monitoring-levels.security=HIGH
call asadmin set configs.config.11-config.monitoring-service.module-monitoring-levels.web-services-container=HIGH



