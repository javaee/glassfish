rm -rf  v3
mkdir v3
pushd v3
wget -O glassfish.zip http://hudson.glassfish.org/job/gf-trunk-build-continuous/lastSuccessfulBuild/artifact/bundles/glassfish.zip
unzip -q glassfish.zip
export S1AS_HOME=$PWD/glassfishv3/glassfish
popd
export APS_HOME=$PWD/appserv-tests
export AS_LOGFILE=$S1AS_HOME/cli.log 
#export AS_DEBUG=true 

#Copy over the modified run.xml for dumping thread stack
#cp ../../run.xml $PWD/appserv-tests/config

rm -rf $S1AS_HOME/domains/domain1

cd $APS_HOME

echo "AS_ADMIN_PASSWORD=" > temppwd
$S1AS_HOME/bin/asadmin --user anonymous --passwordfile $APS_HOME/temppwd create-domain --adminport ${ADMIN_PORT} --domainproperties jms.port=${JMS_PORT}:domain.jmxPort=${JMX_PORT}:orb.listener.port=${ORB_PORT}:http.ssl.port=${SSL_PORT}:orb.ssl.port=${ORB_SSL_PORT}:orb.mutualauth.port=${ORB_SSL_MUTUALAUTH_PORT} --instanceport ${INSTANCE_PORT} domain1

#Create 
echo "admin.domain=domain1
admin.domain.dir=\${env.S1AS_HOME}/domains
admin.port=${ADMIN_PORT}
admin.user=anonymous
admin.host=localhost
http.port=${INSTANCE_PORT}
https.port=${SSL_PORT}
http.host=localhost
http.address=127.0.0.1
http.alternate.port=${ALTERNATE_PORT}
orb.port=${ORB_PORT}
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

(jps |grep Main |cut -f1 -d" " | xargs kill -9  > /dev/null 2>&1) || true

pushd $APS_HOME/devtests/admin/cli

ant all report-result |tee log.txt
# ant all |tee log.txt

(cat log.txt | grep  Total |cut -f2 -d']' |sort |uniq -c |grep -v PAS) || true
popd
cd $S1AS_HOME/bin/
#./asadmin stop-domain

#(jps |grep ASMain |cut -f1 -d" " | xargs kill -9  > /dev/null 2>&1) || true
