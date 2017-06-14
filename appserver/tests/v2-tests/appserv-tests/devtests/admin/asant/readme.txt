This devtest tests the asant sun-appserv-deploy task.
The build.xml will try to deploy several sample applications from the install root.
To run this test, you need to first update the build.properties.  Following are the properties to be updated:
   sunone.home="this should be the location of the app server installation"
   retrieve.directory="this directory is where you want the client to be retrieved.  This directory must exist."  
   admin.user="this is the admin user name"
   admin.password="this is the admin user password"
   admin.host="this is admin host"
   admin.port="this is admin port number"

After updating the build.properties file, you can execute the build invoking the asant script.  This script is located in ${install.Root}/bin.
