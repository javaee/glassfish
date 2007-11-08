#!/bin/sh

$JAVA_HOME/bin/java -classpath ./lib/isql.jar:$S1AS_HOME/pointbase//lib/pbclient.jar:$S1AS_HOME/pointbase/pbembedded.jar isql.ISQL jdbc:pointbase:server://localhost:$@/sqe-samples,new dbuser dbpassword com.pointbase.jdbc.jdbcUniversalDriver shutdownPbs.sql
