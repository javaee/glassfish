#
# Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
# Use is subject to license terms.
#

#!/bin/sh

AS_HOST=localhost
AS_PORT=port
AS_ADMINUSER=admin
AS_ADMINPASSWORD=adminadmin
AS_ADMINPORT=4848

$JAVA_HOME/bin/java -classpath ./target/classes:$S1AS_HOME/lib/appserv-deployment-client.jar:$S1AS_HOME/lib/jmxremote_optional.jar:. org.glassfish.deployment.util.JMXDeploy $1 $AS_HOST $2 $AS_ADMINUSER $AS_ADMINPASSWORD $AS_ADMINPORT



