This test will create 2 jdbc connection pools referring to different application scoped db services. HR database is located on Derby database whereas Salary database is located on mysql database. 

This test specifies the database name and init.sql explicitly in the glassfish-services.xml for the respective database services. A glassfish-resources.xml file is included in the WEB-INF directory that describes the jdbc-connection-pool(s)/jdbc-resource(s) to be used during provisioning. 

The corresponding init.sql files are used by the service provisioning engine to load data into the respective databases that are created during provisioning. Application reads data from these database tables and prints the contents onto a servlet. 

Works only on KVM as this PaaS application requires Derby and MySQL DB Plugins.

Prerequisities : 
- Make sure both paas.javadbplugin.jar as well as paas.mysqldbplugin.jar are present in the S1AS_HOME/modules directory.
- Execute the command to make one as the default, say 

asadmin register-service-provisioning-engine --type Database --defaultservice=true org.glassfish.paas.javadbplugin.DerbyPlugin

Please refer ../../README.txt for more generic guidelines.
