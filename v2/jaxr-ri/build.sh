#
# The contents of this file are subject to the terms 
# of the Common Development and Distribution License 
# (the License).  You may not use this file except in
# compliance with the License.
# 
# You can obtain a copy of the license at 
# https://glassfish.dev.java.net/public/CDDLv1.0.html or
# glassfish/bootstrap/legal/CDDLv1.0.txt.
# See the License for the specific language governing 
# permissions and limitations under the License.
# 
# When distributing Covered Code, include this CDDL 
# Header Notice in each file and include the License file 
# at glassfish/bootstrap/legal/CDDLv1.0.txt.  
# If applicable, add the following below the CDDL Header, 
# with the fields enclosed by brackets [] replaced by
# you own identifying information: 
# "Portions Copyrighted [year] [name of copyright owner]"
# 
# Copyright 2007 Sun Microsystems, Inc. All rights reserved.
#
 

#!/usr/bin/bash

set -x

if [ -z "$JAVA_HOME" ]

then

JAVACMD=`which java`

if [ -z "$JAVACMD" ]

then

echo "Cannot find JAVA. Please set your PATH."

exit 1

fi

JAVA_BINDIR=`dirname $JAVACMD`

JAVA_HOME=$JAVA_BINDIR/..

fi

JAVACMD=$JAVA_HOME/bin/java
JAXR_HOME=.
SEP=":"
cp="$JAVA_HOME/lib/tools.jar$SEP$JAXR_HOME/misc/lib/ant.jar$SEP$JAXR_HOME/misc/lib/jaxp.jar$SEP$JAXR_HOME/misc/lib/parser.jar"

$JAVACMD -classpath $CLASSPATH$SEP$cp org.apache.tools.ant.Main "$@"

set +x
