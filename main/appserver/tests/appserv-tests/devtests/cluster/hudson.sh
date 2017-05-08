#!/usr/bin/bash
#
# DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
#
# Copyright (c) 2017 Oracle and/or its affiliates. All rights reserved.
#
# The contents of this file are subject to the terms of either the GNU
# General Public License Version 2 only ("GPL") or the Common Development
# and Distribution License("CDDL") (collectively, the "License").  You
# may not use this file except in compliance with the License.  You can
# obtain a copy of the License at
# https://oss.oracle.com/licenses/CDDL+GPL-1.1
# or LICENSE.txt.  See the License for the specific
# language governing permissions and limitations under the License.
#
# When distributing the software, include this License Header Notice in each
# file and include the License file at LICENSE.txt.
#
# GPL Classpath Exception:
# Oracle designates this particular file as subject to the "Classpath"
# exception as provided by Oracle in the GPL Version 2 section of the License
# file that accompanied this code.
#
# Modifications:
# If applicable, add the following below the License Header, with the fields
# enclosed by brackets [] replaced by your own identifying information:
# "Portions Copyright [year] [name of copyright owner]"
#
# Contributor(s):
# If you wish your version of this file to be governed by only the CDDL or
# only the GPL Version 2, indicate your decision by adding "[Contributor]
# elects to include this software in this distribution under the [CDDL or GPL
# Version 2] license."  If you don't indicate a single choice of license, a
# recipient has the option to distribute your version of this file under
# either the CDDL, the GPL Version 2 or to extend the choice of license to
# its licensees as provided above.  However, if you add GPL Version 2 code
# and therefore, elected the GPL Version 2 license, then the option applies
# only if the new code is made subject to such option by the copyright
# holder.
#

export HUDSON=true
export ROOT=`pwd`
if [ -x "/usr/bin/cygpath" ]
then
  ROOT=`cygpath -d $ROOT`
  echo "Windows ROOT: $ROOT"
  export CYGWIN=nontsec
fi
if [ -z "$GLASSFISH_URL" ]
then
  GLASSFISH_URL=http://gf-hudson.us.oracle.com/hudson/job/gf-trunk-build-continuous/lastSuccessfulBuild/artifact/bundles/glassfish.zip
fi
echo GLASSFISH_URL=$GLASSFISH_URL
DEVTEST=appserv-tests/devtests/cluster
. $DEVTEST/test-bigcluster.sh .
cp $DEVTEST/hosted-nodes .

echo "Cleanup up from previous tests..."
#cmd_on_hosted_nodes "/usr/jdk/jdk1.6.0_21/bin/jps |egrep 'ASMain|admin-cli' |cut -f1 -d' ' | xargs -l10 kill -9; rm -rf /export/home/hudson/testnode"
cmd_on_hosted_nodes "/usr/jdk/latest/bin/jps |egrep 'ASMain|admin-cli' |cut -f1 -d' ' | xargs -l10 kill -9; rm -rf /export/home/hudson/testnode"
rm -rf glassfish5 apps
rm glassfish.zip*
ln -s appserv-tests/devtests/cluster/apps apps
#echo "Revision under test: " `cat $GFBUILDDIR/revision.txt`
echo "Installing GlassFish..."
#unzip -q $GFBUILDDIR/archive/bundles/glassfish.zip || exit 1
wget -q --no-proxy $GLASSFISH_URL
unzip -q glassfish.zip || exit 1
export S1AS_HOME="$ROOT/glassfish5/glassfish"
export AS_LOGFILE="$S1AS_HOME/cli.log"
asadmin start-domain || exit 1
asadmin install-node --installdir /export/home/hudson/testnode/glassfish5 `grep -v '^#' hosted-nodes | cut -d" " -f2` || exit 1
echo "Benchmark of commands: base case"
benchmark_commands || exit 1
create_hosted_nodes || exit 1
create_hosted_clusters 2 5 4 || exit 1
echo "Benchmark of commands: cluster created but not started"
benchmark_commands || exit 1
(time asadmin start-cluster ch1) 2> start-cluster.time || exit 1
cat start-cluster.time
echo "Benchmark of commands: cluster created and started"
benchmark_commands || exit 1
time asadmin stop-cluster ch1 || exit 1
echo "Benchmark of deployment: 5m app"
benchmark_deploy 5m || exit 1
delete_clusters || exit 1
delete_hosted_nodes || exit 1
asadmin stop-domain || exit 1

startclustertime=`cut -d" " -f2 start-cluster.time`
cat <<EOF >japex-report.xml
<testSuiteReport name="ClusterPerformance" xmlns="http://www.sun.com/japex/testSuiteReport"> 
  <resultUnit>ms</resultUnit> 
  <version>1.000</version> 
  <chartType>linechart</chartType> 
  <osName>SunOS</osName> 
  <dateTime>`date "+%d %b %Y/%T %Z"`</dateTime> 
  <resultAxis>normal</resultAxis> 
  <resultUnitX>ms</resultUnitX> 
  <resultAxisX>normal</resultAxisX> 
  <numberOfThreads>1</numberOfThreads> 
  <configFile>cluster-config.xml/</configFile> 
  <includeWarmupRun>false</includeWarmupRun> 
  <osArchitecture>x86</osArchitecture> 
  <runsPerDriver>1</runsPerDriver> 
  <driver name="ClusterDriver"> 
    <resultAritMean>$startclustertime</resultAritMean> 
    <description xmlns="">Cluster Start Time</description> 
    <testCase name="start-cluster-1"> 
      <resultValue>$startclustertime</resultValue> 
    </testCase> 
  </driver>
</testSuiteReport>
EOF
exit 0

