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

#set databases home
databaseshome=/tmp/jdbc_devtests/databases

v3jdbcdevtestshome=$APS_HOME/devtests/jdbc/v3

#set war location
war2deploy=$v3jdbcdevtestshome/v3_jdbc_dev_tests/dist/v3_jdbc_dev_tests.war

#set Test Results Page
reconfigResult=/tmp/jdbc_devtests/reconfig-results.html

cd $v3home

if [ "$1" = "test1_and_2" ]; then

echo "*******************************************************************************************************************\n"
echo "\nExecuting Reconfiguration Tests \n\n"

echo "Creating Pools \n\n"
echo create pool jdbc-dev-test-pool
./bin/asadmin create-jdbc-connection-pool --datasourceclassname=org.apache.derby.jdbc.EmbeddedConnectionPoolDataSource --restype=javax.sql.ConnectionPoolDataSource --property="Password=APP:User=APP:DatabaseName=$databaseshome/sun-appserv-samples:serverName=localhost:connectionAttributes=\;create\\=true" jdbc-dev-test-pool

echo create resource jdbc/jdbc-dev-test-resource
./bin/asadmin create-jdbc-resource --connectionpoolid=jdbc-dev-test-pool jdbc/jdbc-dev-test-resource

echo create pool jdbc-reconfig-test-pool-1
./bin/asadmin create-jdbc-connection-pool --datasourceclassname=org.apache.derby.jdbc.EmbeddedConnectionPoolDataSource --restype=javax.sql.ConnectionPoolDataSource --property="Password=APP:User=APP:DatabaseName=$databaseshome/sample-db:serverName=localhost" jdbc-reconfig-test-pool-1

echo create resource jdbc/jdbc-reconfig-test-resource-1
./bin/asadmin create-jdbc-resource --connectionpoolid=jdbc-reconfig-test-pool-1 jdbc/jdbc-reconfig-test-resource-1

echo create pool jdbc-reconfig-test-pool-2 
./bin/asadmin create-jdbc-connection-pool --datasourceclassname=org.apache.derby.jdbc.EmbeddedConnectionPoolDataSource --restype=javax.sql.ConnectionPoolDataSource --property="Password=rpassword:User=ruser:DatabaseName=$databaseshome/reconfig-db:serverName=localhost" jdbc-reconfig-test-pool-2

echo create resource jdbc/jdbc-reconfig-test-resource-2
./bin/asadmin create-jdbc-resource --connectionpoolid=jdbc-reconfig-test-pool-2 jdbc/jdbc-reconfig-test-resource-2

echo create pool pool1
./bin/asadmin create-jdbc-connection-pool --datasourceclassname=org.apache.derby.jdbc.EmbeddedConnectionPoolDataSource --restype=javax.sql.ConnectionPoolDataSource --property="Password=APP:User=APP:DatabaseName=$databaseshome/sample-db:serverName=localhost:connectionAttributes=\;create\\=true" pool1

echo create resource jdbc/res1
./bin/asadmin create-jdbc-resource --connectionpoolid=pool1 jdbc/res1

echo create pool pool2
./bin/asadmin create-jdbc-connection-pool --datasourceclassname=org.apache.derby.jdbc.EmbeddedConnectionPoolDataSource --restype=javax.sql.ConnectionPoolDataSource --property="Password=rpassword:User=ruser:DatabaseName=$databaseshome/reconfig-db:serverName=localhost" pool2

echo create resource jdbc/res2
./bin/asadmin create-jdbc-resource --connectionpoolid=pool2 jdbc/res2

echo "\n\n****************************************************************************************************************\n"

echo "\nExecuting TEST1 : JDBC Connection Pool Attribute (max-pool-size) Change \n"
echo "\nasadmin set resources.jdbc-connection-pool.jdbc-dev-test-pool.max-pool-size=40 \n"
./bin/asadmin set resources.jdbc-connection-pool.jdbc-dev-test-pool.max-pool-size=40

#also set max-wait-time-in-millis to a smaller value so that tests run faster
echo "\nasadmin set resources.jdbc-connection-pool.jdbc-dev-test-pool.max-wait-time-in-millis=1000 \n"
./bin/asadmin set resources.jdbc-connection-pool.jdbc-dev-test-pool.max-wait-time-in-millis=1000

echo "\nGET http://localhost:8080/v3_jdbc_dev_tests/ReconfigTestServlet?maxPoolSize=40\&throwException=true\&testId=1 \n"
GET http://localhost:8080/v3_jdbc_dev_tests/ReconfigTestServlet?maxPoolSize=40\&throwException=true\&testId=1 > $reconfigResult 
echo "\n"

#asadmin set max-pool-size to 10 before running test for the second time
echo "\nasadmin set resources.jdbc-connection-pool.jdbc-dev-test-pool.max-pool-size=10 \n"
./bin/asadmin set resources.jdbc-connection-pool.jdbc-dev-test-pool.max-pool-size=10

echo "\nasadmin set resources.jdbc-connection-pool.jdbc-dev-test-pool.max-wait-time-in-millis=1000 \n"
./bin/asadmin set resources.jdbc-connection-pool.jdbc-dev-test-pool.max-wait-time-in-millis=1000

echo "\nGET http://localhost:8080/v3_jdbc_dev_tests/ReconfigTestServlet?maxPoolSize=10\&throwException=true\&testId=1 \n"
GET http://localhost:8080/v3_jdbc_dev_tests/ReconfigTestServlet?maxPoolSize=10\&throwException=true\&testId=1 >> $reconfigResult
echo "\n"

#asadmin set max-pool-size to 20 before running test for the second time
echo "\nasadmin set resources.jdbc-connection-pool.jdbc-dev-test-pool.max-pool-size=20 \n"
./bin/asadmin set resources.jdbc-connection-pool.jdbc-dev-test-pool.max-pool-size=20

echo "\nGET http://localhost:8080/v3_jdbc_dev_tests/ReconfigTestServlet?maxPoolSize=19\&throwException=false\&testId=1 \n"
GET http://localhost:8080/v3_jdbc_dev_tests/ReconfigTestServlet?maxPoolSize=19\&throwException=false\&testId=1 >> $reconfigResult
echo "\n"

echo "\nTEST1 executed successfully\n\n"
echo "\n******************************************************************************************************************\n"

echo "\nExecuting TEST2 : JDBC Connection Pool Property Change \n"
#asadmin set property User to a wrong value and try to get a connection
echo "\nasadmin set resources.jdbc-connection-pool.jdbc-dev-test-pool.property.User=APP2 \n"
./bin/asadmin set resources.jdbc-connection-pool.jdbc-dev-test-pool.property.User=APP2

echo "\nRedeploying war... \n"
./bin/asadmin undeploy v3_jdbc_dev_tests
./bin/asadmin deploy --force=true $war2deploy
echo "\n"

echo "\nGET http://localhost:8080/v3_jdbc_dev_tests/ReconfigTestServlet?throwException=true\&testId=2 \n"
GET http://localhost:8080/v3_jdbc_dev_tests/ReconfigTestServlet?throwException=true\&testId=2 >> $reconfigResult

echo "\nTEST2 executed successfully\n\n"
echo "\n******************************************************************************************************************\n"

echo "\nExecuting TEST3 : JDBC Resource reconfiguration\n"
echo "\nStatus : jdbc-reconfig-test-resource-2 is set with jdbc-reconfig-test-pool-2 (DB: reconfig-db with table reconfigTestTable\n"
echo "\nGET http://localhost:8080/v3_jdbc_dev_tests/ReconfigTestServlet?throwException=false\&testId=3 \n"
GET http://localhost:8080/v3_jdbc_dev_tests/ReconfigTestServlet?throwException=false\&testId=3 >> $reconfigResult
echo "\n"

#asadmin change pool-name before running test for the second time
echo "\nasadmin set resources.jdbc-resource.jdbc/jdbc-reconfig-test-resource-2.pool-name=jdbc-reconfig-test-pool-1 \n"
./bin/asadmin set resources.jdbc-resource.jdbc/jdbc-reconfig-test-resource-2.pool-name=jdbc-reconfig-test-pool-1

# the server needs to restarted after the pool name change.

elif [ "$1" = "test3" ]; then

echo "\nStatus : jdbc-reconfig-test-resource-2 is set with jdbc-reconfig-test-pool-1 (DB : sample-db with table sampleTable\n"
echo "\nGET http://localhost:8080/v3_jdbc_dev_tests/ReconfigTestServlet?throwException=true\&testId=3 \n"
GET http://localhost:8080/v3_jdbc_dev_tests/ReconfigTestServlet?throwException=true\&testId=3 >> $reconfigResult
sleep 5
echo view the test results at file://${reconfigResult}
echo "\n"
echo "\nTEST3 executed successfully\n\n"
echo "\n******************************************************************************************************************\n"


#asadmin change pool-name before running TEST4
echo "\nExecuting TEST4 : JDBC Resource reconfiguration\n"
echo "\nTesting if First resource undergoes change in the pool-name with an asadmin set\n"
echo "\nasadmin set resources.jdbc-resource.jdbc/res1.pool-name=pool2\n"
./bin/asadmin set resources.jdbc-resource.jdbc/res1.pool-name=pool2

# server needs to be restarted after the pool name change.


elif [ "$1" = "test4" ]; then

echo "\nGET http://localhost:8080/v3_jdbc_dev_tests/ReconfigTestServlet?throwException=false\&testId=4\n"
#Test should fail when table is sample-db and pass when table is reconfig-db
GET http://localhost:8080/v3_jdbc_dev_tests/ReconfigTestServlet?throwException=false\&testId=4 >> $reconfigResult

echo "\nTEST4 executed successfully\n\n"
echo "\n******************************************************************************************************************\n"

echo view the test results at file://${reconfigResult}
echo "\n"

fi
