export HUDSON=true
export ROOT=`pwd`

if [ -x "/usr/bin/cygpath" ]
then
  ROOT=`cygpath -d $ROOT`
  echo "Windows ROOT: $ROOT"
  export CYGWIN=nontsec
fi

if [ -x ${LAST_SUCC_BUILD_URL} ]
then
  export LAST_SUCC_BUILD_URL=http://gf-hudson.us.oracle.com/hudson/job/gf-trunk-build-continuous/lastSuccessfulBuild
fi
if [ -x ${LAST_SUCC_BUNDLE} ]
then
  export LAST_SUCC_BUNDLE=artifact/bundles/glassfish.zip
fi

rm -rf glassfish5
wget -q -O revision-under-test.html ${LAST_SUCC_BUILD_URL}
grep 'Build #' revision-under-test.html
time wget -q -O glassfish.zip ${LAST_SUCC_BUILD_URL}/${LAST_SUCC_BUNDLE}
unzip -q glassfish.zip
export S1AS_HOME="$ROOT/glassfish5/glassfish"
export APS_HOME="$ROOT/appserv-tests"
export AS_LOGFILE="$S1AS_HOME/cli.log"

cd "$APS_HOME"
(jps |grep ASMain |cut -f1 -d" " | xargs kill -9  > /dev/null 2>&1) || true
cd "$APS_HOME/devtests/admin/cli"
ant clean
time ant -Dnum_tests=45 all 
egrep 'FAILED *0' "$APS_HOME/count.txt" >/dev/null
