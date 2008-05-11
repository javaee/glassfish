#!/bin/sh
#set v3 home
v3home=/space/connectors/v3/glassfish

#set war location
war2deploy=/space/connectors/v3/Devtests/v3_jdbc_dev_tests/dist/v3_jdbc_dev_tests.war

#set Derby location
derbyhome=$v3home/javadb

#set Test Results Page
testResult=/space/connectors/tmp/jdbc-tests-result.html

echo Setting up Derby for Authentication...
javac -classpath $derbyhome/lib/derby.jar SetDerbyAuthentication.java
java -classpath $derbyhome/lib/derby.jar:. SetDerbyAuthentication
echo "\n"

cd $v3home
echo "Starting domain..."
bin/asadmin start-domain
sleep 5
echo "\n"

#Create Pool/Resource for Multiple User Credentials Test
echo Creating Pool/Resource for Multiple User Credentials Test...
bin/asadmin create-jdbc-connection-pool --datasourceclassname=org.apache.derby.jdbc.EmbeddedConnectionPoolDataSource --restype=javax.sql.ConnectionPoolDataSource  --matchconnections=true --maxwait=15000 --idletimeout=900 --property="password=APP:user=APP:databaseName=sun-appserv-samples:connectionAttributes=\;create\\=true" jdbc-multiple-user-test-pool
bin/asadmin create-jdbc-resource --connectionpoolid=jdbc-multiple-user-test-pool jdbc/jdbc-multiple-user-test-resource
echo "\n"

#Create Pool/Resource for Application Authentication Test
echo Creating Pool/Resource for Application Authentication Test...
bin/asadmin create-jdbc-connection-pool --datasourceclassname=org.apache.derby.jdbc.EmbeddedConnectionPoolDataSource --restype=javax.sql.ConnectionPoolDataSource  --property="password=APP:user=APP:databaseName=sun-appserv-samples:connectionAttributes=\;create\\=true" jdbc-app-auth-test-pool
bin/asadmin create-jdbc-resource --connectionpoolid=jdbc-app-auth-test-pool jdbc/jdbc-app-auth-test-resource
echo "\n"

#Create Pool/Resource for Statement Timeout Test
echo Creating Pool/Resource for Statement Timeout Test...
bin/asadmin create-jdbc-connection-pool --datasourceclassname=org.apache.derby.jdbc.EmbeddedConnectionPoolDataSource --restype=javax.sql.ConnectionPoolDataSource  --statementtimeout=30 --property="password=APP:user=APP:databaseName=sun-appserv-samples:connectionAttributes=\;create\\=true" jdbc-statement-timeout-test-pool
bin/asadmin create-jdbc-resource --connectionpoolid=jdbc-statement-timeout-test-pool jdbc/jdbc-stmt-timeout-test-resource
echo "\n"

#Create Pool/Resource for Max Connection Usage Test
echo Creating Pool/Resource for Max Connection Usage Test...
bin/asadmin create-jdbc-connection-pool --datasourceclassname=org.apache.derby.jdbc.EmbeddedConnectionPoolDataSource --restype=javax.sql.DataSource --steadypoolsize=1 --maxpoolsize=1 --maxconnectionusagecount=10 --property="password=APP:user=APP:databaseName=sun-appserv-samples:connectionAttributes=\;create\\=true" jdbc-max-conn-usage-test-pool
bin/asadmin create-jdbc-resource --connectionpoolid=jdbc-max-conn-usage-test-pool jdbc/jdbc-max-conn-usage-test-resource
echo "\n"

#Create Pool/Resource for Connection Leak Tracing Test
echo Creating Pool/Resource for Connection Leak Tracing Test...
bin/asadmin create-jdbc-connection-pool --datasourceclassname=org.apache.derby.jdbc.EmbeddedConnectionPoolDataSource --restype=javax.sql.DataSource --steadypoolsize=1 --maxpoolsize=1 --leaktimeout=10 --leakreclaim=true --property="password=APP:user=APP:databaseName=sun-appserv-samples:connectionAttributes=\;create\\=true" jdbc-conn-leak-tracing-test-pool
bin/asadmin create-jdbc-resource --connectionpoolid=jdbc-conn-leak-tracing-test-pool jdbc/jdbc-conn-leak-tracing-test-resource
echo "\n"

#Create Pool/Resource for associate-with-thread test
echo Creating Pool/Resource Associate With Thread test
bin/asadmin create-jdbc-connection-pool --datasourceclassname=org.apache.derby.jdbc.EmbeddedConnectionPoolDataSource --restype=javax.sql.ConnectionPoolDataSource  --associatewiththread=false --property="password=APP:user=APP:databaseName=sun-appserv-samples:connectionAttributes=\;create\\=true" jdbc-associate-with-thread-test-pool
bin/asadmin create-jdbc-resource --connectionpoolid=jdbc-associate-with-thread-test-pool jdbc/jdbc-associate-with-thread-test-resource
echo "\n"


#Create Pool/Resource for Other tests
echo Creating Pool/Resource for All other tests...
bin/asadmin create-jdbc-connection-pool --datasourceclassname=org.apache.derby.jdbc.EmbeddedConnectionPoolDataSource --restype=javax.sql.ConnectionPoolDataSource  --property="password=APP:user=APP:databaseName=sun-appserv-samples:connectionAttributes=\;create\\=true" jdbc-common-pool
bin/asadmin create-jdbc-resource --connectionpoolid=jdbc-common-pool jdbc/jdbc-common-resource
echo "\n"

#bin/asadmin create-jdbc-connection-pool --datasourceclassname=org.apache.derby.jdbc.ClientDataSource --restype=javax.sql.DataSource --isolationlevel=TRANSACTION_READ_COMMITTED --property="password=APP:user=APP:databaseName=sun-appserv-samples:connectionAttributes=\;create\\=true" jdbc-dev-test-pool

#bin/asadmin create-jdbc-resource --connectionpoolid=jdbc-dev-test-pool jdbc/jdbc-dev-test-resource

echo Deploying war...
bin/asadmin deploy --force=true $war2deploy
echo "\n"

echo Configuring GlassFish to run the tests...
bin/asadmin stop-domain
echo Deleting $v3home/domains/domain1/applications/v3_jdbc_dev_tests/WEB-INF/lib/glassfish-api-10.0-SNAPSHOT.jar
rm -rf $v3home/domains/domain1/applications/v3_jdbc_dev_tests/WEB-INF/lib/glassfish-api-10.0-SNAPSHOT.jar
bin/asadmin start-domain
echo "\n"

sleep 5
wget http://localhost:8080/v3_jdbc_dev_tests/TestResultServlet -O $testResult
sleep 5
echo view the test results at ${testResult}
echo "\n"
firefox $testResult &
