#!/bin/sh

tmp=`mktemp -d -t install.XXXX`
if [ $? -ne 0 ]; then
    echo "Unable to create temporary directory, exiting..."
    exit 1
fi
echo "Extracting archive, please wait..."
tail +19l $0 > $tmp/tmp.jar
cd $tmp
$JAVA_HOME/bin/jar xvf tmp.jar 
rm tmp.jar
chmod ugo+x product-installer.sh
chmod ugo+x install/bin/engine-wrapper
echo "InstallHome.directory.INSTALL_HOME=$HOME/glassfish-v3tp2" > install.properties
sh product-installer.sh
rm -rf ${tmp}/*
exit $?
