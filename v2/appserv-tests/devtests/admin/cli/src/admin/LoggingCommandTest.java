/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2010-2017 Oracle and/or its affiliates. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License.  You can
 * obtain a copy of the License at
 * https://glassfish.dev.java.net/public/CDDL+GPL_1_1.html
 * or packager/legal/LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 *
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at packager/legal/LICENSE.txt.
 *
 * GPL Classpath Exception:
 * Oracle designates this particular file as subject to the "Classpath"
 * exception as provided by Oracle in the GPL Version 2 section of the License
 * file that accompanied this code.
 *
 * Modifications:
 * If applicable, add the following below the License Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyright [year] [name of copyright owner]"
 *
 * Contributor(s):
 * If you wish your version of this file to be governed by only the CDDL or
 * only the GPL Version 2, indicate your decision by adding "[Contributor]
 * elects to include this software in this distribution under the [CDDL or GPL
 * Version 2] license."  If you don't indicate a single choice of license, a
 * recipient has the option to distribute your version of this file under
 * either the CDDL, the GPL Version 2 or to extend the choice of license to
 * its licensees as provided above.  However, if you add GPL Version 2 code
 * and therefore, elected the GPL Version 2 license, then the option applies
 * only if the new code is made subject to such option by the copyright
 * holder.
 */

package admin;

/*
 * @author Naman Mehta
 */
public class LoggingCommandTest extends AdminBaseDevTest {

    //TODO test-case for IT 12614 

    public static final String CLUSTER_NAME = "cluster1";
    public static final String INSTANCE1_NAME = "instance1";
    public static final String INSTANCE2_NAME = "instance2";
    public static final String STANDALONE_INSTANCE_NAME = "instance3";

    private static final String SET_LOG_LEVEL = "set-log-levels";
    private static final String SET_LOG_ATTRIBUTE = "set-log-attributes";
	private static final String LIST_LOG_ATTRIBUTE = "list-log-attributes";
	private static final String LIST_LOG_LEVEL = "list-log-levels";
	private static final String COLLECT_LOG_FILES = "collect-log-files";
    
	private static final String TARGET_OPTION = "--target";
	private static final String PACKAGE_NAME ="logging.command.test=INFO";

	private static final String ATTRIBUTE_NAME ="java.util.logging.FileHandler.count=10";

	private static final String SERVER = "server";

    public static final String[] EXPECTED_TOKENS = {PACKAGE_NAME};

    public static final String[] EXPECTED_TOKENS1 = {ATTRIBUTE_NAME};

    public static void main(String[] args) {
        new LoggingCommandTest().runTests();
    }

	@Override
    public String getTestName() {
        return "Logging";
    }

    @Override
    protected String getTestDescription() {
        return "Unit test for logging command for various 'target' options";
    }

    @Override
    public void cleanup() {
        try {
            asadmin("stop-local-instance", STANDALONE_INSTANCE_NAME);
            asadmin("stop-local-instance", INSTANCE1_NAME);
            asadmin("stop-local-instance", INSTANCE2_NAME);
            asadmin("stop-cluster", CLUSTER_NAME);
			asadmin("delete-local-instance", STANDALONE_INSTANCE_NAME);            
            asadmin("delete-local-instance", INSTANCE1_NAME);
            asadmin("delete-local-instance", INSTANCE2_NAME);
            asadmin("delete-cluster", CLUSTER_NAME);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void runTests() {
        startDomain();
        
		asadmin("create-cluster", CLUSTER_NAME);

        asadmin("create-local-instance", "--cluster", CLUSTER_NAME,
                /*"--node", "localhost",*/ "--systemproperties",
                "HTTP_LISTENER_PORT=18080:HTTP_SSL_LISTENER_PORT=18181:IIOP_SSL_LISTENER_PORT=13800:IIOP_LISTENER_PORT=13700:JMX_SYSTEM_CONNECTOR_PORT=17676:IIOP_SSL_MUTUALAUTH_PORT=13801:JMS_PROVIDER_PORT=18686:ASADMIN_LISTENER_PORT=14848",
                INSTANCE1_NAME);

        asadmin("create-local-instance", "--cluster", CLUSTER_NAME,
                /*"--node", "localhost", */"--systemproperties",
                "HTTP_LISTENER_PORT=28080:HTTP_SSL_LISTENER_PORT=28181:IIOP_SSL_LISTENER_PORT=23800:IIOP_LISTENER_PORT=23700:JMX_SYSTEM_CONNECTOR_PORT=27676:IIOP_SSL_MUTUALAUTH_PORT=23801:JMS_PROVIDER_PORT=28686:ASADMIN_LISTENER_PORT=24848",
                INSTANCE2_NAME);

        asadmin("create-local-instance",
                /*"--node", "localhost",*/"--systemproperties",
                "HTTP_LISTENER_PORT=28080:HTTP_SSL_LISTENER_PORT=38181:IIOP_SSL_LISTENER_PORT=33800:IIOP_LISTENER_PORT=33700:JMX_SYSTEM_CONNECTOR_PORT=37676:IIOP_SSL_MUTUALAUTH_PORT=33801:JMS_PROVIDER_PORT=38686:ASADMIN_LISTENER_PORT=34848",
                STANDALONE_INSTANCE_NAME);

        asadmin("start-cluster", CLUSTER_NAME);
        asadmin("start-local-instance", STANDALONE_INSTANCE_NAME);

		testSetLogLevelInServer();
        testSetLogLevelInCluster();
        testSetLogLevelInStandaloneInstance();
        testSetLogLevelInServerConfig();
        testSetLogLevelInClusterConfig();
        testSetLogLevelInStandaloneInstanceConfig();
		
		testSetLogAttributeInServer();
        testSetLogAttributeInCluster();
        testSetLogAttributeInStandaloneInstance();
        testSetLogAttributeInServerConfig();
        testSetLogAttributeInClusterConfig();
        testSetLogAttributeInStandaloneInstanceConfig();

		testListLogAttributeInServer();
		testListLogAttributeInServer();
        testListLogAttributeInCluster();
        testListLogAttributeInStandaloneInstance();
        testListLogAttributeInServerConfig();
        testListLogAttributeInClusterConfig();
        testListLogAttributeInStandaloneInstanceConfig();

		testListLogLevleInServer();
		testListLogLevleInServer();
        testListLogLevleInCluster();
        testListLogLevleInStandaloneInstance();
        testListLogLevleInServerConfig();
        testListLogLevleInClusterConfig();
        testListLogLevleInStandaloneInstanceConfig();

		testCollectLogFilesInServer();
		testCollectLogFilesInCluster();	
		testCollectLogFilesStandaloneInstance();

        cleanup();
        stopDomain();
        stat.printSummary();
    }

    private void testSetLogLevelInCluster() {
        String testName = "testSetLogLevelInCluster";
        AsadminReturn result = asadminWithOutput(SET_LOG_LEVEL,TARGET_OPTION, CLUSTER_NAME, PACKAGE_NAME);
        reportResultStatus(testName, result);
        reportExpectedResult(testName, result, INSTANCE2_NAME, INSTANCE1_NAME);
    }

    private void testSetLogLevelInStandaloneInstance(){
        String testName = "testSetLogLevelInStandaloneInstance";
        AsadminReturn result = asadminWithOutput(SET_LOG_LEVEL,TARGET_OPTION, STANDALONE_INSTANCE_NAME, PACKAGE_NAME);
        reportResultStatus(testName, result);
        reportExpectedResult(testName, result, STANDALONE_INSTANCE_NAME );
    }

    private void testSetLogLevelInServer(){
        String testName = "testSetLogLevelInServer";
        AsadminReturn result = asadminWithOutput(SET_LOG_LEVEL,TARGET_OPTION, SERVER, PACKAGE_NAME);
        reportResultStatus(testName, result);
        reportExpectedResult(testName, result, SERVER );
    }

	 private void testSetLogLevelInClusterConfig() {
        String testName = "testSetLogLevelInClusterConfig";
        AsadminReturn result = asadminWithOutput(SET_LOG_LEVEL,TARGET_OPTION, CLUSTER_NAME+"-config", PACKAGE_NAME);
        reportResultStatus(testName, result);
        reportExpectedResult(testName, result, INSTANCE2_NAME, INSTANCE1_NAME);
    }

    private void testSetLogLevelInStandaloneInstanceConfig(){
        String testName = "testSetLogLevelInStandaloneInstanceConfig";
        AsadminReturn result = asadminWithOutput(SET_LOG_LEVEL,TARGET_OPTION, STANDALONE_INSTANCE_NAME+"-config", PACKAGE_NAME);
        reportResultStatus(testName, result);
        reportExpectedResult(testName, result, STANDALONE_INSTANCE_NAME );
    }

    private void testSetLogLevelInServerConfig(){
        String testName = "testSetLogLevelInServerConfig";
        AsadminReturn result = asadminWithOutput(SET_LOG_LEVEL,TARGET_OPTION, SERVER+"-config", PACKAGE_NAME);
        reportResultStatus(testName, result);
        reportExpectedResult(testName, result, SERVER );
    }


	private void testSetLogAttributeInCluster() {
        String testName = "testSetLogAttributeInCluster";
        AsadminReturn result = asadminWithOutput(SET_LOG_ATTRIBUTE,TARGET_OPTION, CLUSTER_NAME, ATTRIBUTE_NAME);
        reportResultStatus(testName, result);
        reportExpectedResult(testName, result, INSTANCE2_NAME, INSTANCE1_NAME);
    }

    private void testSetLogAttributeInStandaloneInstance(){
        String testName = "testSetLogAttributeInStandaloneInstance";
        AsadminReturn result = asadminWithOutput(SET_LOG_ATTRIBUTE,TARGET_OPTION, STANDALONE_INSTANCE_NAME, ATTRIBUTE_NAME);
        reportResultStatus(testName, result);
        reportExpectedResult(testName, result, STANDALONE_INSTANCE_NAME );
    }

    private void testSetLogAttributeInServer(){
        String testName = "testSetLogAttributeInServer";
        AsadminReturn result = asadminWithOutput(SET_LOG_ATTRIBUTE,TARGET_OPTION, SERVER, ATTRIBUTE_NAME);
        reportResultStatus(testName, result);
        reportExpectedResult(testName, result, SERVER );
    }

	 private void testSetLogAttributeInClusterConfig() {
        String testName = "testSetLogAttributeInClusterConfig";
        AsadminReturn result = asadminWithOutput(SET_LOG_ATTRIBUTE,TARGET_OPTION, CLUSTER_NAME+"-config", ATTRIBUTE_NAME);
        reportResultStatus(testName, result);
        reportExpectedResult(testName, result, INSTANCE2_NAME, INSTANCE1_NAME);
    }

    private void testSetLogAttributeInStandaloneInstanceConfig(){
        String testName = "testSetLogAttributeInStandaloneInstanceConfig";
        AsadminReturn result = asadminWithOutput(SET_LOG_ATTRIBUTE,TARGET_OPTION, STANDALONE_INSTANCE_NAME+"-config", ATTRIBUTE_NAME);
        reportResultStatus(testName, result);
        reportExpectedResult(testName, result, STANDALONE_INSTANCE_NAME );
    }

    private void testSetLogAttributeInServerConfig(){
        String testName = "testSetLogAttributeInServerConfig";
        AsadminReturn result = asadminWithOutput(SET_LOG_ATTRIBUTE,TARGET_OPTION, SERVER+"-config", ATTRIBUTE_NAME);
        reportResultStatus(testName, result);
        reportExpectedResult(testName, result, SERVER );
    }

	private void testListLogAttributeInServer(){
        String testName = "testListLogAttributeInServer";
        AsadminReturn result = asadminWithOutput(LIST_LOG_ATTRIBUTE, SERVER);
		reportExpectedResult(testName, result, "log4j.logger.org.hibernate.validator.util.Version");        
    }

	private void testListLogAttributeInCluster(){
        String testName = "testListLogAttributeInCluster";
        AsadminReturn result = asadminWithOutput(LIST_LOG_ATTRIBUTE, CLUSTER_NAME);
		reportExpectedResult(testName, result, "log4j.logger.org.hibernate.validator.util.Version");        
    }

	private void testListLogAttributeInStandaloneInstance(){
        String testName = "testListLogAttributeInStandaloneInstance";
        AsadminReturn result = asadminWithOutput(LIST_LOG_ATTRIBUTE, STANDALONE_INSTANCE_NAME);
		reportExpectedResult(testName, result, "log4j.logger.org.hibernate.validator.util.Version");        
    }

	private void testListLogAttributeInServerConfig(){
        String testName = "testListLogAttributeInServerConfig";
        AsadminReturn result = asadminWithOutput(LIST_LOG_ATTRIBUTE, SERVER+"-config");
		reportExpectedResult(testName, result, "log4j.logger.org.hibernate.validator.util.Version");        
    }

	private void testListLogAttributeInClusterConfig(){
        String testName = "testListLogAttributeInClusterConfig";
        AsadminReturn result = asadminWithOutput(LIST_LOG_ATTRIBUTE, CLUSTER_NAME+"-config");
		reportExpectedResult(testName, result, "log4j.logger.org.hibernate.validator.util.Version");        
    }

	private void testListLogAttributeInStandaloneInstanceConfig(){
        String testName = "testListLogAttributeInStandaloneInstanceConfig";
        AsadminReturn result = asadminWithOutput(LIST_LOG_ATTRIBUTE, STANDALONE_INSTANCE_NAME+"-config");
		reportExpectedResult(testName, result, "log4j.logger.org.hibernate.validator.util.Version");        
    }

	private void testListLogLevleInServer(){
        String testName = "testListLogLevleInServer";
        AsadminReturn result = asadminWithOutput(LIST_LOG_LEVEL, SERVER);
		reportExpectedResult(testName, result, "javax.enterprise.system.ssl.security","java.util.logging.ConsoleHandler");        
    }

	private void testListLogLevleInCluster(){
        String testName = "testListLogLevleInCluster";
        AsadminReturn result = asadminWithOutput(LIST_LOG_LEVEL, CLUSTER_NAME);
		reportExpectedResult(testName, result, "javax.enterprise.system.ssl.security","java.util.logging.ConsoleHandler");        
    }

	private void testListLogLevleInStandaloneInstance(){
        String testName = "testListLogLevleInStandaloneInstance";
        AsadminReturn result = asadminWithOutput(LIST_LOG_LEVEL, STANDALONE_INSTANCE_NAME);
		reportExpectedResult(testName, result, "javax.enterprise.system.ssl.security","java.util.logging.ConsoleHandler");        
    }

	private void testListLogLevleInServerConfig(){
        String testName = "testListLogLevleInServerConfig";
        AsadminReturn result = asadminWithOutput(LIST_LOG_LEVEL, SERVER+"-config");
		reportExpectedResult(testName, result, "javax.enterprise.system.ssl.security","java.util.logging.ConsoleHandler");        
    }

	private void testListLogLevleInClusterConfig(){
        String testName = "testListLogLevleInClusterConfig";
        AsadminReturn result = asadminWithOutput(LIST_LOG_LEVEL, CLUSTER_NAME+"-config");
		reportExpectedResult(testName, result, "javax.enterprise.system.ssl.security","java.util.logging.ConsoleHandler");        
    }

	private void testListLogLevleInStandaloneInstanceConfig(){
        String testName = "testListLogLevleInStandaloneInstanceConfig";
        AsadminReturn result = asadminWithOutput(LIST_LOG_LEVEL, STANDALONE_INSTANCE_NAME+"-config");
		reportExpectedResult(testName, result, "javax.enterprise.system.ssl.security","java.util.logging.ConsoleHandler");        
    }

	private void testCollectLogFilesInServer(){
        String testName = "testCollectLogFilesInServer";
        AsadminReturn result = asadminWithOutput(COLLECT_LOG_FILES,TARGET_OPTION, SERVER);
		reportExpectedResult(testName, result, "Created Zip file under");        
    }

	private void testCollectLogFilesInCluster(){
        String testName = "testCollectLogFilesInCluster";
        AsadminReturn result = asadminWithOutput(COLLECT_LOG_FILES,TARGET_OPTION, CLUSTER_NAME);
		reportExpectedResult(testName, result, "Created Zip file under");        
    }

	private void testCollectLogFilesStandaloneInstance(){
        String testName = "testCollectLogFilesStandaloneInstance";
        AsadminReturn result = asadminWithOutput(COLLECT_LOG_FILES,TARGET_OPTION, STANDALONE_INSTANCE_NAME);
		reportExpectedResult(testName, result, "Created Zip file under");        
    }


    private void reportResultStatus(String testName, AsadminReturn result) {
        report(testName, result.returnValue);
        report(testName, result.err.isEmpty());
    }

    private void reportExpectedResult(String testName, AsadminReturn result, String... expected) {
        if (expected.length == 0) {
            expected = EXPECTED_TOKENS;
        }
        for (String token : expected) {
            report(testName, result.out.contains(token));
        }
    }

	
}
