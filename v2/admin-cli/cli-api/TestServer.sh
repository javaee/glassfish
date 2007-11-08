# 
# Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
# Use is subject to license terms.
#

# set up references to paths, jars, we'll need
# _JMXRI should point to the jar file for the JMX Reference Implementation version 1.2
# _JMXREMOTE should point to the jar file for the JSR 160 jar file, including the TCP connector

#_BASEDIR=.
#_JMXRI=$_BASEDIR/jmx/lib/jmxri.jar
#_JMXREMOTE=$_BASEDIR/jmx-remote/rjmx-impl/lib/rjmx-impl.jar

_BASEDIR=.
_JMXRI=$_BASEDIR/jmxri.jar
_JMXREMOTE=$_BASEDIR/llc-jmxremoteb21.jar
_JMXTOOLS=$_BASEDIR/jmxtools.jar


# set up classpath
_CLASSPATH=TestServer.jar\:$_JMXRI\:$_JMXREMOTE:$_JMXTOOLS

# main routine to invoke
_MAIN=com.sun.cli.jmx.test.TestServer



java -server -ea -cp $_CLASSPATH $_MAIN $*



