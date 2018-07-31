This test will create 2 jdbc connection pools referring to different application scoped db services. HR database is located on Derby database whereas Salary database is located on mysql database. 

This test bundles init.sql file as part of application archive for execution on the different databases. A glassfish-resources.xml file is included in the WEB-INF directory that describes the jdbc-connection-pool(s)/jdbc-resource(s) to be used during provisioning. 

The corresponding init.sql files are used by the service provisioning engine to load data into the respective databases that are created during provisioning. Application reads data from these database tables and prints the contents onto a servlet. 

Also a service.properties file is included in the application archive that maps the init.sql files with the respective databases and also provides mapping between the service names and database names.

Please refer ../../README.txt for more generic guidelines.
