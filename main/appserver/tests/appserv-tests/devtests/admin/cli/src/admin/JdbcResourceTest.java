/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2010-2013 Oracle and/or its affiliates. All rights reserved.
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
 * @author Jagadish Ramu
 */
public class JdbcResourceTest extends AdminBaseDevTest {

    //TODO test-case for IT 12614 

    public static final String CLUSTER_NAME = "cluster1";
    public static final String INSTANCE1_NAME = "instance1";
    public static final String INSTANCE2_NAME = "instance2";
    public static final String STANDALONE_INSTANCE_NAME = "instance3";

    private static final String RESOURCE_NAME ="resource-1";
    private static final String POOL_NAME="pool-1";

    public static final String[] EXPECTED_TOKENS = {RESOURCE_NAME};

    private static final String CASCADE_OPTION = "--cascade";
    private static final String CASCADE_VALUE = "true";


    private static final String CREATE_RESOURCE_REF = "create-resource-ref";
    private static final String DELETE_RESOURCE_REF = "delete-resource-ref";
    private static final String LIST_RESOURCE_REF = "list-resource-refs";
    private static final String LIST_JNDI_ENTRIES = "list-jndi-entries";

    private static final String DELETE_JDBC_RESOURCE = "delete-jdbc-resource";
    private static final String CREATE_JDBC_RESOURCE = "create-jdbc-resource";
    private static final String CREATE_JDBC_CONNECTION_POOL = "create-jdbc-connection-pool";
    private static final String DELETE_JDBC_CONNECTION_POOL = "delete-jdbc-connection-pool";
    private static final String LIST_JDBC_RESOURCES = "list-jdbc-resources";
    private static final String LIST_JDBC_CONNECTION_POOLS= "list-jdbc-connection-pools";
    private static final String TARGET_OPTION = "--target";
    private static final String POOLNAME_OPTION = "--poolname";
    private static final String SERVER = "server";
    private static final String DOMAIN = "domain";

    private static final String TIMER_POOL = "__TimerPool";
    private static final String DEFAULT_POOL = "DerbyPool";

    private static final String TIMER_RESOURCE = "jdbc/__TimerPool";
    private static final String DEFAULT_RESOURCE = "jdbc/__default";

    public static void main(String[] args) {
        new JdbcResourceTest().runTests();
    }

    @Override
    protected String getTestDescription() {
        return "Unit test for jdbc-resource for various 'target' options";
    }

    @Override
    public void cleanup() {
        try {
            asadmin("stop-local-instance", STANDALONE_INSTANCE_NAME);
            asadmin("delete-local-instance", STANDALONE_INSTANCE_NAME);
            asadmin("stop-local-instance", INSTANCE1_NAME);
            asadmin("stop-local-instance", INSTANCE2_NAME);
            asadmin("stop-cluster", CLUSTER_NAME);
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

        testCreateJdbcConnectionPool();
        testCreateJdbcResourceInServer();
        testListJdbcResourceInServer();
        testListJdbcResourceTargetServer();
        testDeleteJdbcResourceInServer();

        testDeleteDefaultResource();
        testDeleteDefaultResourceRef();

        testCreateJdbcesourceInCluster();
        testListJdbcResourceTargetCluster();
        testListJdbcResourceTargetInstance1();
        testListJdbcResourceTargetInstance2();
        testDeleteJdbcResourceInCluster();


        testCreateJdbcResourceInStandaloneInstance();
        testListJdbcResourceTargetStandaloneInstance();
        testDeleteJdbcResourceInStandaloneInstance();

        testCreateJdbcResourceInDomain();
        testListJdbcResourceTargetDomain();

        testCreateResourceRefInCluster();
        testListResourceRefInCluster();
        //testListJndiEntriesInCluster();
        testListJdbcResourceTargetCluster();
        testDeleteJdbcResourceInDomainExpectFailure();
        testDeleteJdbcResourceExpectFailure(STANDALONE_INSTANCE_NAME);
        testDeleteJdbcResourceExpectFailure(SERVER);
        testDeleteResourceRefInCluster();
        testListJdbcResourceTargetDomain();


        testCreateResourceRefInStandaloneInstance();
        testListResourceRefInStandaloneInstance();
        //testListJndiEntriesInStandaloneInstance();
        testListJdbcResourceTargetStandaloneInstance();
        testDeleteJdbcResourceInDomainExpectFailure();
        testDeleteJdbcResourceExpectFailure(CLUSTER_NAME);
        testDeleteJdbcResourceExpectFailure(SERVER);
        testDeleteResourceRefInStandaloneInstance();
        testListJdbcResourceTargetDomain();

        testCreateResourceRefInServer();
        testListResourceRefInServer();
        testListJndiEntriesInServer();
        testListJdbcResourceInServer();
        testDeleteJdbcResourceInDomainExpectFailure();
        testDeleteJdbcResourceExpectFailure(CLUSTER_NAME);
        testDeleteJdbcResourceExpectFailure(STANDALONE_INSTANCE_NAME);
        testDeleteResourceRefInServer();
        testListJdbcResourceTargetDomain();

        testDeleteJdbcResourceInDomain();
        testDeleteJdbcConnectionPool();

        testCreateJdbcConnectionPool();
        testCreateJdbcResourceInDomain();
        testCreateResourceRefInCluster();
        testCreateResourceRefInStandaloneInstance();
        testCreateResourceRefInServer();
        testDeleteJdbcConnectionPoolWithCascade();

        testListJdbcConnectionPoolsNothingToList();
        testListJdbcResourcesNothingToList();

        cleanup();
        stopDomain();
        stat.printSummary();
    }

    private void testDeleteJdbcResourceExpectFailure(String target){
        String testName = "testDeleteJdbcResourceExpectFailure";
        AsadminReturn result = asadminWithOutput(DELETE_JDBC_RESOURCE,TARGET_OPTION, target, RESOURCE_NAME);
        reportFailureResultStatus(testName, result);
        reportExpectedFailureResult(testName, result, "not referenced");
    }

    private void testDeleteJdbcResourceInDomainExpectFailure(){
        String testName = "testDeleteJdbcResourceInDomainExpectFailure";
        AsadminReturn result = asadminWithOutput(DELETE_JDBC_RESOURCE,TARGET_OPTION, DOMAIN, RESOURCE_NAME);
        reportFailureResultStatus(testName, result);
    }

    private void testDeleteResourceRefInCluster() {
        String testName = "testDeleteResourceRefInCluster";
        AsadminReturn result = asadminWithOutput(DELETE_RESOURCE_REF,TARGET_OPTION, CLUSTER_NAME, RESOURCE_NAME);
        reportResultStatus(testName, result);
        reportExpectedResult(testName, result, "resource-ref resource-1 deleted successfully.");
        //reportExpectedResult(testName, result, INSTANCE2_NAME, INSTANCE1_NAME);
        reportUnexpectedResult(testName, result, STANDALONE_INSTANCE_NAME);
    }

    private void testDeleteResourceRefInStandaloneInstance() {
        String testName = "testDeleteResourceRefInStandaloneInstance";
        AsadminReturn result = asadminWithOutput(DELETE_RESOURCE_REF,TARGET_OPTION, STANDALONE_INSTANCE_NAME, RESOURCE_NAME);
        reportResultStatus(testName, result);
        //reportExpectedResult(testName, result, "Command create-resource-ref executed successfully.");
        reportExpectedResult(testName, result, STANDALONE_INSTANCE_NAME );
        reportUnexpectedResult(testName, result, INSTANCE2_NAME, INSTANCE1_NAME,CLUSTER_NAME);
    }

    private void testDeleteResourceRefInServer() {
        String testName = "testDeleteResourceRefInServer";
        AsadminReturn result = asadminWithOutput(DELETE_RESOURCE_REF,TARGET_OPTION, SERVER, RESOURCE_NAME);
        reportResultStatus(testName, result);
        reportExpectedResult(testName, result, "resource-ref resource-1 deleted successfully.");
        //reportExpectedResult(testName, result, SERVER );
        reportUnexpectedResult(testName, result, STANDALONE_INSTANCE_NAME, INSTANCE2_NAME, INSTANCE1_NAME,CLUSTER_NAME);
    }

    private void testCreateResourceRefInCluster() {
        String testName = "testCreateResourceRefInCluster";
        AsadminReturn result = asadminWithOutput(CREATE_RESOURCE_REF,TARGET_OPTION, CLUSTER_NAME, RESOURCE_NAME);
        reportResultStatus(testName, result);
        reportExpectedResult(testName, result, "resource-ref resource-1 created successfully.");
        //reportExpectedResult(testName, result, INSTANCE2_NAME, INSTANCE1_NAME);
        reportUnexpectedResult(testName, result, STANDALONE_INSTANCE_NAME);
    }

    private void testListResourceRefInCluster() {
        String testName = "testListResourceRefInCluster";
        AsadminReturn result = asadminWithOutput(LIST_RESOURCE_REF,CLUSTER_NAME);
        reportResultStatus(testName, result);
        reportExpectedResult(testName, result, RESOURCE_NAME);
    }

    private void testListJndiEntriesInCluster() {
        String testName = "testListJndiEntriesInCluster";
        AsadminReturn result = asadminWithOutput(LIST_JNDI_ENTRIES,CLUSTER_NAME);
        reportResultStatus(testName, result);
        reportExpectedResult(testName, result, RESOURCE_NAME);
    }

    private void testCreateResourceRefInStandaloneInstance(){
        String testName = "testCreateResourceRefInStandaloneInstance";
        AsadminReturn result = asadminWithOutput(CREATE_RESOURCE_REF,TARGET_OPTION, STANDALONE_INSTANCE_NAME, RESOURCE_NAME);
        reportResultStatus(testName, result);
        //reportExpectedResult(testName, result, "Command create-resource-ref executed successfully.");
        reportExpectedResult(testName, result, STANDALONE_INSTANCE_NAME );
        reportUnexpectedResult(testName, result, INSTANCE2_NAME, INSTANCE1_NAME,CLUSTER_NAME);
    }

    private void testListResourceRefInStandaloneInstance(){
        String testName = "testListResourceRefInStandaloneInstance";
        AsadminReturn result = asadminWithOutput(LIST_RESOURCE_REF,STANDALONE_INSTANCE_NAME);
        reportResultStatus(testName, result);
        reportExpectedResult(testName, result, RESOURCE_NAME);
    }

    private void testListJndiEntriesInStandaloneInstance(){
        String testName = "testListJndiEntriesInStandaloneInstance";
        AsadminReturn result = asadminWithOutput(LIST_JNDI_ENTRIES,STANDALONE_INSTANCE_NAME);
        reportResultStatus(testName, result);
        reportExpectedResult(testName, result, RESOURCE_NAME);
    }

    private void testCreateResourceRefInServer(){
        String testName = "testCreateResourceRefInServer";
        AsadminReturn result = asadminWithOutput(CREATE_RESOURCE_REF,TARGET_OPTION, SERVER, RESOURCE_NAME);
        reportResultStatus(testName, result);
        //reportExpectedResult(testName, result, "Command create-resource-ref executed successfully.");
        reportExpectedResult(testName, result, SERVER );
        reportUnexpectedResult(testName, result, STANDALONE_INSTANCE_NAME, INSTANCE2_NAME, INSTANCE1_NAME,CLUSTER_NAME);
    }

    private void testListResourceRefInServer(){
        String testName = "testListResourceRefInServer";
        AsadminReturn result = asadminWithOutput(LIST_RESOURCE_REF,SERVER);
        reportResultStatus(testName, result);
        reportExpectedResult(testName, result, RESOURCE_NAME);
    }

    private void testListJndiEntriesInServer(){
        String testName = "testListJndiEntriesInServer";
        AsadminReturn result = asadminWithOutput(LIST_JNDI_ENTRIES,SERVER);
        reportResultStatus(testName, result);
        reportExpectedResult(testName, result, RESOURCE_NAME);
    }


    private void testCreateJdbcConnectionPool() {
        String testName = "testCreateJdbcConnectionPool";
        AsadminReturn result = asadminWithOutput(CREATE_JDBC_CONNECTION_POOL,
                "--datasourceclassname", "org.apache.derby.jdbc.ClientDataSource",
                "--restype", "javax.sql.DataSource",
                 POOL_NAME);
        reportResultStatus(testName, result);
        reportExpectedResult(testName, result, "JDBC connection pool pool-1 created successfully");
        //reportExpectedResult(testName, result, STANDALONE_INSTANCE_NAME,
          //      INSTANCE1_NAME, INSTANCE2_NAME);
        reportUnexpectedResult(testName, result, CLUSTER_NAME);
    }

    private void testDeleteJdbcConnectionPoolWithCascade(){
        String testName = "testDeleteJdbcConnectionPool";
        AsadminReturn result = asadminWithOutput(DELETE_JDBC_CONNECTION_POOL,
                CASCADE_OPTION,
                CASCADE_VALUE,
                POOL_NAME);
        reportResultStatus(testName, result);
        //reportExpectedResult(testName, result, "Command delete-jdbc-connection-pool executed successfully.");
        //reportExpectedResult(testName, result, STANDALONE_INSTANCE_NAME,
        //        INSTANCE1_NAME, INSTANCE2_NAME);
        reportUnexpectedResult(testName, result, CLUSTER_NAME);
    }
    private void testDeleteJdbcConnectionPool() {
        String testName = "testDeleteJdbcConnectionPool";
        AsadminReturn result = asadminWithOutput(DELETE_JDBC_CONNECTION_POOL, POOL_NAME);
        reportResultStatus(testName, result);
        //reportExpectedResult(testName, result, "Command delete-jdbc-connection-pool executed successfully.");
        //reportExpectedResult(testName, result, STANDALONE_INSTANCE_NAME,
        //        INSTANCE1_NAME, INSTANCE2_NAME);
        reportUnexpectedResult(testName, result, CLUSTER_NAME);

    }

    private void testDeleteJdbcResourceInDomain() {
        String testName = "testDeleteJdbcResourceInDomain";
        AsadminReturn result = asadminWithOutput(DELETE_JDBC_RESOURCE,TARGET_OPTION, DOMAIN, RESOURCE_NAME);
        reportResultStatus(testName, result);
        reportExpectedResult(testName, result, "JDBC resource resource-1 deleted successfully");
        //reportExpectedResult(testName, result, STANDALONE_INSTANCE_NAME,
        //        INSTANCE1_NAME, INSTANCE2_NAME);
        reportUnexpectedResult(testName, result, CLUSTER_NAME);
    }

    private void testDeleteJdbcResourceInStandaloneInstance() {
        String testName = "testDeleteJdbcResourceInStandaloneInstance";
        AsadminReturn result = asadminWithOutput(DELETE_JDBC_RESOURCE,TARGET_OPTION, STANDALONE_INSTANCE_NAME,
                RESOURCE_NAME);
        reportResultStatus(testName, result);
        reportExpectedResult(testName, result, STANDALONE_INSTANCE_NAME);
        reportExpectedResult(testName, result, "JDBC resource resource-1 deleted successfully");
        //reportExpectedResult(testName, result,
          //      INSTANCE1_NAME, INSTANCE2_NAME);
        reportUnexpectedResult(testName, result, CLUSTER_NAME);
    }

    private void testDeleteJdbcResourceInCluster() {
        String testName = "testDeleteJdbcResourceInCluster";
        AsadminReturn result = asadminWithOutput(DELETE_JDBC_RESOURCE,TARGET_OPTION, CLUSTER_NAME, RESOURCE_NAME);
        reportResultStatus(testName, result);
        //reportExpectedResult(testName, result, INSTANCE1_NAME, INSTANCE2_NAME);
        reportExpectedResult(testName, result, "JDBC resource resource-1 deleted successfully");
        reportUnexpectedResult(testName, result,
                STANDALONE_INSTANCE_NAME);
    }

    private void testDeleteJdbcResourceInServer() {
        String testName = "testDeleteJdbcResourceInServer";
        AsadminReturn result = asadminWithOutput(DELETE_JDBC_RESOURCE,RESOURCE_NAME);
        reportResultStatus(testName, result);
        reportExpectedResult(testName, result);
        reportExpectedResult(testName, result, "JDBC resource resource-1 deleted successfully");
        //reportExpectedResult(testName, result, INSTANCE2_NAME, INSTANCE1_NAME,
        //        STANDALONE_INSTANCE_NAME);
    }

    private void testCreateJdbcResourceInDomain() {
        String testName = "testCreateJdbcResourceInDomain";
        AsadminReturn result = asadminWithOutput(CREATE_JDBC_RESOURCE,
                POOLNAME_OPTION, POOL_NAME,
                TARGET_OPTION, DOMAIN,
                 RESOURCE_NAME);
        reportExpectedResult(testName, result);
        reportExpectedResult(testName, result, "JDBC resource resource-1 created successfully.");
        //reportExpectedResult(testName, result, INSTANCE2_NAME, INSTANCE1_NAME, STANDALONE_INSTANCE_NAME);
        reportUnexpectedResult(testName, result, CLUSTER_NAME);
    }


    private void testCreateJdbcResourceInStandaloneInstance() {
        String testName = "testCreateJdbcResourceInStandaloneInstance";
        AsadminReturn result = asadminWithOutput(CREATE_JDBC_RESOURCE,
                POOLNAME_OPTION, POOL_NAME,
                TARGET_OPTION, STANDALONE_INSTANCE_NAME,
                 RESOURCE_NAME);
        reportExpectedResult(testName, result);
        reportExpectedResult(testName, result, "JDBC resource resource-1 created successfully.");
        //reportExpectedResult(testName, result, INSTANCE2_NAME, INSTANCE1_NAME);
        reportUnexpectedResult(testName, result, CLUSTER_NAME);
    }

    private void testCreateJdbcesourceInCluster() {
        String testName = "testCreateJdbcResourceInCluster";
        AsadminReturn result = asadminWithOutput(CREATE_JDBC_RESOURCE,
                POOLNAME_OPTION, POOL_NAME,
                TARGET_OPTION, CLUSTER_NAME,
                 RESOURCE_NAME);
        reportExpectedResult(testName, result);
        //reportExpectedResult(testName, result, INSTANCE2_NAME, INSTANCE1_NAME);
        reportExpectedResult(testName, result, "JDBC resource resource-1 created successfully.");
        reportUnexpectedResult(testName, result, STANDALONE_INSTANCE_NAME);
    }

    private void testCreateJdbcResourceInServer() {
        String testName = "testCreateJdbcResourceInServer";
        AsadminReturn result = asadminWithOutput(CREATE_JDBC_RESOURCE,
                POOLNAME_OPTION, POOL_NAME,
                TARGET_OPTION, SERVER,
                 RESOURCE_NAME);
        reportExpectedResult(testName, result);
        reportExpectedResult(testName, result, "JDBC resource resource-1 created successfully.");
        //reportExpectedResult(testName, result, STANDALONE_INSTANCE_NAME, INSTANCE1_NAME,
        //        INSTANCE2_NAME);
        reportUnexpectedResult(testName, result, CLUSTER_NAME);
    }

    public void testListJdbcConnectionPoolsNothingToList(){
        String testName = "testListJdbcConnectionPools";
        AsadminReturn result = asadminWithOutput(LIST_JDBC_CONNECTION_POOLS);
        reportResultStatus(testName, result);
        //reportExpectedResult(testName, result, "");
        reportExpectedResult(testName, result, TIMER_POOL, DEFAULT_POOL);
        reportUnexpectedResult(testName, result, POOL_NAME);
    }

    public void testListJdbcResourcesNothingToList(){
        String testName = "testListJdbcResources";
        AsadminReturn result = asadminWithOutput(LIST_JDBC_RESOURCES);
        reportResultStatus(testName, result);
        //reportExpectedResult(testName, result, "");
        reportExpectedResult(testName, result, TIMER_RESOURCE, DEFAULT_RESOURCE);
        reportUnexpectedResult(testName, result, RESOURCE_NAME);

    }

    public void testListJdbcResourceInServer() {
        String testName = "testListJdbcResourceInServer";
        AsadminReturn result = asadminWithOutput(LIST_JDBC_RESOURCES);
        reportResultStatus(testName, result);
        reportExpectedResult(testName, result, TIMER_RESOURCE, DEFAULT_RESOURCE);
        reportUnexpectedResult(testName, result, STANDALONE_INSTANCE_NAME,
                CLUSTER_NAME, INSTANCE1_NAME);
    }


    public void testListJdbcResourceTargetServer() {
        String testName = "testListJdbcResourceTargetServer";
        AsadminReturn result = asadminWithOutput(LIST_JDBC_RESOURCES, SERVER);
        reportResultStatus(testName, result);
        reportExpectedResult(testName, result);
        reportUnexpectedResult(testName, result, STANDALONE_INSTANCE_NAME,
                CLUSTER_NAME, INSTANCE1_NAME);
    }

    public void testListJdbcResourceTargetDomain() {
        String testName = "testListJdbcResourceTargetDomain";

        {
        AsadminReturn result = asadminWithOutput(LIST_JDBC_RESOURCES, DOMAIN);
        reportResultStatus(testName, result);
        reportExpectedResult(testName, result, TIMER_RESOURCE, DEFAULT_RESOURCE);
        }

        {
        AsadminReturn result = asadminWithOutput(LIST_JDBC_RESOURCES);
        reportResultStatus(testName, result);
        //reportExpectedResult(testName, result, "");
        reportExpectedResult(testName, result, TIMER_RESOURCE, DEFAULT_RESOURCE);
        reportUnexpectedResult(testName, result, RESOURCE_NAME);
        }

        {
        AsadminReturn result = asadminWithOutput(LIST_JDBC_RESOURCES, SERVER);
        reportResultStatus(testName, result);
        //reportExpectedResult(testName, result, "");
        reportExpectedResult(testName, result, TIMER_RESOURCE, DEFAULT_RESOURCE);
        reportUnexpectedResult(testName, result, RESOURCE_NAME);
        }

        {
        AsadminReturn result = asadminWithOutput(LIST_JDBC_RESOURCES, CLUSTER_NAME);
        reportResultStatus(testName, result);
        reportExpectedResult(testName, result, "");
/*
        reportExpectedResult(testName, result, TIMER_RESOURCE, DEFAULT_RESOURCE);
        reportUnexpectedResult(testName, result, RESOURCE_NAME);
*/
        }

        {
        AsadminReturn result = asadminWithOutput(LIST_JDBC_RESOURCES, STANDALONE_INSTANCE_NAME);
        reportResultStatus(testName, result);
        reportExpectedResult(testName, result, "");
/*
        reportExpectedResult(testName, result, TIMER_RESOURCE, DEFAULT_RESOURCE);
        reportUnexpectedResult(testName, result, RESOURCE_NAME);
*/
        }
    }

    public void testListJdbcResourceTargetCluster() {
        String testName = "testListJdbcResourceTargetCluster";
        AsadminReturn result = asadminWithOutput(LIST_JDBC_RESOURCES, CLUSTER_NAME);
        reportResultStatus(testName, result);
        reportExpectedResult(testName, result);
        reportUnexpectedResult(testName, result, INSTANCE1_NAME, INSTANCE2_NAME);
        reportUnexpectedResult(testName, result, STANDALONE_INSTANCE_NAME);
    }

    public void testListJdbcResourceTargetInstance1() {
        String testName = "testListJdbcResourceTargetInstance1";
        AsadminReturn result = asadminWithOutput(LIST_JDBC_RESOURCES, INSTANCE1_NAME);
//        reportFailureResultStatus(testName, result);
//        reportExpectedFailureResult(testName, result, "not allowed");
        reportResultStatus(testName, result);
        reportUnexpectedResult(testName, result, STANDALONE_INSTANCE_NAME, INSTANCE2_NAME);
    }

    public void testListJdbcResourceTargetInstance2() {
        String testName = "testListJdbcResourceTargetInstance2";
        AsadminReturn result = asadminWithOutput(LIST_JDBC_RESOURCES, INSTANCE2_NAME);
//        reportFailureResultStatus(testName, result);
//        reportExpectedFailureResult(testName, result, "not allowed");
        reportResultStatus(testName, result);
        reportUnexpectedResult(testName, result, STANDALONE_INSTANCE_NAME, INSTANCE1_NAME );
    }

    public void testListJdbcResourceTargetStandaloneInstance() {
        String testName = "testListJdbcResourceTargetStandaloneInstance";
        AsadminReturn result = asadminWithOutput(LIST_JDBC_RESOURCES, STANDALONE_INSTANCE_NAME);
        reportResultStatus(testName, result);
        reportExpectedResult(testName, result);
        reportExpectedResult(testName, result, STANDALONE_INSTANCE_NAME);
        reportUnexpectedResult(testName, result, INSTANCE1_NAME, INSTANCE2_NAME, CLUSTER_NAME);
    }

    public void testDeleteDefaultResource() {
        String testName = "testDeleteDefaultResource";
        AsadminReturn result = asadminWithOutput(DELETE_JDBC_RESOURCE, DEFAULT_RESOURCE);
        reportFailureResultStatus(testName, result);
        reportExpectedFailureResult(testName, result, "jdbc resource [ jdbc/__default ] cannot be deleted");
    }

    public void testDeleteDefaultResourceRef() {
        String testName = "testDeleteDefaultResourceRef";
        AsadminReturn result = asadminWithOutput(DELETE_RESOURCE_REF, DEFAULT_RESOURCE);
        reportFailureResultStatus(testName, result);
        reportExpectedFailureResult(testName, result, "default JDBC resource ref cannot be deleted");
    }

    private void reportFailureResultStatus(String testName, AsadminReturn result) {
        report(testName, !result.returnValue);
        report(testName, !result.err.isEmpty());
    }


    private void reportResultStatus(String testName, AsadminReturn result) {
        report(testName, result.returnValue);
        report(testName, result.err.isEmpty());
    }

    private void reportExpectedFailureResult(String testName, AsadminReturn result, String... expected) {
        for (String token : expected) {
            report(testName, result.err.contains(token));
        }
    }

    private void reportExpectedResult(String testName, AsadminReturn result, String... expected) {
        if (expected.length == 0) {
            expected = EXPECTED_TOKENS;
        }
        for (String token : expected) {
            report(testName, result.out.contains(token));
        }
    }

    private void reportUnexpectedResult(String testName, AsadminReturn result, String... unexpected) {
        for (String token : unexpected) {
            report(testName, !result.out.contains(token));
        }
    }
}
