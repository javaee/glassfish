if [ -n "$verbose" ]
then
    set -x
fi
export HUDSON=true
export ROOT=`pwd`

REHudson=gf-hudson.us.oracle.com

if [ -x "/usr/bin/cygpath" ]
then
  ROOT=`cygpath -d $ROOT`
  echo "Windows ROOT: $ROOT"
  export CYGWIN=nontsec
fi
rm -rf glassfishv3
rm revision-under-test.html
wget -q -O revision-under-test.html http://${REHudson}/hudson/job/gf-trunk-build-continuous/lastSuccessfulBuild
grep 'Build #' revision-under-test.html
time wget -q -O glassfish.zip http://${REHudson}/hudson/job/gf-trunk-build-continuous/lastSuccessfulBuild/artifact/bundles/glassfish.zip
rm -fR glassfish3
unzip -q glassfish.zip
if [ $? -ne 0 ]
then
  exit 1
fi
export S1AS_HOME="$ROOT/glassfish3/glassfish"
export APS_HOME="$ROOT/appserv-tests"
cd "$APS_HOME"
(jps |grep Main |cut -f1 -d" " | xargs kill -9  > /dev/null 2>&1) || true
#
# Get rid of any previously-running GlassFish instance
#
echo "Cleaning any left-over ASMain Java processes"
for pid in `jps -l | grep "com.sun.enterprise.glassfish.bootstrap.ASMain" | cut -d " "  -f 1 ` ; do echo "PID is $pid"; done
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
