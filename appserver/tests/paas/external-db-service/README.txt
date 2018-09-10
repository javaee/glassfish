This PaaS application creates an external derby database vm and uses this to create an external service. The external service is created with a configuration that requires a database creation.

This test bundles init.sql file as part of application archive. A glassfish-resources.xml file is included in the WEB-INF directory that describes the jdbc-connection-pool/jdbc-resource to be used during provisioning. 

The init.sql file is used by the service provisioning engine to load data into external database service. Application reads data from this database table and prints the contents onto a servlet. 

Works on Derby as glassfish-resources.xml contents are related to Derby database and a derby database is created as an external entity in this application.

Please refer ../README.txt for more generic guidelines.
