#!/bin/sh

# should be done already
# mvn clean; mvn install;  mvn assembly:assembly
echo ""
echo "===> YOU MUST HAVE ALREADY DONE 'mvn install; mvn assembly:assembly' <=="
echo ""

export CP=target/amx-tests-10.0-SNAPSHOT-jar-with-dependencies.jar
export MAIN=org.glassfish.admin.amxtest.TestMain
export PROPS=resources/amxtest.properties

java -cp $CP -ea $MAIN $PROPS 

