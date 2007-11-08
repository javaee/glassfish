#
# Copyright 2006 Sun Microsystems, Inc. All rights reserved.
# Use is subject to license terms.
#

# J2EE_CLASSPATH is appended to the classpath referenced by the EJB server.
# J2EE_CLASSPATH must include the location of the JDBC driver classes 
# (except for the Cloudscape driver shipped with this release).
# Each directory is delimited by a colon.

J2EE_CLASSPATH=${RI_ROOT}/lib
export J2EE_CLASSPATH

# JAVA_HOME refers to the directory where the Java(tm) 2 SDK
# Standard Edition software is installed.

if [ -z "$JAVA_HOME" ]
then
    JAVA_HOME=${JDK_HOME}
    export JAVA_HOME
fi
