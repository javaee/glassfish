#
# DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
#
# Copyright (c) 2018 Oracle and/or its affiliates. All rights reserved.
#
# The contents of this file are subject to the terms of either the GNU
# General Public License Version 2 only ("GPL") or the Common Development
# and Distribution License("CDDL") (collectively, the "License").  You
# may not use this file except in compliance with the License.  You can
# obtain a copy of the License at
# https://oss.oracle.com/licenses/CDDL+GPL-1.1
# or LICENSE.txt.  See the License for the specific
# language governing permissions and limitations under the License.
#
# When distributing the software, include this License Header Notice in each
# file and include the License file at LICENSE.txt.
#
# GPL Classpath Exception:
# Oracle designates this particular file as subject to the "Classpath"
# exception as provided by Oracle in the GPL Version 2 section of the License
# file that accompanied this code.
#
# Modifications:
# If applicable, add the following below the License Header, with the fields
# enclosed by brackets [] replaced by your own identifying information:
# "Portions Copyright [year] [name of copyright owner]"
#
# Contributor(s):
# If you wish your version of this file to be governed by only the CDDL or
# only the GPL Version 2, indicate your decision by adding "[Contributor]
# elects to include this software in this distribution under the [CDDL or GPL
# Version 2] license."  If you don't indicate a single choice of license, a
# recipient has the option to distribute your version of this file under
# either the CDDL, the GPL Version 2 or to extend the choice of license to
# its licensees as provided above.  However, if you add GPL Version 2 code
# and therefore, elected the GPL Version 2 license, then the option applies
# only if the new code is made subject to such option by the copyright
# holder.
#

# !/bin/sh
# Last updated 27 January 2005, Lloyd Chambers
# Use this script to generate AMX SDK
#


# return the full path for any input directory name--partial, relative or full
# resulting value is returned in the variable 'FULLPATH_RESULT'
fullpath()
{
	if [ $# != 1 ]
	then
		echo "fullpath: exactly one argument required"
		exit 1
	fi
	
	DIR_IN=$1

	CWD=`pwd`
		if [ -d $DIR_IN ]
		then
			cd $DIR_IN
		    FULLPATH_RESULT=`pwd`
	    else
	    	echo "Can't cd to " $DIR_IN
	    	exit 1
	    fi
	
	cd $CWD
}


#determine directory of this script
fullpath `dirname $0`
SCRIPT_DIR=$FULLPATH_RESULT

# the classpath separator--if not set, choose a default
if [ -z "$CLASSPATH_SEPARATOR" ]
then
	# not defined--look for telltale sign of Windows--the ":\" in our path
	echo $SCRIPT_DIR | grep -e ":\\\\" -e ":/"
	if [ $? = 0 ]
	then
		CPS=";"
	else
		CPS=":"
	fi
else
	CPS=$CLASSPATH_SEPARATOR
fi


CPS=;
DEV_ROOT=$SCRIPT_DIR/../../../../../../..
fullpath `dirname $DEV_ROOT`
DEV_ROOT=$FULLPATH_RESULT

echo SCRIPT_DIR=$SCRIPT_DIR
echo DEV_ROOT=$DEV_ROOT

PUBLISH="$DEV_ROOT/publish/JDK1.4_DBG.OBJ"
echo PUBLISH=$PUBLISH

LIB="$PUBLISH/packager/lib"
echo LIB=$LIB

JMX_RI_JAR=$PUBLISH/jmx/lib/jmxri.jar
JMX_REMOTE_JAR=$PUBLISH/rjmx-ri/jmxremote.jar
JAVAX77_JAR=$PUBLISH/management-api/lib/management-api.jar

# the jar built by gmake in admin-core/mbeanapi
# contains extra com.sun.enterprise stuff
MBEANAPI_BUILD_JAR=$PUBLISH/admin-core/mbeanapi/lib/mbeanapi.jar
AMX_CLIENT_JAR_NAME=amx-client.jar

# name of the directory into which we'll build the SDK
SDK_DIR=amx-sdk

STD_JARS="$JMX_RI_JAR;$JMX_REMOTE_JAR;$JAVAX77_JAR"
JAR_CP="$STD_JARS;$LIB/appserv-rt.jar;$MBEANAPI_BUILD_JAR;$LIB/j2ee.jar"


SRC_CLIENT="$DEV_ROOT/admin-core/mbeanapi/src/java"
#SRC_IMPL="$DEV_ROOT/admin/mbeanapi-impl/src/java"
#SRC_TESTS="$DEV_ROOT/admin/mbeanapi-impl/tests"
SRC_OTHER=
SRC_SAMPLE="$DEV_ROOT/appserv-tests/devtests/ee/admin/mbeanapi/samples"
SRC="$SRC_CLIENT;$SRC_SAMPLE"
SRC="$SRC;$SRC_OTHER"

LINK_URL=http://java.sun.com/j2se/1.4.2/docs/api
SUB_PACKAGES=com.sun.appserv.management
SUB_PACKAGES="$SUB_PACKAGES"
# SUB_PACKAGES="$SUB_PACKAGES:com.sun.enterprise.management"
SAMPLES_PKG=samples.amx
SAMPLES_PKG_DIR=samples/amx

NO_QUALIFIER="-noqualifier java.lang:java.io:java.util:javax.management:javax.management.remote"
SHARED_OPTIONS="-breakiterator -sourcepath $SRC -link $LINK_URL -classpath $JAR_CP -source 1.4 $NO_QUALIFIER"


echo STD_JARS=$STD_JARS
echo JAR_CPS=$JAR_CP
echo SRC_CLIENT=$SRC_CLIENT
echo SRC_IMPL=$SRC_IMPL
echo SRC_SAMPLE=$SRC_SAMPLE
echo SRC=$SRC
echo LINK_URL=$LINK_URL
echo SUB_PACKAGES=$SUB_PACKAGES
echo SAMPLES_PKG=$SAMPLES_PKG
echo SAMPLES_PKG_DIR=$SAMPLES_PKG_DIR
echo ""
echo NO_QUALIFIER=$NO_QUALIFIER
echo SHARED_OPTIONS=$SHARED_OPTIONS

rm -rf $SDK_DIR ${SDK_DIR}.jar
mkdir $SDK_DIR

echo "\nCopying support jars..."
cp $JMX_RI_JAR $JMX_REMOTE_JAR $SDK_DIR
cp $JAVAX77_JAR $SDK_DIR/javax77.jar

echo "\nCopying samples scripts..." 
cp $SRC_SAMPLE/run-samples.bat $SRC_SAMPLE/run-samples.sh $SDK_DIR
cp $SRC_SAMPLE/SampleMain.properties $SDK_DIR


# Extract just com.sun.appserv.management from MBEANAPI_BUILD_JAR
echo "\nCreating amx-client.jar..."
jar xf $MBEANAPI_BUILD_JAR
# get rid of stuff we don't want
# make a new jar excluding com.sun.enterprise
rm -rf META-INF com/sun/enterprise com/sun/CVS
jar cf $AMX_CLIENT_JAR_NAME com/
mv $AMX_CLIENT_JAR_NAME $SDK_DIR
rm -rf com


# copy and compile sample code
echo "\nCopying and compiling sample code..."
mkdir -p $SDK_DIR/$SAMPLES_PKG_DIR
cp $SRC_SAMPLE/$SAMPLES_PKG_DIR/*.java $SDK_DIR/$SAMPLES_PKG_DIR

javac -classpath "${SDK_DIR}/${AMX_CLIENT_JAR_NAME};$STD_JARS" -sourcepath $SDK_DIR $SDK_DIR/samples/amx/*.java

# generate all amx javadoc without source-code links
echo "\nGenerating AMX javadoc..."
javadoc -d amx-javadoc -protected -subpackages $SUB_PACKAGES $SHARED_OPTIONS
mv amx-javadoc $SDK_DIR

# generate the sample javadoc, with source-code links
# Note that this must be done after the AMX javadoc, as we are linking to it
echo "\nGenerating javadoc for Sample code..."
javadoc -linksource -d samples-javadoc -private -subpackages $SAMPLES_PKG -linkoffline ../amx-javadoc ./$SDK_DIR/amx-javadoc  $SHARED_OPTIONS
mv samples-javadoc $SDK_DIR


echo "\nCreating " $SDK_DIR.jar
jar cf ${SDK_DIR}.jar $SDK_DIR

echo DONE










