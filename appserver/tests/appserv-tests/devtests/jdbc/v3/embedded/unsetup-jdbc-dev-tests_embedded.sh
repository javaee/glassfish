#!/bin/sh
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


v3home=${S1AS_HOME}
databaseshome=/tmp/jdbc_devtests/databases
cd $v3home

echo undeploying application .....
./bin/asadmin undeploy v3_jdbc_dev_tests
echo "\n"

echo deleting jdbc-resources .....
./bin/asadmin delete-jdbc-resource jdbc/jdbc-multiple-user-test-resource
./bin/asadmin delete-jdbc-resource jdbc/jdbc-app-auth-test-resource
./bin/asadmin delete-jdbc-resource jdbc/jdbc-common-resource
./bin/asadmin delete-jdbc-resource jdbc/jdbc-stmt-timeout-test-resource
./bin/asadmin delete-jdbc-resource jdbc/jdbc-max-conn-usage-test-resource
./bin/asadmin delete-jdbc-resource jdbc/jdbc-conn-leak-tracing-test-resource
./bin/asadmin delete-jdbc-resource jdbc/jdbc-associate-with-thread-test-resource
#./bin/asadmin delete-jdbc-resource jdbc/jdbc-simple-xa-test-resource
./bin/asadmin delete-jdbc-resource jdbc/jdbc-simple-xa-test-resource-1
./bin/asadmin delete-jdbc-resource jdbc/jdbc-simple-xa-test-resource-2
./bin/asadmin delete-jdbc-resource jdbc/jdbc-lazy-assoc-test-resource
./bin/asadmin delete-jdbc-resource jdbc/jdbc-lazy-enlist-resource-1
./bin/asadmin delete-jdbc-resource jdbc/jdbc-lazy-enlist-resource-2
./bin/asadmin delete-jdbc-resource jdbc/double-resource-reference-resource-1
./bin/asadmin delete-jdbc-resource jdbc/double-resource-reference-resource-2
./bin/asadmin delete-jdbc-resource jdbc/jdbc-dev-test-resource
./bin/asadmin delete-jdbc-resource jdbc/jdbc-reconfig-test-resource-1
./bin/asadmin delete-jdbc-resource jdbc/jdbc-reconfig-test-resource-2
./bin/asadmin delete-jdbc-resource jdbc/res1
./bin/asadmin delete-jdbc-resource jdbc/res2
echo "\n"

echo deleting jdbc-connection-pools .....
./bin/asadmin delete-jdbc-connection-pool jdbc-multiple-user-test-pool
./bin/asadmin delete-jdbc-connection-pool jdbc-app-auth-test-pool
./bin/asadmin delete-jdbc-connection-pool jdbc-common-pool
./bin/asadmin delete-jdbc-connection-pool jdbc-statement-timeout-test-pool
./bin/asadmin delete-jdbc-connection-pool jdbc-max-conn-usage-test-pool
./bin/asadmin delete-jdbc-connection-pool jdbc-conn-leak-tracing-test-pool
./bin/asadmin delete-jdbc-connection-pool jdbc-associate-with-thread-test-pool
#./bin/asadmin delete-jdbc-connection-pool jdbc-simple-xa-test-pool
./bin/asadmin delete-jdbc-connection-pool jdbc-lazy-assoc-test-pool
./bin/asadmin delete-jdbc-connection-pool jdbc-simple-xa-test-pool-1
./bin/asadmin delete-jdbc-connection-pool jdbc-simple-xa-test-pool-2
./bin/asadmin delete-jdbc-connection-pool jdbc-lazy-enlist-pool-1
./bin/asadmin delete-jdbc-connection-pool jdbc-lazy-enlist-pool-2
./bin/asadmin delete-jdbc-connection-pool double-resource-reference-pool-1
./bin/asadmin delete-jdbc-connection-pool jdbc-dev-test-pool
./bin/asadmin delete-jdbc-connection-pool jdbc-reconfig-test-pool-1
./bin/asadmin delete-jdbc-connection-pool jdbc-reconfig-test-pool-2
./bin/asadmin delete-jdbc-connection-pool pool1
./bin/asadmin delete-jdbc-connection-pool pool2
echo "\n"

echo Deleting files...
rm -f ./SetDerbyAuthentication.class
rm -rf $databaseshome
cd $APS_HOME/devtests/jdbc/v3/embedded
rm -rf sun-appserv-samples/ lazyenlist-test-1/ lazyenlist-test-2/ *.class derby.log
echo "\n"

