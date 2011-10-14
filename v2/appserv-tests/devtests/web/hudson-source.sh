#! /bin/bash 

. hudson-base.sh

rm -rf $S1AS_HOME/domains/domain1

cd $APS_HOME

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

pushd $APS_HOME/devtests/web
./exclude-jobs.sh

echo ant all

(cat web.output | grep FAIL | grep -v "Total FAIL") || true
popd
$S1AS_HOME/bin/asadmin stop-domain
(jps |grep Main |cut -f1 -d" " | xargs kill -9  > /dev/null 2>&1) || true

