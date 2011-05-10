@echo off
setlocal
set level=HIGH
set where=server
if NOT "%1Z"=="Z" set level=%1
if NOT "%2Z"=="Z" set where=%2
echo ***************   BEFORE *****************
call asadmin get %where%.monitoring-service.module-monitoring-levels
call asadmin enable-monitoring --target %where% --modules deployment=%level%:connector-connection-pool=%level%:connector-service=%level%:ejb-container=%level%:http-service=%level%:jdbc-connection-pool=%level%:jms-service=%level%:jvm=%level%:orb=%level%:thread-pool=%level%:transaction-service=%level%:web-container=%level%:jersey=%level%:jpa=HIGH=%level%:security=%level%:web-services-container=%level% 
echo ***************   AFTER  *****************
call asadmin get %where%.monitoring-service.module-monitoring-levels
endlocal
goto :EOF

