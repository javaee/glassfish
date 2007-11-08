# 
# Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
# Use is subject to license terms.
#

# set up references to paths, jars, we'll need
# _JMXRI should point to the jar file for the JMX Reference Implementation version 1.2
# _JMXREMOTE should point to the jar file for the JSR 160 jar file, including the TCP connector


_BASEDIR=.
_JMXRI=$_BASEDIR/jmxri.jar
_JMXREMOTE=$_BASEDIR/rjmx-impl.jar
_JMXTOOLS=$_BASEDIR/rjmx-impl.jar


# set up classpath
_CLASSPATH=jmxadmin.jar\:$_JMXRI\:$_JMXREMOTE:$_JMXTOOLS

# main routine to invoke
_MAIN=com.sun.cli.jmx.cmd.JMXAdminMain



java -ea -cp $_CLASSPATH $_MAIN $*


