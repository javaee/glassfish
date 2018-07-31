Steps to run these automated tests:
-----------------------------------

1. Unzip latest version of glassfish.zip and set S1AS_HOME enviroment variable to point to the extracted GlassFish location.

  For example: export S1AS_HOME=/tmp/glassfish5/glassfish

2. Also set PAAS_TESTS_HOME environment variable to point to the location where paas tests are checked out.

 For example: export PAAS_TESTS_HOME=/tmp/main/appserver/tests/paas

3. If you are using Oracle database, Copy the Oracle database plugin jars into $S1AS_HOME/modules.
If you are using MySQL database, copy the MySQL database plugin jars into $S1AS_HOME/modules directory.
Remove $S1AS_HOME/modules/paas.javadbplugin.jar if a Database other than JavaDB is used. Else do the following :

When multiple database plugins are present in the modules directory, to register a particular database plugin as the default service provisioning engine, use the register-service-provisioning-engine command. For example,

asadmin register-service-provisioning-engine --type Database --defaultservice=true org.glassfish.paas.mysqldbplugin.MySQLDBPlugin

or

asadmin register-service-provisioning-engine --type Database --defaultservice=true org.glassfish.paas.javadbplugin.DerbyPlugin

4. [Only for Oracle DB] Copy Oracle jdbc driver (ojdbc14.jar) into $S1AS_HOME/domains/domain1/lib. Ref: http://download.oracle.com/otn/utilities_drivers/jdbc/10205/ojdbc14.jar

5. [Only for MySQL DB] Copy Mysql jdbc driver (mysql-connector-java-5.0.4-bin.jar) into $S1AS_HOME/domains/domain1/lib.

6. [Only for Native mode] Copy downloaded lb.zip under $S1AS_HOME/config directory.
[Only for OVM mode] Download and Copy the necessary OVM related jars into $S1AS_HOME/modules directory.

7. Setup virtualization enviroment for your GlassFish installation. 

   For example, run native_setup.sh to configure native IMS config. Modify kvm_setup.sh to suite your system details and run it. 

This step is optional in which case the service(s) required for this PaaS app will be provisioned in non-virtualized environment.

8. When the load balancer is used, specify the load balancer's port 50080'..eg., -DargLine="-Dhttp.port=50080" 

   GF_EMBEDDED_ENABLE_CLI=true mvn -Dhttps.proxyHost=www-proxy.us.oracle.com -Dhttps.proxyPort=80 clean verify surefire-report:report -DargLine="-Dhttp.port=50080"

   Without lb-plugin just skip the argument part.Deafult port of 28080 will be used.

   GF_EMBEDDED_ENABLE_CLI=true mvn -Dhttps.proxyHost=www-proxy.us.oracle.com -Dhttps.proxyPort=80 clean verify surefire-report:report

   The arguments -Dhttps.proxyHost=www-proxy.us.oracle.com -Dhttps.proxyPort=80 are included for the test 'mq-shared-service-test' to work properly.

