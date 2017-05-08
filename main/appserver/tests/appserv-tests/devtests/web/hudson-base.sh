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
