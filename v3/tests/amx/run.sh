#!/bin/sh

export CP=target/amx-tests-10.0-SNAPSHOT-jar-with-dependencies.jar
export MAIN=org.glassfish.admin.amxtest.TestMain
export PROPS=resources/amxtest.properties

java -cp $CP -ea $MAIN $PROPS 

