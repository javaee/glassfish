if [ -n "$verbose" ]
then
    set -x
fi
export HUDSON=true
export ROOT=`pwd`

REHudson=gf-hudson.us.oracle.com

# The first command-line argument is the (optional) predecessor job from which
# to get the revision under test and the glassfish.zip file to expand.
# Default: gf-trunk-build-continuous

if [ -x "/usr/bin/cygpath" ]
then
  ROOT=`cygpath -d $ROOT`
  echo "Windows ROOT: $ROOT"
  export CYGWIN=nontsec
fi
upstreamSource=gf-trunk-build-continuous
if [ $1 ] 
then
  upstreamSource=$1
fi
rm -rf glassfishv3
rm revision-under-test.html
wget --no-proxy -q -O revision-under-test.html http://${REHudson}/hudson/job/${upstreamSource}/lastSuccessfulBuild
grep 'Build #' revision-under-test.html
time wget --no-proxy -q -O glassfish.zip http://${REHudson}/hudson/job/${upstreamSource}/lastSuccessfulBuild/artifact/bundles/glassfish.zip
rm -fR glassfish5
unzip -q glassfish.zip
if [ $? -ne 0 ]
then
  exit 1
fi
export S1AS_HOME="$ROOT/glassfish5/glassfish"
export APS_HOME="$ROOT/appserv-tests"
export ANT_OPTS= "Xms128m -Xmx256m"
cd "$APS_HOME"
(jps -l |grep ASMain |cut -f1 -d" " | xargs -t kill -9  > /dev/null 2>&1) || true
#
cd "$APS_HOME/devtests/deployment"

antTarget="all-ee"
if [ -z "$DEPL_TARGET" ]
then
    $S1AS_HOME/bin/asadmin start-domain
    antTarget="all"
fi
# Get rid of any lingering password file from an earlier run
rm ~/.asadminpass

time ant $antTarget
antStatus=$?

if [ -z "$DEPL_TARGET" ]
then
    $S1AS_HOME/bin/asadmin stop-domain
fi
#
echo DEPL_TARGET is $DEPL_TARGET
if [ $antStatus -ne 0 ]
then
    ps -ef 
    exit $antStatus
fi
egrep '\[FAILED|UNKNOWN\]' client.log >> /dev/null
#no match -> 1 for the status value
if [ $? -eq 1 ]
then
  exit 0
else
  exit 1
fi
