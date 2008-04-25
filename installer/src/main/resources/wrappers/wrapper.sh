#!/bin/sh

echo "Extracting archive, please wait..."
tail +12l $0 > tmp.jar
$JAVA_HOME/bin/jar xvf tmp.jar 
rm tmp.jar
chmod ugo+x product-installer.sh
chmod ugo+x install/bin/engine-wrapper
echo "InstallHome.directory.INSTALL_HOME=$HOME/glassfish-v3tp2" > install.properties
sh product-installer.sh
exit $?
