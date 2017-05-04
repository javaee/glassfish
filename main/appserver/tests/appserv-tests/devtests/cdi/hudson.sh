date

java -version
ant -version

rm -rf glassfish-v3-image
mkdir glassfish-v3-image
pushd glassfish-v3-image

export http_proxy=http://www-proxy.us.oracle.com:80

# download the latest GF 
#wget http://rator.sfbay/maven/repositories/glassfish//org/glassfish/distributions/glassfish/3.0-SNAPSHOT/glassfish-3.0-SNAPSHOT.zip
#wget http://javaweb.sfbay/java/re/glassfish/v3/promoted/trunk-latest/archive/bundles/glassfish-ri.zip
#wget -O web.zip http://hudson.sfbay/job/glassfish-v3-devbuild/lastSuccessfulBuild/artifact/v3/distributions/web/target/web.zip
#wget http://hudson.sfbay/job/gfv3-build-test/lastSuccessfulBuild/artifact/v3/distributions-ips/glassfish/target/glassfish.zip

wget --no-proxy http://gf-hudson.us.oracle.com/hudson/job/gf-trunk-build-continuous/lastSuccessfulBuild/artifact/bundles/glassfish.zip

#wget http://hudson.glassfish.org/job/gf-trunk-build-continuous/lastSuccessfulBuild/artifact/bundles/glassfish.zip

date

unzip -q glassfish.zip

date

export S1AS_HOME=$PWD/glassfish5/glassfish
popd
export APS_HOME=$PWD/appserv-tests
export AS_LOGFILE=$S1AS_HOME/cli.log 

cd $APS_HOME

(jps |grep Main |cut -f1 -d" " | xargs kill -9  > /dev/null 2>&1) || true

pushd $APS_HOME/devtests/cdi
rm count.txt || true

rm -rf bin .classpath  .project 

ant clean || true;

rm -f RepRunConf.txt; rm -f *.output

rm -f alltests.res
rm -f $APS_HOME/test_results*
#clean all RepRunConf.txt
find . -name "RepRunConf.txt" | xargs rm -f

# start GlassFish
$S1AS_HOME/bin/asadmin start-domain domain1
# start Derby
$S1AS_HOME/bin/asadmin start-database

ant all

# start GlassFish
$S1AS_HOME/bin/asadmin stop-domain domain1
# start Derby
$S1AS_HOME/bin/asadmin stop-database

touch $APS_HOME/test_resultsValid.xml


