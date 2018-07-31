This test bundles init.sql and teardown.sql files as part of application archive. A glassfish-resources.xml file is included in the WEB-INF directory that describes the jdbc-connection-pool/jdbc-resource to be used during provisioning. 

The init.sql file is used by the service provisioning engine to load data into database that is created during provisioning. Application reads data from this database table and prints the contents onto a servlet. 

While undeployment happens, before app is undeployed, the service provisioning engine executes the teardown.sql. This test also does  DriverManager.getConnection to check if the database contents have been cleared.

Please refer ../README.txt for more generic guidelines.
