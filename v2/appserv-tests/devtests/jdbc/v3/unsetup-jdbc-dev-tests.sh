v3home=/space/connectors/v3/glassfish
cd $v3home

echo undeploying application .....
bin/asadmin undeploy v3_jdbc_dev_tests
echo "\n"

echo deleting jdbc-resources .....
bin/asadmin delete-jdbc-resource jdbc/jdbc-multiple-user-test-resource
bin/asadmin delete-jdbc-resource jdbc/jdbc-app-auth-test-resource
bin/asadmin delete-jdbc-resource jdbc/jdbc-common-resource
bin/asadmin delete-jdbc-resource jdbc/jdbc-stmt-timeout-test-resource
bin/asadmin delete-jdbc-resource jdbc/jdbc-max-conn-usage-test-resource
bin/asadmin delete-jdbc-resource jdbc/jdbc-conn-leak-tracing-test-resource
echo "\n"

echo deleting jdbc-connection-pools .....
bin/asadmin delete-jdbc-connection-pool jdbc-multiple-user-test-pool
bin/asadmin delete-jdbc-connection-pool jdbc-app-auth-test-pool
bin/asadmin delete-jdbc-connection-pool jdbc-common-pool
bin/asadmin delete-jdbc-connection-pool jdbc-statement-timeout-test-pool
bin/asadmin delete-jdbc-connection-pool jdbc-max-conn-usage-test-pool
bin/asadmin delete-jdbc-connection-pool jdbc-conn-leak-tracing-test-pool
echo "\n"

echo Deleting files...
rm -f ./SetDerbyAuthentication.class
echo "\n"

echo stopping domain .....
bin/asadmin stop-domain
echo "\n"

