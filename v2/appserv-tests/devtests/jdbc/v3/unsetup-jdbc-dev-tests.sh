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
bin/asadmin delete-jdbc-resource jdbc/jdbc-associate-with-thread-test-resource
#bin/asadmin delete-jdbc-resource jdbc/jdbc-simple-xa-test-resource
bin/asadmin delete-jdbc-resource jdbc/jdbc-simple-xa-test-resource-1
bin/asadmin delete-jdbc-resource jdbc/jdbc-simple-xa-test-resource-2
bin/asadmin delete-jdbc-resource jdbc/jdbc-lazy-assoc-test-resource
bin/asadmin delete-jdbc-resource jdbc/jdbc-lazy-enlist-resource-1
bin/asadmin delete-jdbc-resource jdbc/jdbc-lazy-enlist-resource-2
bin/asadmin delete-jdbc-resource jdbc/double-resource-reference-resource-1
bin/asadmin delete-jdbc-resource jdbc/double-resource-reference-resource-2

echo "\n"

echo deleting jdbc-connection-pools .....
bin/asadmin delete-jdbc-connection-pool jdbc-multiple-user-test-pool
bin/asadmin delete-jdbc-connection-pool jdbc-app-auth-test-pool
bin/asadmin delete-jdbc-connection-pool jdbc-common-pool
bin/asadmin delete-jdbc-connection-pool jdbc-statement-timeout-test-pool
bin/asadmin delete-jdbc-connection-pool jdbc-max-conn-usage-test-pool
bin/asadmin delete-jdbc-connection-pool jdbc-conn-leak-tracing-test-pool
bin/asadmin delete-jdbc-connection-pool jdbc-associate-with-thread-test-pool
#bin/asadmin delete-jdbc-connection-pool jdbc-simple-xa-test-pool
bin/asadmin delete-jdbc-connection-pool jdbc-lazy-assoc-test-pool
bin/asadmin delete-jdbc-connection-pool jdbc-simple-xa-test-pool-1
bin/asadmin delete-jdbc-connection-pool jdbc-simple-xa-test-pool-2
bin/asadmin delete-jdbc-connection-pool jdbc-lazy-enlist-pool-1
bin/asadmin delete-jdbc-connection-pool jdbc-lazy-enlist-pool-2
bin/asadmin delete-jdbc-connection-pool double-resource-reference-pool-1

echo "\n"

echo Deleting files...
rm -f ./SetDerbyAuthentication.class
echo "\n"

echo stopping domain .....
bin/asadmin stop-domain
echo "\n"

