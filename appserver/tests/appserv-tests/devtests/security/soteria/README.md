This is integration devtests for JSR 375 RI(soteria).
The sample apps are taken from [Soteria](https://github.com/javaee-security-spec/soteria) repository.
Follow the below instructions to run the tests.
1. set APS_HOME to appserv-tests dir.
2. set M2_HOME to maven Home.
3. set S1AS_HOME to glassfish installation. The S1AS_HOME should contain glassfish directory.
4. Run mvn clean verify

Known Issue:
1.Aruillian gf container: 
------------
Jun 05, 2017 3:50:22 PM org.jboss.arquillian.container.glassfish.clientutils.GlassFishClientUtil getResponseMap
SEVERE: exit_code: FAILURE, message: An error occurred while processing the request. Please see the server logs for details. [status: SERVER_ERROR reason: Service Unavailable]
[ERROR] Tests run: 1, Failures: 0, Errors: 1, Skipped: 0, Time elapsed: 6.173 s <<< FAILURE! - in org.glassfish.soteria.test.AppMemBasicIT
[ERROR] org.glassfish.soteria.test.AppMemBasicIT  Time elapsed: 6.165 s  <<< ERROR!
com.sun.jersey.api.container.ContainerException: exit_code: FAILURE, message: An error occurred while processing the request. Please see the server logs for details. [status: SERVER_ERROR reason: Service Unavailable]

Jun 05, 2017 3:50:22 PM org.jboss.arquillian.container.glassfish.managed_3_1.GlassFishServerControl$1 run
WARNING: Forcing container shutdown
Stopping container using command: [java, -jar, /media/sameerpandit/WLS/javaEE/tt/glassfish5/glassfish/../glassfish/modules/admin-cli.jar, stop-domain, -t]
------------

Resolve this by running the test with a fresh $S1AS_HOME.
