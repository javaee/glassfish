This test bundles init.sql file as part of application archive. A glassfish-resources.xml file is included in the WEB-INF directory that describes the jdbc-connection-pool/jdbc-resource to be used during provisioning. 

The init.sql file is used by the service provisioning engine to load data into database that is created during provisioning. Application reads data from this database table and prints the contents onto a servlet. 

The Database service is created as a shared service in this test case. 

Please refer ../README.txt for more generic guidelines.
