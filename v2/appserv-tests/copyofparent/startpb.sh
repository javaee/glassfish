#!/bin/sh

# need $APS_HOME/lib in classpath to pickup Java stored procedure class

$JAVA_HOME/bin/java -classpath $S1AS_HOME/pointbase/lib/pbembedded.jar:$APS_HOME/lib com.pointbase.net.netServer /port:$@ /pointbase.ini=./build/pointbase.ini /noconsole
