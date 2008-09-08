#!/bin/sh
#set v3 home
v3home=/home/shalini/glassfish

#set databases home
databaseshome=$v3home/databases

#set war location
war2deploy=/home/shalini/v3/v3/v3/v3_jdbc_dev_tests/dist/v3_jdbc_dev_tests.war

#set Test Results Page
reconfigResult=/tmp/reconfig-results.html

cd $v3home
echo "Starting domain..."
./bin/asadmin start-domain
sleep 10 
echo "\n"

#create pool jdbc-dev-test-pool
./bin/asadmin create-jdbc-connection-pool --datasourceclassname=org.apache.derby.jdbc.EmbeddedConnectionPoolDataSource --restype=javax.sql.ConnectionPoolDataSource --property="Password=APP:User=APP:DatabaseName=$databaseshome/sun-appserv-samples:serverName=localhost:connectionAttributes=\;create\\=true" jdbc-dev-test-pool

#create resource jdbc/jdbc-dev-test-resource
./bin/asadmin create-jdbc-resource --connectionpoolid=jdbc-dev-test-pool jdbc/jdbc-dev-test-resource

#create pool jdbc-reconfig-test-pool-1
./bin/asadmin create-jdbc-connection-pool --datasourceclassname=org.apache.derby.jdbc.EmbeddedConnectionPoolDataSource --restype=javax.sql.ConnectionPoolDataSource --property="Password=APP:User=APP:DatabaseName=$databaseshome/sample-db:serverName=localhost" jdbc-reconfig-test-pool-1
echo "Created jdbc-reconfig-test-pool-1 \n"

#create resource jdbc/jdbc-reconfig-test-resource-1
./bin/asadmin create-jdbc-resource --connectionpoolid=jdbc-reconfig-test-pool-1 jdbc/jdbc-reconfig-test-resource-1
echo "Created jdbc/jdbc-reconfig-test-resource-1 \n"

#create pool jdbc-reconfig-test-pool-2 
./bin/asadmin create-jdbc-connection-pool --datasourceclassname=org.apache.derby.jdbc.EmbeddedConnectionPoolDataSource --restype=javax.sql.ConnectionPoolDataSource --property="Password=rpassword:User=ruser:DatabaseName=$databaseshome/reconfig-db:serverName=localhost" jdbc-reconfig-test-pool-2
echo "Created jdbc-reconfig-test-pool-2 \n"

#create resource jdbc/jdbc-reconfig-test-resource-2
./bin/asadmin create-jdbc-resource --connectionpoolid=jdbc-reconfig-test-pool-2 jdbc/jdbc-reconfig-test-resource-2

echo "Setting max-pool-size of jdbc-dev-test-pool to 40 to test set when pool is not activated\n"
#asadmin set max-pool-size to 40 before running test 
./bin/asadmin set --value=40 resources.jdbc-connection-pool.jdbc-dev-test-pool.max-pool-size

#also set max-wait-time-in-millis to a smaller value so that tests run faster
./bin/asadmin set --value=1000 resources.jdbc-connection-pool.jdbc-dev-test-pool.max-wait-time-in-millis

echo "Executing JDBC Connection Pool Attribute/Property Reconfiguration Tests\n"
#testId=1 for attribute change
GET http://localhost:8080/v3_jdbc_dev_tests/ReconfigTestServlet?maxPoolSize=40\&throwException=true\&testId=1 > $reconfigResult 
echo "\n"

#asadmin set max-pool-size to 10 before running test for the second time
./bin/asadmin set --value=10 resources.jdbc-connection-pool.jdbc-dev-test-pool.max-pool-size

#also set max-wait-time-in-millis to a smaller value so that tests run faster
./bin/asadmin set --value=1000 resources.jdbc-connection-pool.jdbc-dev-test-pool.max-wait-time-in-millis

#sleep 10
GET http://localhost:8080/v3_jdbc_dev_tests/ReconfigTestServlet?maxPoolSize=10\&throwException=true\&testId=1 >> $reconfigResult
echo "\n"

#asadmin set max-pool-size to 20 before running test for the second time
./bin/asadmin set --value=20 resources.jdbc-connection-pool.jdbc-dev-test-pool.max-pool-size

sleep 10
GET http://localhost:8080/v3_jdbc_dev_tests/ReconfigTestServlet?maxPoolSize=19\&throwException=false\&testId=1 >> $reconfigResult
echo "\n"

#asadmin set property User to a wrong value and try to get a connection
./bin/asadmin set --value=APP2 resources.jdbc-connection-pool.jdbc-dev-test-pool.property.User

echo Configuring GlassFish to run the tests for testing property change...
echo redeploying war...
bin/asadmin deploy --force=true $war2deploy
echo "\n"

bin/asadmin stop-domain
echo Deleting $v3home/domains/domain1/applications/v3_jdbc_dev_tests/WEB-INF/lib/*.jar
rm -rf $v3home/domains/domain1/applications/v3_jdbc_dev_tests/WEB-INF/lib/glassfish-api-*.jar
bin/asadmin start-domain
sleep 15
echo "\n"

GET http://localhost:8080/v3_jdbc_dev_tests/ReconfigTestServlet?throwException=true\&testId=2 >> $reconfigResult

echo "Executing JDBC Resource reconfiguration tests\n"
echo "Status : jdbc-reconfig-test-resource-2 is set with jdbc-reconfig-test-pool-2 (DB: reconfig-db with table reconfigTestTable\n"
echo "Executing ReconfigTestServlet ... \n"
GET http://localhost:8080/v3_jdbc_dev_tests/ReconfigTestServlet?throwException=false\&testId=3 >> $reconfigResult
echo "\n"

#asadmin change pool-name before running test for the second time
./bin/asadmin set --value=jdbc-reconfig-test-pool-1 resources.jdbc-resource.jdbc/jdbc-reconfig-test-resource-2.pool-name

sleep 5
./bin/asadmin stop-domain
sleep 5
./bin/asadmin start-domain

sleep 10
echo "Executing Reconfig Test for resource property change. throwException=true \n"
echo "Status : jdbc-reconfig-test-resource-2 is set with jdbc-reconfig-test-pool-1 (DB : sample-db with table sampleTable\n"
echo "Executing ReconfigTestServlet ... \n"
GET http://localhost:8080/v3_jdbc_dev_tests/ReconfigTestServlet?throwException=true\&testId=3 >> $reconfigResult
sleep 5
echo view the test results at ${reconfigResult}
echo "\n"
firefox $reconfigResult &
sleep 10

#stop the domain
./bin/asadmin stop-domain

