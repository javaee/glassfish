#!/bin/sh

echo "S1AS_HOME is => " $S1AS_HOME
echo " APS_HOME is =? " $APS_HOME

$S1AS_HOME/lib/ant/bin/ant run 
cd $APS_HOME
gmake stopPB 
gmake startPB 
cd - 
#$S1AS_HOME/bin/asadmin set domain.resources.jdbc-connection-pool.jdbc-pointbase-pool.is_connection_validation_required=true 
$S1AS_HOME/lib/ant/bin/ant run
