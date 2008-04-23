#!/bin/sh

echo "Extracting archive, please wait..."
tail +11l $0 > tmp.jar
$JAVA_HOME/bin/jar xvf tmp.jar 
rm tmp.jar
chmod ugo+x product-installer.sh
chmod ugo+x install/bin/engine-wrapper
sh product-installer.sh
exit $?
