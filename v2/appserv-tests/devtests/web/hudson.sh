#!/bin/bash
#
# DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
#
# Copyright (c) 2011 Oracle and/or its affiliates. All rights reserved.
#
# The contents of this file are subject to the terms of either the GNU
# General Public License Version 2 only ("GPL") or the Common Development
# and Distribution License("CDDL") (collectively, the "License").  You
# may not use this file except in compliance with the License.  You can
# obtain a copy of the License at
# https://glassfish.dev.java.net/public/CDDL+GPL_1_1.html
# or packager/legal/LICENSE.txt.  See the License for the specific
# language governing permissions and limitations under the License.
#
# When distributing the software, include this License Header Notice in each
# file and include the License file at packager/legal/LICENSE.txt.
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

# 
# Usage: appserv-tests/devtests/web/hudson.sh [<url for download glassfish> <job name for skip file>]
#
# Hudson setup:
#
# Source Code Management: Subversion Modules
# Repository URL: https://svn.java.net/svn/glassfish~svn/trunk/v2/appserv-tests/devtests/web
# Local module directory: appserv-tests/devtests/web
# Repository URL: https://svn.java.net/svn/glassfish~svn/trunk/v2/appserv-tests/lib
# Local module directory: appserv-tests/lib
# Repository URL: https://svn.java.net/svn/glassfish~svn/trunk/v2/appserv-tests/config
# Local module directory: appserv-tests/config
# Repository URL: https://svn.java.net/svn/glassfish~svn/trunk/v2/appserv-tests/util
# Local module directory: appserv-tests/util
#
# Build after other projects are build: mirror-glassfish-repository
#
# The following TCP ports are assigned by Hudson to avoid collision
#   WEBTIER_ADMIN_PORT 
#   WEBTIER_JMS_PORT
#   WEBTIER_JMX_PORT 
#   WEBTIER_ORB_PORT
#   WEBTIER_SSL_PORT
#   WEBTIER_PORT
#   WEBTIER_ALTERNATE_PORT
#   WEBTIER_ORB_SSL_PORT
#   WEBTIER_ORB_SSL_MUTUALAUTH_PORT
#   WEBTIER_INSTANCE_PORT
#   WEBTIER_INSTANCE_PORT_2
#   WEBTIER_INSTANCE_PORT_3
#   WEBTIER_INSTANCE_HTTPS_PORT
#
# Record finderprints of files to track usage: glassfish-v3-image/glassfish.zip
#     Fingerprint all archived artifacts
#
# Archive the artifacts: appserv-tests/test_results*.*,glassfish-v3-image/glassfish4/glassfish/domains/domain1/logs/*
#
# Publish SQE test result report
#     SQE report XMLs: appserv-tests/test_resultsValid.xml
#
# E-mail Notification
#     Recipients: <....>@oracle.com
#     Send e-mail for every unstable build

kill_processes() {
    uname=`uname | awk '{print $1}'`
    case "$uname" in
        CYGWIN*) KILL="taskkill /F /T /PID";;
        *) KILL="kill -9";;
    esac

    (ps -aef | grep java | grep ASMain | grep -v grep | awk '{print $2}' | xargs $KILL > /dev/null 2>&1) || true
}

GLASSFISH_DOWNLOAD_URL=${1:-"http://gf-hudson.us.oracle.com/hudson/job/gf-trunk-build-continuous/lastSuccessfulBuild/artifact/bundles/glassfish.zip"}
SKIP_NAME=$2

java -version

rm -rf glassfish-v3-image
mkdir glassfish-v3-image
pushd glassfish-v3-image

# download the latest GF 
wget -O glassfish.zip $GLASSFISH_DOWNLOAD_URL

unzip -q glassfish.zip

export S1AS_HOME=$PWD/glassfish4/glassfish
popd
export APS_HOME=$PWD/appserv-tests
export AS_LOGFILE=$S1AS_HOME/cli.log 
#export AS_DEBUG=true 

#Copy over the modified run.xml for dumping thread stack
#cp ../../run.xml $PWD/appserv-tests/config

rm -rf $S1AS_HOME/domains/domain1

cd $APS_HOME

echo "AS_ADMIN_PASSWORD=" > temppwd
$S1AS_HOME/bin/asadmin --user admin --passwordfile $APS_HOME/config/adminpassword.txt create-domain --adminport ${WEBTIER_ADMIN_PORT} --domainproperties jms.port=${WEBTIER_JMS_PORT}:domain.jmxPort=${WEBTIER_JMX_PORT}:orb.listener.port=${WEBTIER_ORB_PORT}:http.ssl.port=${WEBTIER_SSL_PORT}:orb.ssl.port=${WEBTIER_ORB_SSL_PORT}:orb.mutualauth.port=${WEBTIER_ORB_SSL_MUTUALAUTH_PORT} --instanceport ${WEBTIER_PORT} domain1

if [ `uname`="Linux" ]; then
    HOST="localhost.localdomain"
else
    HOST="localhost"
fi

#Create 
echo "admin.domain=domain1
admin.domain.dir=\${env.S1AS_HOME}/domains
admin.port=${WEBTIER_ADMIN_PORT}
admin.user=admin
admin.host=$HOST
http.port=${WEBTIER_PORT}
https.port=${WEBTIER_SSL_PORT}
http.host=$HOST
http.address=127.0.0.1
http.alternate.port=${WEBTIER_ALTERNATE_PORT}
orb.port=${WEBTIER_ORB_PORT}
admin.password=
ssl.password=changeit
master.password=changeit
admin.password.file=\${env.APS_HOME}/config/adminpassword.txt
appserver.instance.name=server
config.dottedname.prefix=server
resources.dottedname.prefix=domain.resources
results.mailhost=$HOST
results.mailer=QLTestsForPEInstallOrDASInEEInstall@sun.com
results.mailee=yourname@sun.com
autodeploy.dir=\${env.S1AS_HOME}/domains/\${admin.domain}/autodeploy
precompilejsp=true
jvm.maxpermsize=192m
ENABLE_REPLICATION=false
appserver.instance.dir=\${admin.domain.dir}/\${admin.domain}
cluster.name=clusterA
instance.name=inst1
instance.name.2=inst2
instance.name.3=inst3
instance.http.port=${WEBTIER_INSTANCE_PORT}
instance.https.port=${WEBTIER_INSTANCE_HTTPS_PORT}
instance.http.port.2=${WEBTIER_INSTANCE_PORT_2}
instance.http.port.3=${WEBTIER_INSTANCE_PORT_3}
nodeagent.name=localhost-domain1
" > config.properties

kill_processes

pushd $APS_HOME/devtests/web
./exclude-jobs.sh $SKIP_NAME

ant all

#check out fresh build.xml 
rm $APS_HOME/devtests/web/build.xml

kill_processes
(cat web.output | grep FAIL | grep -v "Total FAIL") || true
#popd
#cd $S1AS_HOME/bin/
#./asadmin stop-domain
#kill_processes
