#!/bin/bash
#
# DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
#
# Copyright (c) 2013 Oracle and/or its affiliates. All rights reserved.
#
# The contents of this file are subject to the terms of either the GNU
# General Public License Version 2 only ("GPL") or the Common Development
# and Distribution License("CDDL") (collectively, the "License").  You
# may not use this file except in compliance with the License.  You can
# obtain a copy of the License at
# https://glassfish.dev.java.net/public/CDDL+GPL_1_1.html
# or packager/legal/LICENSE.txt.  See the License for the specific
# language governing permissions and limitations under the License.
#
# When distributing the software, include this License Header Notice in each
# file and include the License file at packager/legal/LICENSE.txt.
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

export http_proxy=http://www-proxy.us.oracle.com:80

rm -rf glassfish-v4-image
mkdir glassfish-v4-image
pushd glassfish-v4-image

wget --no-proxy http://gf-hudson.us.oracle.com/hudson/job/gf-4.1.2-build-continuous/lastSuccessfulBuild/artifact/bundles/glassfish.zip
unzip -q glassfish.zip

export S1AS_HOME=$PWD/glassfish4/glassfish
popd

rm -rf opends-image
mkdir opends-image
pushd opends-image

wget --no-check-certificate http://java.net/downloads/opends/promoted-builds/2.2.1/OpenDS-2.2.1.zip
unzip -q OpenDS-2.2.1.zip

export OPENDS_HOME=$PWD/OpenDS-2.2.1
popd

date
java -version
ant -version

export APS_HOME=$PWD/appserv-tests

(jps |grep ASMain |cut -f1 -d" " | xargs kill -9  > /dev/null 2>&1) || true
(jps |grep DerbyControl |cut -f1 -d" " | xargs kill -9  > /dev/null 2>&1) || true
(jps |grep DirectoryServer |cut -f1 -d" " | xargs kill -9  > /dev/null 2>&1) || true

# Workaround for JDK7 and OpenDS
cp $APS_HOME/devtests/security/ldap/opends/X500Signer.jar $OPENDS_HOME/lib

# Configure and start OpenDS using the default ports
$OPENDS_HOME/setup -i -v -n -p 1389 --adminConnectorPort 4444 -x 1689 -w dmanager -b "dc=sfbay,dc=sun,dc=com" -Z 1636 --useJavaKeystore $S1AS_HOME/domains/domain1/config/keystore.jks -W changeit -N s1as


$S1AS_HOME/bin/asadmin start-database
$S1AS_HOME/bin/asadmin start-domain
pushd $APS_HOME/devtests/security
unset http_proxy
rm count.txt || true
ant all |tee log.txt

$S1AS_HOME/bin/asadmin stop-domain
$S1AS_HOME/bin/asadmin stop-database
$OPENDS_HOME/bin/stop-ds -p 4444 -D "cn=Directory Manager" -w dmanager -P $OPENDS_HOME/config/admin-truststore -U $OPENDS_HOME/config/admin-keystore.pin

egrep 'FAILED= *0' count.txt
egrep 'DID NOT RUN= *0' count.txt
popd

date
#(jps |grep ASMain |cut -f1 -d" " | xargs kill -9  > /dev/null 2>&1) || true
#(jps |grep DerbyControl |cut -f1 -d" " | xargs kill -9  > /dev/null 2>&1) || true
#(jps |grep DirectoryServer |cut -f1 -d" " | xargs kill -9  > /dev/null 2>&1) || true
