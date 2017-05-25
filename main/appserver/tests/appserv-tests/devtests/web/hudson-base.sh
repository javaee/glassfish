#
# DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
#
# Copyright (c) 2017 Oracle and/or its affiliates. All rights reserved.
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


(jps |grep Main |cut -f1 -d" " | xargs kill -9  > /dev/null 2>&1) || true

[ "$BASE_PORT" ] || BASE_PORT=40000
[ "$WEBTIER_ADMIN_PORT" ] || WEBTIER_ADMIN_PORT=$[ ${BASE_PORT} + 1 ]
[ "$WEBTIER_PORT" ] || WEBTIER_PORT=$[ ${BASE_PORT} + 2 ]
[ "$WEBTIER_SSL_PORT" ] || WEBTIER_SSL_PORT=$[ ${BASE_PORT} + 3 ]
[ "$WEBTIER_ALTERNATE_PORT" ] || WEBTIER_ALTERNATE_PORT=$[ ${BASE_PORT} + 4 ]
[ "$WEBTIER_ORB_PORT" ] || WEBTIER_ORB_PORT=$[ ${BASE_PORT} + 5 ]
[ "$WEBTIER_ORB_SSL_PORT" ] || WEBTIER_ORB_SSL_PORT=$[ ${BASE_PORT} + 6 ]
[ "$WEBTIER_ORB_SSL_MUTUALAUTH_PORT" ] || WEBTIER_ORB_SSL_MUTUALAUTH_PORT=$[ ${BASE_PORT} + 7 ]
[ "$WEBTIER_JMS_PORT" ] || WEBTIER_JMS_PORT=$[ ${BASE_PORT} + 8 ]
[ "$JMX_PORT" ] || JMX_PORT=$[ ${BASE_PORT} + 9 ]
[ "$APACHE_PORT" ] || APACHE_PORT=$[ ${BASE_PORT} + 11 ]
[ "$APACHE_SSL_PORT" ] || APACHE_SSL_PORT=$[ ${BASE_PORT} + 12 ]

build() {
     echo building now

   pushd glassfish
   export GFDIR=`pwd`
   mvn -Dmaven.test.skip=true install
   popd

   unzip -q -o glassfish/distributions/glassfish/target/glassfish.zip
}

if [ -z "${S1AS_HOME}" ]
then
   build
   export S1AS_HOME=$PWD/glassfish5/glassfish
   export APS_HOME=$PWD/appserv-tests
else 
   export APS_HOME="$(pwd )/../.."
   echo APS_HOME=$APS_HOME
fi

export AS_LOGFILE=$S1AS_HOME/cli.log 
#export AS_DEBUG=true 


cd $APS_HOME

configure() {
	rm -rf $S1AS_HOME/domains/domain1
   echo "AS_ADMIN_PASSWORD=" > temppwd
   $S1AS_HOME/bin/asadmin --user admin --passwordfile $APS_HOME/config/adminpassword.txt create-domain --adminport ${WEBTIER_ADMIN_PORT} --domainproperties jms.port=${WEBTIER_JMS_PORT}:domain.jmxPort=${JMX_PORT}:orb.listener.port=${WEBTIER_ORB_PORT}:http.ssl.port=${WEBTIER_SSL_PORT}:orb.ssl.port=${WEBTIER_ORB_SSL_PORT}:orb.mutualauth.port=${WEBTIER_ORB_SSL_MUTUALAUTH_PORT} --instanceport ${WEBTIER_PORT} domain1

   #Create 
   echo "admin.domain=domain1
   admin.domain.dir=\${env.S1AS_HOME}/domains
   admin.port=${WEBTIER_ADMIN_PORT}
   admin.user=admin
   admin.host=localhost
   http.port=${WEBTIER_PORT}
   https.port=${WEBTIER_SSL_PORT}
   http.host=localhost
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
   results.mailhost=localhost
   results.mailer=QLTestsForPEInstallOrDASInEEInstall@sun.com
   results.mailee=yourname@sun.com
   autodeploy.dir=\${env.S1AS_HOME}/domains/\${admin.domain}/autodeploy
   precompilejsp=true
   jvm.maxpermsize=192m
   appserver.instance.dir=\${admin.domain.dir}/\${admin.domain}" > config.properties
}

run() {
   pushd $APS_HOME/devtests/web
   ./exclude-jobs.sh

   ant all

   (cat web.output | grep FAIL | grep -v "Total FAIL") || true
   popd
   $S1AS_HOME/bin/asadmin stop-domain
   (jps |grep Main |cut -f1 -d" " | xargs kill -9  > /dev/null 2>&1) || true
}
