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
# run the tests twice -- e.g. Issue # 12127
ant clean
time ant all
# don't run twice if we already failed!
egrep 'FAILED *0' "$APS_HOME/count.txt" >/dev/null || exit 1

# bnevins June 9, 2010
# if the tests can not be run twice  then nobody will use them because it will screw up their installation
# This will happen when we don't clean-up and return to the original state -- which is a good test in itself!
# Also we really don't care how long these tests take
# of course it is not an issue on Hudson.
(jps |grep Main |cut -f1 -d" " | xargs kill -9  > /dev/null 2>&1) || true
ant clean
time ant -Dnum_tests=45 all 
egrep 'FAILED *0' "$APS_HOME/count.txt" >/dev/null
