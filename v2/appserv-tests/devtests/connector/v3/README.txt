
dev-tests on connectors and connection pool

--------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
(1) # TO CHECKOUT AND RUN ALL TESTS 
--------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------

Checkout the following :

svn -N co https://svn.java.net/svn/glassfish~svn/trunk/v2/appserv-tests
cd appserv-tests
svn co https://svn.java.net/svn/glassfish~svn/trunk/v2/appserv-tests/config
svn co https://svn.java.net/svn/glassfish~svn/trunk/v2/appserv-tests/lib
svn co https://svn.java.net/svn/glassfish~svn/trunk/v2/appserv-tests/util
svn co https://svn.java.net/svn/glassfish~svn/trunk/v2/appserv-tests/sqetests
svn -N co https://svn.java.net/svn/glassfish~svn/trunk/v2/appserv-tests/devtests
cd devtests
svn co https://svn.java.net/svn/glassfish~svn/trunk/v2/appserv-tests/devtests/connector
cd connector/v3

set environment :
export APS_HOME=<appserv-tests> directory
export S1AS_HOME=<GlassFish Installation> directory
Set CLASSPATH to contain javax.resource-api.jar.
export CLASSPATH=$S1AS_HOME/modules/javax.resource-api.jar:$CLASSPATH
Note: You need install and configure ant manually:
export ANT_HOME=<your ant home directory>
export PATH=$ANT_HOME/bin:$PATH 

$S1AS_HOME/bin/asadmin start-domain domain1
use "ant startDerby" to start derby via appserv-tests (APS_HOME) target so that a stored procedure 
needed by connector test (cci, cci-embedded) is available

To run all tests : ant all
To run with security manager turned on : ant all-with-security-manager

$S1AS_HOME/bin/asadmin stop-domain domain1
cd $APS_HOME/devtests/connector/v3
ant stopDerby


Results will be generated as : $APS_HOME/test_results.html
Console output as : $APS_HOME/devtests/connector/v3/connector.output

--------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
(2) TO CHECKOUT AND RUN A SPECIFIC TEST CASE  
--------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
Checkout the following :

svn -N co https://svn.java.net/svn/glassfish~svn/trunk/v2/appserv-tests
cd appserv-tests
svn co https://svn.java.net/svn/glassfish~svn/trunk/v2/appserv-tests/config
svn co https://svn.java.net/svn/glassfish~svn/trunk/v2/appserv-tests/lib
svn co https://svn.java.net/svn/glassfish~svn/trunk/v2/appserv-tests/util
svn co https://svn.java.net/svn/glassfish~svn/trunk/v2/appserv-tests/sqetests
svn -N co https://svn.java.net/svn/glassfish~svn/trunk/v2/appserv-tests/devtests
cd devtests
svn -N co https://svn.java.net/svn/glassfish~svn/trunk/v2/appserv-tests/devtests/connector
cd connector
svn -N co https://svn.java.net/svn/glassfish~svn/trunk/v2/appserv-tests/devtests/connector/v3
cd v3

eg: there is a test-case by name "embeddedweb"
svn co https://svn.java.net/svn/glassfish~svn/trunk/v2/appserv-tests/devtests/connector/v3/embeddedweb


set environment :
export APS_HOME=<appserv-tests> directory
export S1AS_HOME=<GlassFish Installation> directory
Set CLASSPATH to contain javax.resource.jar.
export CLASSPATH=$S1AS_HOME/modules/javax.resource.jar:$CLASSPATH
Note: You need install and configure ant manually:
export ANT_HOME=<your ant home directory>
export PATH=$ANT_HOME/bin:$PATH 

$S1AS_HOME/bin/asadmin start-domain domain1
use "ant startDerby" to start derby via appserv-tests (APS_HOME) target so that a stored procedure needed by connector test (cci, cci-embedded) is available


cd $APS_HOME/devtests/connector/v3/embeddedweb

ant all


$S1AS_HOME/bin/asadmin stop-domain domain1
cd $APS_HOME/devtests/connector/v3
ant stopDerby

