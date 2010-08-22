if [ -n "$verbose" ]
then
    set -x
fi
export HUDSON=true
export ROOT=`pwd`
if [ -x "/usr/bin/cygpath" ]
then
  ROOT=`cygpath -d $ROOT`
  echo "Windows ROOT: $ROOT"
  export CYGWIN=nontsec
fi
rm -rf glassfishv3
wget -q -O revision-under-test.html http://gf-hudson.sfbay.sun.com/hudson/job/gf-trunk-build-continuous/lastSuccessfulBuild
grep 'Build #' revision-under-test.html
time wget -q -O glassfish.zip http://gf-hudson.sfbay.sun.com/hudson/job/gf-trunk-build-continuous/lastSuccessfulBuild/artifact/bundles/glassfish.zip
unzip -q glassfish.zip
export S1AS_HOME="$ROOT/glassfishv3/glassfish"
export APS_HOME="$ROOT/appserv-tests"
cd "$APS_HOME"
(jps |grep Main |cut -f1 -d" " | xargs kill -9  > /dev/null 2>&1) || true
cd "$APS_HOME/devtests/deployment"

if [ -z "$DEPL_TARGET"]
then
    $S1AS_HOME/bin/asadmin start-domain
fi

time ant all

if [ -z "$DEPL_TARGET"]
then
    $S1AS_HOME/bin/asadmin stop-domain
fi
errors=`egrep -c '\[FAILED|UNKNOWN\]' appserv-tests/devtests/deployment/client.log`
if [ 0 -ne $errors ] 
then
  exit 1
fi
