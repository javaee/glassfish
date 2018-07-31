This test will create 2 jdbc connection pools referring to different shared db services. HR database is located on Derby database whereas Salary database is located on mysql database. 

This test bundles init.sql files as part of application archive. A glassfish-resources.xml file is included in the WEB-INF directory that describes the jdbc-connection-pools/jdbc-resources to be used during provisioning. 

The init.sql files are used by the service provisioning engine to load data into respective databases that are created during provisioning. Application reads data from these database tables and prints the contents onto a servlet. 

A service.properties file is also included in the application archive containing the mapping between service names and database names and init sql files.

Please refer ../../README.txt for more generic guidelines.
