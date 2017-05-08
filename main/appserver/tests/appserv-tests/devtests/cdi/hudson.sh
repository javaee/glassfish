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


