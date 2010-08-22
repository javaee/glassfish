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
export AS_LOGFILE="$S1AS_HOME/cli.log"

cd "$APS_HOME"
(jps |grep Main |cut -f1 -d" " | xargs kill -9  > /dev/null 2>&1) || true
cd "$APS_HOME/devtests/admin/cli"
time ant all
egrep 'FAILED *0' "$APS_HOME/count.txt" >/dev/null || exit 1
