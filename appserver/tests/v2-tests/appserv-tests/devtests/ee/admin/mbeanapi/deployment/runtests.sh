#!/bin/sh
#
# This script assumes that the appserv-tests directory is a subdir of the main build-dir
#

s1as=/ee
java=${s1as}/jdk/bin/java

base=../../../../../..
cp1="${base}/publish/JDK1.4_DBG.OBJ/admin-core/mbeanapi/lib/mbeanapi.jar"
cp2="${base}/publish/JDK1.4_DBG.OBJ/jmx/lib/jmxri.jar"
cp3="${base}/publish/JDK1.4_DBG.OBJ/rjmx-ri/jmxremote.jar"
cp4="${base}/appserv-tests/devtests/ee/admin/mbeanapi/deployment/build"
cp=${cp1}:${cp2}:${cp3}:${cp4}

${java} -cp ${cp} -ea   com.sun.enterprise.admin.mbeanapi.deployment.DeploymentTestsAuto
