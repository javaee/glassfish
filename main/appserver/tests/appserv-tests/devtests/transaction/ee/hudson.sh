java -version
ant -version
echo `which ant`

rm -rf glassfish-v4-image
mkdir glassfish-v4-image
pushd glassfish-v4-image

export http_proxy=http://www-proxy.us.oracle.com:80

# download the latest GF 
# check if latest GF is provided by upstream job otherwise use default.
if [ -z "$GLASSFISH_URL" ]
then
  GLASSFISH_URL=http://gf-hudson.us.oracle.com/hudson/job/gf-trunk-build-continuous/lastSuccessfulBuild/artifact/bundles/glassfish.zip
fi
wget --no-proxy $GLASSFISH_URL
unzip -q glassfish.zip

# PUT IN GMS PATCH
#wget http://java.net/jira/secure/attachment/45919/shoal-gms-impl.jar
#mv shoal-gms-impl.jar $PWD/glassfish5/glassfish/modules
# PUT IN GMS PATCH

export S1AS_HOME=$PWD/glassfish5/glassfish
popd
export APS_HOME=$PWD/appserv-tests
#export AS_DEBUG=true 

rm -rf $S1AS_HOME/domains/domain1

cd $APS_HOME

echo "AS_ADMIN_PASSWORD=" > temppwd
cat $APS_HOME/temppwd
(jps |grep Main |cut -f1 -d" " | xargs kill -9  > /dev/null 2>&1) || true
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

cd $S1AS_HOME/domains/domain1/config/
sed "s/1527/${DB_PORT}/g" domain.xml > domain.xml.replaced
mv domain.xml.replaced domain.xml
grep PortNumber domain.xml

cd $APS_HOME/config
(rm derby.properties.replaced  > /dev/null 2>&1) || true
sed "s/1527/${DB_PORT}/g" derby.properties > derby.properties.replaced
rm derby.properties
sed "s/1528/${DB_PORT_2}/g" derby.properties.replaced > derby.properties
cat derby.properties

pushd $APS_HOME/devtests/transaction/ee

//ant -Dsave.logs=true -DenableShoalLogger=true all |tee log.txt
ant -Dsave.logs=true all |tee log.txt
antStatus=$?

ant dev-report

exit $antStatus
