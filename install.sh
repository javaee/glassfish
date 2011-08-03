#!/bin/bash

if [ "$SERVER_DIR" == "" ] ; then
    echo "You must define SERVER_DIR (e.g., export SERVER_DIR=~/src/servers/glassfish3)"
    exit -1
fi

if [ "$MODULE_DIR" == "" ] ; then
    export MODULE_DIR=$SERVER_DIR/glassfish/modules
fi

echo Stopping the server
gf_undeploy admin-console
gf_stop
echo Clearing the OSGi cache
rm -rf $SERVER_DIR/glassfish/domains/domain1/osgi-cache
rm -rf $SERVER_DIR/glassfish/domains/domain1/generated
echo Removing any existing demo plugins from $MODULE_DIR
rm $MODULE_DIR/plugin*.jar 2>/dev/null

echo Building....
mvn clean install 

if [ "$?" -ne 0 ] ; then
    echo "**** Error: build failed"
    exit -1
fi

echo Installing modules to $MODULE_DIR
cp plugin-system/target/plugin-system-*.jar $MODULE_DIR
for PLUGIN in plugins/* ; do
    PLUGIN=`basename $PLUGIN`
    if [ -d plugins/$PLUGIN -a "example1" != "$PLUGIN" ] ; then
        echo "     $PLUGIN..."
        cp plugins/$PLUGIN/target/*.jar $MODULE_DIR 2>/dev/null
    fi
done

echo Starting the server
$SERVER_DIR/bin/asadmin start-domain --debug=true

echo Deploying the application
read -p "Attach debugger if desired, then press Enter to deploy the web app"
gf_deploy webapp/target/admin-console/

echo Done.
