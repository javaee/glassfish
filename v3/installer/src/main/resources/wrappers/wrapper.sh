#!/bin/sh

tmp=`mktemp -d -t install.XXXX`
if [ $? -ne 0 ]; then
    echo "Unable to create temporary directory, exiting..."
    exit 1
fi
echo "Extracting archive, please wait..."
tail +24l $0 > $tmp/tmp.jar
cd $tmp
$JAVA_HOME/bin/jar xvf tmp.jar 
rm tmp.jar
chmod ugo+x product-installer.sh
chmod ugo+x install/bin/engine-wrapper
echo "InstallHome.directory.INSTALL_HOME=$HOME/glassfish-v3tp2" > install.properties
if [ $# -eq 0 ]
then
sh product-installer.sh
else
sh product-installer.sh $*
fi
rm -rf ${tmp}/*
exit $?
