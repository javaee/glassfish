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

#!/bin/sh

set -x

_CLASSPATH=$CLASSPATH

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

JAXR_HOME=..
JAXR_LIB=../lib

CLASSPATH=$JAXR_HOME/../lib/jaxb-api.jar:$JAXR_HOME/../lib/jaxb-impl.jar:$JAXR_LIB/FastInfoset.jar:$JAXR_LIB/jaxb-libs.jar:$JAXR_LIB/jaxb-xjc.jar:$JAXR_LIB/relaxngDatatype.jar:$JAXR_LIB/xsdlib.jar:$jaxP_HOME/xercesImpl.jar:$JAXR_LIB/jaxr-api.jar:$JAXR_LIB/lib/mail.jar:$JAXR_LIB/activation.jar:$JAXR_LIB/jsse.jar:$JAXR_LIB/jaas.jar:$JAXR_LIB/jaxr-impl.jar:$JAXR_LIB/saaj-api.jar:$JAXR_LIB/saaj-impl.jar:$JAXR_LIB/jaxp-api.jar:$JAXR_LIB/dom.jar:$JAXR_LIB/sax.jar:$JAXR_LIB/dom.jar:$JAXR_HOME/samples/jaxr-browser/jaxr-browser.jar

ARG=$1

case $ARG in
  "help" ) ARG="help_unix";;
esac 
     
$JAVACMD -classpath $CLASSPATH -Dorg.apache.commons.logging.log=org.apache.commons.logging.impl.SimpleLog -Dorg.apache.commons.logging.simplelog.defaultlog=warn RegistryBrowser $ARG $2 $3 $4

CLASSPATH=$_CLASSPATH
_CLASSPATH=



