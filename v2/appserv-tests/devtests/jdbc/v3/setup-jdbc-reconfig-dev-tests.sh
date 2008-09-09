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
./bin/asadmin create-jdbc-resource --connectionpoolid=jdbc-dev-test-pool jdbc/res1

echo create pool pool2
./bin/asadmin create-jdbc-connection-pool --datasourceclassname=org.apache.derby.jdbc.EmbeddedConnectionPoolDataSource --restype=javax.sql.ConnectionPoolDataSource --property="Password=rpassword:User=ruser:DatabaseName=$databaseshome/reconfig-db:serverName=localhost" pool2

echo create resource jdbc/res2
./bin/asadmin create-jdbc-resource --connectionpoolid=jdbc-reconfig-test-pool-1 jdbc/res2

echo "\n\n****************************************************************************************************************\n"

echo "\nExecuting TEST1 : JDBC Connection Pool Attribute (max-pool-size) Change \n"
echo "\nasadmin set --value=40 resources.jdbc-connection-pool.jdbc-dev-test-pool.max-pool-size \n"
./bin/asadmin set --value=40 resources.jdbc-connection-pool.jdbc-dev-test-pool.max-pool-size

#also set max-wait-time-in-millis to a smaller value so that tests run faster
echo "\nasadmin set --value=1000 resources.jdbc-connection-pool.jdbc-dev-test-pool.max-wait-time-in-millis \n"
./bin/asadmin set --value=1000 resources.jdbc-connection-pool.jdbc-dev-test-pool.max-wait-time-in-millis

echo "\nGET http://localhost:8080/v3_jdbc_dev_tests/ReconfigTestServlet?maxPoolSize=40\&throwException=true\&testId=1 \n"
GET http://localhost:8080/v3_jdbc_dev_tests/ReconfigTestServlet?maxPoolSize=40\&throwException=true\&testId=1 > $reconfigResult 
echo "\n"

#asadmin set max-pool-size to 10 before running test for the second time
echo "\nasadmin set --value=10 resources.jdbc-connection-pool.jdbc-dev-test-pool.max-pool-size \n"
./bin/asadmin set --value=10 resources.jdbc-connection-pool.jdbc-dev-test-pool.max-pool-size

echo "\nasadmin set --value=1000 resources.jdbc-connection-pool.jdbc-dev-test-pool.max-wait-time-in-millis \n"
./bin/asadmin set --value=1000 resources.jdbc-connection-pool.jdbc-dev-test-pool.max-wait-time-in-millis

echo "\nGET http://localhost:8080/v3_jdbc_dev_tests/ReconfigTestServlet?maxPoolSize=10\&throwException=true\&testId=1 \n"
GET http://localhost:8080/v3_jdbc_dev_tests/ReconfigTestServlet?maxPoolSize=10\&throwException=true\&testId=1 >> $reconfigResult
echo "\n"

#asadmin set max-pool-size to 20 before running test for the second time
echo "\nasadmin set --value=20 resources.jdbc-connection-pool.jdbc-dev-test-pool.max-pool-size \n"
./bin/asadmin set --value=20 resources.jdbc-connection-pool.jdbc-dev-test-pool.max-pool-size

echo "\nGET http://localhost:8080/v3_jdbc_dev_tests/ReconfigTestServlet?maxPoolSize=19\&throwException=false\&testId=1 \n"
GET http://localhost:8080/v3_jdbc_dev_tests/ReconfigTestServlet?maxPoolSize=19\&throwException=false\&testId=1 >> $reconfigResult
echo "\n"

echo "\nTEST1 executed successfully\n\n"
echo "\n******************************************************************************************************************\n"

echo "\nExecuting TEST2 : JDBC Connection Pool Property Change \n"
#asadmin set property User to a wrong value and try to get a connection
echo "\nasadmin set --value=APP2 resources.jdbc-connection-pool.jdbc-dev-test-pool.property.User \n"
./bin/asadmin set --value=APP2 resources.jdbc-connection-pool.jdbc-dev-test-pool.property.User

echo "\nRedeploying war... \n"
bin/asadmin deploy --force=true $war2deploy
echo "\n"

bin/asadmin stop-domain
echo "\nDeleting $v3home/domains/domain1/applications/v3_jdbc_dev_tests/WEB-INF/lib/glassfish-api-*.jar\n"
rm -rf $v3home/domains/domain1/applications/v3_jdbc_dev_tests/WEB-INF/lib/glassfish-api-*.jar
bin/asadmin start-domain
sleep 10
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
echo "\nasadmin set --value=jdbc-reconfig-test-pool-1 resources.jdbc-resource.jdbc/jdbc-reconfig-test-resource-2.pool-name \n"
./bin/asadmin set --value=jdbc-reconfig-test-pool-1 resources.jdbc-resource.jdbc/jdbc-reconfig-test-resource-2.pool-name

sleep 5
./bin/asadmin stop-domain
sleep 5
./bin/asadmin start-domain
sleep 10

echo "\nStatus : jdbc-reconfig-test-resource-2 is set with jdbc-reconfig-test-pool-1 (DB : sample-db with table sampleTable\n"
echo "\nGET http://localhost:8080/v3_jdbc_dev_tests/ReconfigTestServlet?throwException=true\&testId=3 \n"
GET http://localhost:8080/v3_jdbc_dev_tests/ReconfigTestServlet?throwException=true\&testId=3 >> $reconfigResult
sleep 5
#echo view the test results at ${reconfigResult}
#echo "\n"
#firefox $reconfigResult &
#sleep 10

#stop the domain
#./bin/asadmin stop-domain

echo "\nTEST3 executed successfully\n\n"
echo "\n******************************************************************************************************************\n"

echo "\nExecuting TEST4 : JDBC Resource reconfiguration\n"
echo "\n Testing if First resource undergoes change in the pool-name with an asadmin set\n"
echo "\nasadmin set --value=pool2 resources.jdbc-resource.jdbc/res1.pool-name\n"
./bin/asadmin set --value=pool2 resources.jdbc-resource.jdbc/res1.pool-name

sleep 5
./bin/asadmin stop-domain
sleep 5
./bin/asadmin start-domain
sleep 10

echo "\nGET http://localhost:8080/v3_jdbc_dev_tests/ReconfigTestServlet?throwException=true\&testId=3\n"
#Test should fail when table is sample-db and pass when table is reconfig-db
GET http://localhost:8080/v3_jdbc_dev_tests/ReconfigTestServlet?throwException=false\&testId=4 >> $reconfigResult

sleep 5
./bin/asadmin stop-domain
echo "\nTEST4 executed successfully\n\n"
echo "\n******************************************************************************************************************\n"

echo view the test results at ${reconfigResult}
echo "\n"
firefox $reconfigResult &
sleep 10

