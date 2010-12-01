dev-tests on JDBC and Connection Pool

-------------------------------------------------------------------------------------------------------------
(1) # TO RUN ALL TESTS :
-------------------------------------------------------------------------------------------------------------
Checkout the following : 

svn -N co https://svn.java.net/svn/glassfish~svn/trunk/v2/appserv-tests 
cd appserv-tests
svn co https://svn.java.net/svn/glassfish~svn/trunk/v2/appserv-tests/config
svn co https://svn.java.net/svn/glassfish~svn/trunk/v2/appserv-tests/lib
svn co https://svn.java.net/svn/glassfish~svn/trunk/v2/appserv-tests/util
svn -N co https://svn.java.net/svn/glassfish~svn/trunk/v2/appserv-tests/devtests
cd devtests
svn co https://svn.java.net/svn/glassfish~svn/trunk/v2/appserv-tests/devtests/jdbc

set environment :
export APS_HOME=<appserv-tests> directory
export S1AS_HOME=<GlassFish Installation> directory

$S1AS_HOME/bin/asadmin start-domain domain1
$S1AS_HOME/bin/asadmin start-database

cd $APS_HOME/devtests/jdbc

To run all tests : ant all
To run all oracle tests : ant all-oracle
To run with security manager turned on : ant all-with-security-manager

$S1AS_HOME/bin/asadmin stop-domain domain1
$S1AS_HOME/bin/asadmin stop-database

Results will be generated as : $APS_HOME/JDBCtest_results.html
Console output as : $APS_HOME/devtests/jdbc/jdbc.output


-------------------------------------------------------------------------------------------------------------
(2) TO CHECKOUT AND RUN A PARTICULAR TEST CASE ALONE :
-------------------------------------------------------------------------------------------------------------
svn -N co https://svn.java.net/svn/glassfish~svn/trunk/v2/appserv-tests
cd appserv-tests
svn co https://svn.java.net/svn/glassfish~svn/trunk/v2/appserv-tests/config
svn co https://svn.java.net/svn/glassfish~svn/trunk/v2/appserv-tests/lib
svn co https://svn.java.net/svn/glassfish~svn/trunk/v2/appserv-tests/util
svn -N co https://svn.java.net/svn/glassfish~svn/trunk/v2/appserv-tests/devtests
cd devtests
svn -N co https://svn.java.net/svn/glassfish~svn/trunk/v2/appserv-tests/devtests/jdbc
cd jdbc

eg: there is a test-case by name "statementtimeout"
svn co https://svn.java.net/svn/glassfish~svn/trunk/v2/appserv-tests/devtests/jdbc/statementtimeout

set environment :
export APS_HOME=<appserv-tests> directory
export S1AS_HOME=<GlassFish Installation> directory

$S1AS_HOME/bin/asadmin start-domain domain1
$S1AS_HOME/bin/asadmin start-database

cd $APS_HOME/devtests/jdbc/<TEST> 
eg:
cd $APS_HOME/devtests/jdbc/statementtimeout
ant all

$S1AS_HOME/bin/asadmin stop-domain domain1
$S1AS_HOME/bin/asadmin stop-database
-------------------------------------------------------------------------------------------------------------






