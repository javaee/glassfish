@echo off
set JAVA_HOME=%1
echo %JAVA_HOME%
"%JAVA_HOME%/bin/java" -classpath %APS_HOME%/lib/isql.jar;%S1AS_HOME%/pointbase/lib/pbclient.jar;%S1AS_HOME%/pointbase/lib/pbembedded.jar isql.ISQL jdbc:pointbase:server://localhost:%2/sqe-samples,new dbuser dbpassword com.pointbase.jdbc.jdbcUniversalDriver shutdownPbs.sql
