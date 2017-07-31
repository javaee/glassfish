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


#set v3 home
v3home=${S1AS_HOME}

#set v3 jdbc devtests home
v3jdbcdevtestshome=$APS_HOME/devtests/jdbc/v3

#set war location
war2deploy=$v3jdbcdevtestshome/v3_jdbc_dev_tests/dist/v3_jdbc_dev_tests.war

#set Derby location
derbyhome=$v3home/../javadb

#set databases home
databaseshome=/tmp/jdbc_devtests/databases
mkdir -p $databaseshome

#set Test Results Page
testResult1=/tmp/jdbc_devtests/jdbc-tests-result-1.html
testResult2=/tmp/jdbc_devtests/jdbc-tests-result-2.html

echo Setting up Derby for Authentication...
javac -classpath $derbyhome/lib/derby.jar ../SetDerbyAuthentication.java -d $v3jdbcdevtestshome/embedded
java -classpath $derbyhome/lib/derby.jar:. SetDerbyAuthentication
echo "\n"

cd $v3home

#Create Pool/Resource for Multiple User Credentials Test
echo Creating Pool/Resource for Multiple User Credentials Test...
./bin/asadmin create-jdbc-connection-pool --datasourceclassname=org.apache.derby.jdbc.EmbeddedConnectionPoolDataSource --restype=javax.sql.ConnectionPoolDataSource  --matchconnections=true --maxwait=15000 --idletimeout=900 --property="password=APP:user=APP:databaseName=sun-appserv-samples:connectionAttributes=\;create\\=true" jdbc-multiple-user-test-pool
./bin/asadmin create-jdbc-resource --connectionpoolid=jdbc-multiple-user-test-pool jdbc/jdbc-multiple-user-test-resource
echo "\n"

#Create Pool/Resource for Application Authentication Test
echo Creating Pool/Resource for Application Authentication Test...
./bin/asadmin create-jdbc-connection-pool --datasourceclassname=org.apache.derby.jdbc.EmbeddedConnectionPoolDataSource --restype=javax.sql.ConnectionPoolDataSource  --property="password=APP:user=APP:databaseName=$databaseshome/sun-appserv-samples:connectionAttributes=\;create\\=true" jdbc-app-auth-test-pool
./bin/asadmin create-jdbc-resource --connectionpoolid=jdbc-app-auth-test-pool jdbc/jdbc-app-auth-test-resource
echo "\n"

#Create Pool/Resource for Statement Timeout Test
echo Creating Pool/Resource for Statement Timeout Test...
./bin/asadmin create-jdbc-connection-pool --datasourceclassname=org.apache.derby.jdbc.EmbeddedConnectionPoolDataSource --restype=javax.sql.ConnectionPoolDataSource  --statementtimeout=30 --property="password=APP:user=APP:databaseName=sun-appserv-samples:connectionAttributes=\;create\\=true" jdbc-statement-timeout-test-pool
./bin/asadmin create-jdbc-resource --connectionpoolid=jdbc-statement-timeout-test-pool jdbc/jdbc-stmt-timeout-test-resource
echo "\n"

#Create Pool/Resource for Max Connection Usage Test
echo Creating Pool/Resource for Max Connection Usage Test...
./bin/asadmin create-jdbc-connection-pool --datasourceclassname=org.apache.derby.jdbc.EmbeddedConnectionPoolDataSource --restype=javax.sql.ConnectionPoolDataSource --steadypoolsize=1 --maxpoolsize=1 --maxconnectionusagecount=10 --property="password=APP:user=APP:databaseName=sun-appserv-samples:connectionAttributes=\;create\\=true" jdbc-max-conn-usage-test-pool
./bin/asadmin create-jdbc-resource --connectionpoolid=jdbc-max-conn-usage-test-pool jdbc/jdbc-max-conn-usage-test-resource
echo "\n"

#Create Pool/Resource for Connection Leak Tracing Test
echo Creating Pool/Resource for Connection Leak Tracing Test...
./bin/asadmin create-jdbc-connection-pool --datasourceclassname=org.apache.derby.jdbc.EmbeddedConnectionPoolDataSource --restype=javax.sql.ConnectionPoolDataSource --steadypoolsize=1 --maxpoolsize=1 --leaktimeout=10 --leakreclaim=true --lazyconnectionassociation=false --lazyconnectionenlistment=false --property="password=APP:user=APP:databaseName=sun-appserv-samples:connectionAttributes=\;create\\=true" jdbc-conn-leak-tracing-test-pool
./bin/asadmin create-jdbc-resource --connectionpoolid=jdbc-conn-leak-tracing-test-pool jdbc/jdbc-conn-leak-tracing-test-resource
echo "\n"

#Create Pool/Resource for Other tests
echo Creating Pool/Resource for All other tests...
./bin/asadmin create-jdbc-connection-pool --datasourceclassname=org.apache.derby.jdbc.EmbeddedConnectionPoolDataSource --restype=javax.sql.ConnectionPoolDataSource  --property="password=APP:user=APP:databaseName=sun-appserv-samples:connectionAttributes=\;create\\=true" jdbc-common-pool
./bin/asadmin create-jdbc-resource --connectionpoolid=jdbc-common-pool jdbc/jdbc-common-resource
echo "\n"


#Create Pool/Resource for associate-with-thread test
echo Creating Pool/Resource Associate With Thread test
./bin/asadmin create-jdbc-connection-pool --datasourceclassname=org.apache.derby.jdbc.EmbeddedConnectionPoolDataSource --restype=javax.sql.ConnectionPoolDataSource  --nontransactionalconnections=true --associatewiththread=true --property="password=APP:user=APP:databaseName=sun-appserv-samples:connectionAttributes=\;create\\=true" jdbc-associate-with-thread-test-pool
./bin/asadmin create-jdbc-resource --connectionpoolid=jdbc-associate-with-thread-test-pool jdbc/jdbc-associate-with-thread-test-resource
echo "\n"


#Create Pool/Resource for lazy-connection-associationtest
echo Creating Pool/Resource Lazy connection association test
./bin/asadmin create-jdbc-connection-pool --maxwait=10 --datasourceclassname=org.apache.derby.jdbc.EmbeddedConnectionPoolDataSource --restype=javax.sql.ConnectionPoolDataSource  --lazyconnectionenlistment=true --lazyconnectionassociation=true --property="password=APP:user=APP:databaseName=sun-appserv-samples:connectionAttributes=\;create\\=true" jdbc-lazy-assoc-test-pool 
./bin/asadmin create-jdbc-resource --connectionpoolid=jdbc-lazy-assoc-test-pool jdbc/jdbc-lazy-assoc-test-resource
echo "\n"


#Create Pool/Resource (1) for Simple XA Test
echo "Creating Pool/Resource (1) Simple XA Test"
./bin/asadmin create-jdbc-connection-pool --datasourceclassname=org.apache.derby.jdbc.EmbeddedXADataSource --restype=javax.sql.XADataSource  --associatewiththread=false --property="password=APP:user=APP:databaseName=xa-test-1:connectionAttributes=\;create\\=true" jdbc-simple-xa-test-pool-1
./bin/asadmin create-jdbc-resource --connectionpoolid=jdbc-simple-xa-test-pool-1 jdbc/jdbc-simple-xa-test-resource-1
echo "\n"

#Create Pool/Resource (2)  for Simple XA Test
echo "Creating Pool/Resource (2) Simple XA Test"
./bin/asadmin create-jdbc-connection-pool --datasourceclassname=org.apache.derby.jdbc.EmbeddedConnectionPoolDataSource --restype=javax.sql.ConnectionPoolDataSource  --property="password=APP:user=APP:databaseName=xa-test-2:connectionAttributes=\;create\\=true" jdbc-simple-xa-test-pool-2
./bin/asadmin create-jdbc-resource --connectionpoolid=jdbc-simple-xa-test-pool-2 jdbc/jdbc-simple-xa-test-resource-2
echo "\n"


#Create Pool/Resource (1)  for Lazy Connection Enlistment test
echo "Creating Pool/Resource (1) Lazy Connection Enlistment test"
./bin/asadmin create-jdbc-connection-pool --datasourceclassname=org.apache.derby.jdbc.EmbeddedConnectionPoolDataSource --restype=javax.sql.ConnectionPoolDataSource  --property="password=APP:user=APP:databaseName=lazyenlist-test-1:connectionAttributes=\;create\\=true" jdbc-lazy-enlist-pool-1
./bin/asadmin create-jdbc-resource --connectionpoolid=jdbc-lazy-enlist-pool-1 jdbc/jdbc-lazy-enlist-resource-1
echo "\n"


#Create Pool/Resource (2)  for Lazy Connection Enlistment test
echo "Creating Pool/Resource (2) Lazy Connection Enlistment test"
./bin/asadmin create-jdbc-connection-pool --lazyconnectionenlistment=true --datasourceclassname=org.apache.derby.jdbc.EmbeddedConnectionPoolDataSource --restype=javax.sql.ConnectionPoolDataSource  --property="password=APP:user=APP:databaseName=lazyenlist-test-2:connectionAttributes=\;create\\=true" jdbc-lazy-enlist-pool-2
./bin/asadmin create-jdbc-resource --connectionpoolid=jdbc-lazy-enlist-pool-2 jdbc/jdbc-lazy-enlist-resource-2
echo "\n"


#Create Pool/Resource (1)  for double resource reference test
echo "Creating Pool/Resource (1) for double resource reference test"
./bin/asadmin create-jdbc-connection-pool --datasourceclassname=org.apache.derby.jdbc.EmbeddedConnectionPoolDataSource --restype=javax.sql.ConnectionPoolDataSource  --property="password=APP:user=APP:databaseName=sun-appserv-samples:connectionAttributes=\;create\\=true" double-resource-reference-pool-1
./bin/asadmin create-jdbc-resource --connectionpoolid=double-resource-reference-pool-1 jdbc/double-resource-reference-resource-1
echo "\n"


#Create Resource (2)  for double resource reference test
echo "Creating Resource (2) for double resource reference test"
./bin/asadmin create-jdbc-resource --connectionpoolid=double-resource-reference-pool-1 jdbc/double-resource-reference-resource-2
echo "\n"


echo Deploying war...
./bin/asadmin deploy --force=true $war2deploy
echo "\n"
sleep 5

wget http://localhost:8080/v3_jdbc_dev_tests/TestResultServlet -O $testResult1
wget http://localhost:8080/v3_jdbc_dev_tests/TestResultServlet?testName=lazy-assoc -O $testResult2

echo view the test results at file://${testResult1}
echo view the test results at file://${testResult2}
echo "\n"

