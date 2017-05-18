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
 * https://oss.oracle.com/licenses/CDDL+GPL-1.1
 * or LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 *
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at LICENSE.txt.
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
public class ConnectorResourceTest extends AdminBaseDevTest {

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

    private static final String DELETE_CONNECTOR_RESOURCE = "delete-connector-resource";
    private static final String CREATE_CONNECTOR_RESOURCE = "create-connector-resource";
    private static final String CREATE_CONNECTOR_CONNECTION_POOL = "create-connector-connection-pool";
    private static final String DELETE_CONNECTOR_CONNECTION_POOL = "delete-connector-connection-pool";
    private static final String LIST_CONNECTOR_RESOURCES = "list-connector-resources";
    private static final String LIST_CONNECTOR_CONNECTION_POOLS= "list-connector-connection-pools";
    private static final String TARGET_OPTION = "--target";
    private static final String POOLNAME_OPTION = "--poolname";
    private static final String SERVER = "server";
    private static final String DOMAIN = "domain";

    public static void main(String[] args) {
        new ConnectorResourceTest().runTests();
    }

    @Override
    protected String getTestDescription() {
        return "Unit test for connector-resource for various 'target' options";
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
                /*"--node", "localhost",*/ "--systemproperties",
                "HTTP_LISTENER_PORT=28080:HTTP_SSL_LISTENER_PORT=28181:IIOP_SSL_LISTENER_PORT=23800:IIOP_LISTENER_PORT=23700:JMX_SYSTEM_CONNECTOR_PORT=27676:IIOP_SSL_MUTUALAUTH_PORT=23801:JMS_PROVIDER_PORT=28686:ASADMIN_LISTENER_PORT=24848",
                INSTANCE2_NAME);

        asadmin("create-local-instance",
                /*"--node", "localhost",*/"--systemproperties",
                "HTTP_LISTENER_PORT=28080:HTTP_SSL_LISTENER_PORT=38181:IIOP_SSL_LISTENER_PORT=33800:IIOP_LISTENER_PORT=33700:JMX_SYSTEM_CONNECTOR_PORT=37676:IIOP_SSL_MUTUALAUTH_PORT=33801:JMS_PROVIDER_PORT=38686:ASADMIN_LISTENER_PORT=34848",
                STANDALONE_INSTANCE_NAME);

        asadmin("start-cluster", CLUSTER_NAME);
        asadmin("start-local-instance", STANDALONE_INSTANCE_NAME);

        testCreateConnectorConnectionPool();
        testCreateConnectorResourceInServer();
        testListConnectorResourceInServer();
        testListConnectorResourceTargetServer();
        testDeleteConnectorConnectionPoolWithCascade();
        testListConnectorResourcesNothingToList();

        testCreateConnectorConnectionPool();
        testCreateConnectorResourceInServer();
        testListConnectorResourceTargetServer();
        testDeleteConnectorResourceInServer();
        testListConnectorResourcesNothingToList();

        testCreateConnectoResourceInCluster();
        testListConnectorResourceTargetCluster();
        testListConnectorResourceTargetInstance1();
        testListConnectorResourceTargetInstance2();
        testDeleteConnectorConnectionPoolWithCascade();
        testListConnectorResourceTargetClusterNothingToList();
        testListConnectorResourceTargetClusteredInstance1NothingToList();
        testListConnectorResourceTargetClusteredInstance2NothingToList();

        testCreateConnectorConnectionPool();
        testCreateConnectoResourceInCluster();
        testListConnectorResourceTargetCluster();
        testListConnectorResourceTargetInstance1();
        testListConnectorResourceTargetInstance2();
        testDeleteConnectorResourceInCluster();
        testListConnectorResourceTargetClusterNothingToList();
        testListConnectorResourceTargetClusteredInstance1NothingToList();
        testListConnectorResourceTargetClusteredInstance2NothingToList();


        testCreateConnectorResourceInStandaloneInstance();
        testListConnectorResourceTargetStandaloneInstance();
        testDeleteConnectorConnectionPoolWithCascade();
        testListConnectorResourceTargetStandaloneInstanceNothingToList();

        testCreateConnectorConnectionPool();
        testCreateConnectorResourceInStandaloneInstance();
        testListConnectorResourceTargetStandaloneInstance();
        testDeleteConnectorResourceInStandaloneInstance();
        testListConnectorResourceTargetStandaloneInstanceNothingToList();


        testCreateConnectorResourceInDomain();
        testListConnectorResourceTargetDomain();

        testCreateResourceRefInCluster();
        testListResourceRefInCluster();
        //testListJndiEntriesInCluster();
        testListConnectorResourceTargetCluster();
        testDeleteConnectorResourceInDomainExpectFailure();
        testDeleteConnectorResourceExpectFailure(STANDALONE_INSTANCE_NAME);
        testDeleteConnectorResourceExpectFailure(SERVER);
        testDeleteResourceRefInCluster();
        testListConnectorResourceTargetDomain();


        testCreateResourceRefInStandaloneInstance();
        testListResourceRefInStandaloneInstance();
        //testListJndiEntriesInStandaloneInstance();
        testListConnectorResourceTargetStandaloneInstance();
        testDeleteConnectorResourceInDomainExpectFailure();
        testDeleteConnectorResourceExpectFailure(CLUSTER_NAME);
        testDeleteConnectorResourceExpectFailure(SERVER);
        testDeleteResourceRefInStandaloneInstance();
        testListConnectorResourceTargetDomain();

        testCreateResourceRefInServer();
        testListResourceRefInServer();
        testListJndiEntriesInServer();
        testListConnectorResourceInServer();
        testDeleteConnectorResourceInDomainExpectFailure();
        testDeleteConnectorResourceExpectFailure(CLUSTER_NAME);
        testDeleteConnectorResourceExpectFailure(STANDALONE_INSTANCE_NAME);
        testDeleteResourceRefInServer();
        testListConnectorResourceTargetDomain();

        testDeleteConnectorResourceInDomain();
        testDeleteConnectorConnectionPool();

        testCreateConnectorConnectionPool();
        testCreateConnectorResourceInDomain();
        testCreateResourceRefInCluster();
        testCreateResourceRefInStandaloneInstance();
        testCreateResourceRefInServer();
        testDeleteConnectorConnectionPoolWithCascade();

        testListConnectorConnectionPoolsNothingToList();
        testListConnectorResourcesNothingToList();

        testCreateConnectorConnectionPool();
        testCreateConnectorResourceInDomain();
        testCreateResourceRefInCluster();
        testDeleteConnectorConnectionPoolWithCascade();

        testListConnectorConnectionPoolsNothingToList();
        testListConnectorResourcesNothingToList();
        testListConnectorResourceTargetClusterNothingToList();
        testListConnectorResourceTargetClusteredInstance1NothingToList();
        testListConnectorResourceTargetClusteredInstance2NothingToList();
        testListConnectorResourceTargetStandaloneInstanceNothingToList();

        cleanup();
        stopDomain();
        stat.printSummary();
    }

    private void testDeleteConnectorResourceExpectFailure(String target){
        String testName = "testDeleteConnectorResourceExpectFailure";
        AsadminReturn result = asadminWithOutput(DELETE_CONNECTOR_RESOURCE,TARGET_OPTION, target, RESOURCE_NAME);
        reportFailureResultStatus(testName, result);
        reportExpectedFailureResult(testName, result, "not referenced");
    }

    private void testDeleteConnectorResourceInDomainExpectFailure(){
        String testName = "testDeleteConnectorResourceInDomainExpectFailure";
        AsadminReturn result = asadminWithOutput(DELETE_CONNECTOR_RESOURCE,TARGET_OPTION, DOMAIN, RESOURCE_NAME);
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
        reportExpectedResult(testName, result, "resource-ref resource-1 deleted successfully.");
        //reportExpectedResult(testName, result, STANDALONE_INSTANCE_NAME );
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
        reportExpectedResult(testName, result, "resource-ref resource-1 created successfully.");
        //reportExpectedResult(testName, result, STANDALONE_INSTANCE_NAME );
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
        reportExpectedResult(testName, result, "resource-ref resource-1 created successfully.");
        //reportExpectedResult(testName, result, SERVER );
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


    private void testCreateConnectorConnectionPool() {
        String testName = "testCreateConnectorConnectionPool";
        AsadminReturn result = asadminWithOutput(CREATE_CONNECTOR_CONNECTION_POOL,
                "--raname", "jmsra",
                "--connectiondefinition", "javax.jms.QueueConnectionFactory",
                 POOL_NAME);
        reportResultStatus(testName, result);
        reportExpectedResult(testName, result, "Connector connection pool pool-1 created.");
        //reportExpectedResult(testName, result, STANDALONE_INSTANCE_NAME,
          //      INSTANCE1_NAME, INSTANCE2_NAME);
        reportUnexpectedResult(testName, result, CLUSTER_NAME);
    }

    private void testDeleteConnectorConnectionPoolWithCascade(){
        String testName = "testDeleteConnectorConnectionPool";
        AsadminReturn result = asadminWithOutput(DELETE_CONNECTOR_CONNECTION_POOL,
                CASCADE_OPTION,
                CASCADE_VALUE,
                POOL_NAME);
        reportResultStatus(testName, result);
        //reportExpectedResult(testName, result, "Command delete-connector-connection-pool executed successfully.");
        //reportExpectedResult(testName, result, STANDALONE_INSTANCE_NAME,
        //        INSTANCE1_NAME, INSTANCE2_NAME);
        reportUnexpectedResult(testName, result, CLUSTER_NAME);
    }
    private void testDeleteConnectorConnectionPool() {
        String testName = "testDeleteConnectorConnectionPool";
        AsadminReturn result = asadminWithOutput(DELETE_CONNECTOR_CONNECTION_POOL, POOL_NAME);
        reportResultStatus(testName, result);
        //reportExpectedResult(testName, result, "Command delete-connector-connection-pool executed successfully.");
        //reportExpectedResult(testName, result, STANDALONE_INSTANCE_NAME,
        //        INSTANCE1_NAME, INSTANCE2_NAME);
        reportUnexpectedResult(testName, result, CLUSTER_NAME);

    }

    private void testDeleteConnectorResourceInDomain() {
        String testName = "testDeleteConnectorResourceInDomain";
        AsadminReturn result = asadminWithOutput(DELETE_CONNECTOR_RESOURCE,TARGET_OPTION, DOMAIN, RESOURCE_NAME);
        reportResultStatus(testName, result);
        reportExpectedResult(testName, result, "Connector resource resource-1 deleted.");
        //reportExpectedResult(testName, result, STANDALONE_INSTANCE_NAME,
          //      INSTANCE1_NAME, INSTANCE2_NAME);
        reportUnexpectedResult(testName, result, CLUSTER_NAME);
    }

    private void testDeleteConnectorResourceInStandaloneInstance() {
        String testName = "testDeleteConnectorResourceInStandaloneInstance";
        AsadminReturn result = asadminWithOutput(DELETE_CONNECTOR_RESOURCE,TARGET_OPTION, STANDALONE_INSTANCE_NAME,
                RESOURCE_NAME);
        reportResultStatus(testName, result);
        reportExpectedResult(testName, result, STANDALONE_INSTANCE_NAME);
        reportExpectedResult(testName, result, "Connector resource resource-1 deleted.");
        //reportExpectedResult(testName, result,
         //       INSTANCE1_NAME, INSTANCE2_NAME);
        reportUnexpectedResult(testName, result, CLUSTER_NAME);
    }

    private void testDeleteConnectorResourceInCluster() {
        String testName = "testDeleteConnectorResourceInCluster";
        AsadminReturn result = asadminWithOutput(DELETE_CONNECTOR_RESOURCE,TARGET_OPTION, CLUSTER_NAME, RESOURCE_NAME);
        reportResultStatus(testName, result);
        //reportExpectedResult(testName, result, INSTANCE1_NAME, INSTANCE2_NAME);
        reportExpectedResult(testName, result, "Connector resource resource-1 deleted.");
        //reportExpectedResult(testName, result,
          //      STANDALONE_INSTANCE_NAME);
    }

    private void testDeleteConnectorResourceInServer() {
        String testName = "testDeleteConnectorResourceInServer";
        AsadminReturn result = asadminWithOutput(DELETE_CONNECTOR_RESOURCE,RESOURCE_NAME);
        reportResultStatus(testName, result);
        reportExpectedResult(testName, result);
        reportExpectedResult(testName, result, "Connector resource resource-1 deleted.");
        //reportExpectedResult(testName, result, INSTANCE2_NAME, INSTANCE1_NAME,
        //        STANDALONE_INSTANCE_NAME);
    }

    private void testCreateConnectorResourceInDomain() {
        String testName = "testCreateConnectorResourceInDomain";
        AsadminReturn result = asadminWithOutput(CREATE_CONNECTOR_RESOURCE,
                POOLNAME_OPTION, POOL_NAME,
                TARGET_OPTION, DOMAIN,
                 RESOURCE_NAME);
        reportExpectedResult(testName, result);
        reportExpectedResult(testName, result, "Connector resource resource-1 created.");
        //reportExpectedResult(testName, result, INSTANCE2_NAME, INSTANCE1_NAME, STANDALONE_INSTANCE_NAME);
        reportUnexpectedResult(testName, result, CLUSTER_NAME);
    }


    private void testCreateConnectorResourceInStandaloneInstance() {
        String testName = "testCreateConnectorResourceInStandaloneInstance";
        AsadminReturn result = asadminWithOutput(CREATE_CONNECTOR_RESOURCE,
                POOLNAME_OPTION, POOL_NAME,
                TARGET_OPTION, STANDALONE_INSTANCE_NAME,
                 RESOURCE_NAME);
        reportExpectedResult(testName, result);
        reportExpectedResult(testName, result, "Connector resource resource-1 created.");
        //reportExpectedResult(testName, result, INSTANCE2_NAME, INSTANCE1_NAME);
        reportUnexpectedResult(testName, result, CLUSTER_NAME);
    }

    private void testCreateConnectoResourceInCluster() {
        String testName = "testCreateConnectorResourceInCluster";
        AsadminReturn result = asadminWithOutput(CREATE_CONNECTOR_RESOURCE,
                POOLNAME_OPTION, POOL_NAME,
                TARGET_OPTION, CLUSTER_NAME,
                 RESOURCE_NAME);
        reportExpectedResult(testName, result);
        //reportExpectedResult(testName, result, INSTANCE2_NAME, INSTANCE1_NAME);
        reportExpectedResult(testName, result, "Connector resource resource-1 created.");
        reportUnexpectedResult(testName, result, STANDALONE_INSTANCE_NAME);
    }

    private void testCreateConnectorResourceInServer() {
        String testName = "testCreateConnectorResourceInServer";
        AsadminReturn result = asadminWithOutput(CREATE_CONNECTOR_RESOURCE,
                POOLNAME_OPTION, POOL_NAME,
                TARGET_OPTION, SERVER,
                 RESOURCE_NAME);
        reportExpectedResult(testName, result);
        reportExpectedResult(testName, result, "Connector resource resource-1 created.");
        //reportExpectedResult(testName, result, STANDALONE_INSTANCE_NAME, INSTANCE1_NAME,
        //        INSTANCE2_NAME);
        reportUnexpectedResult(testName, result, CLUSTER_NAME);
    }

    public void testListConnectorConnectionPoolsNothingToList(){
        String testName = "testListConnectorConnectionPools";
        AsadminReturn result = asadminWithOutput(LIST_CONNECTOR_CONNECTION_POOLS);
        reportResultStatus(testName, result);
        reportExpectedResult(testName, result, "");
    }

    public void testListConnectorResourcesNothingToList(){
        String testName = "testListConnectorResources";
        AsadminReturn result = asadminWithOutput(LIST_CONNECTOR_RESOURCES);
        reportResultStatus(testName, result);
        reportExpectedResult(testName, result, "");
    }

    public void testListConnectorResourceInServer() {
        String testName = "testListConnectorResourceInServer";
        AsadminReturn result = asadminWithOutput(LIST_CONNECTOR_RESOURCES);
        reportResultStatus(testName, result);
        reportExpectedResult(testName, result);
        reportUnexpectedResult(testName, result, STANDALONE_INSTANCE_NAME,
                CLUSTER_NAME, INSTANCE1_NAME);
    }


    public void testListConnectorResourceTargetServer() {
        String testName = "testListConnectorResourceTargetServer";
        AsadminReturn result = asadminWithOutput(LIST_CONNECTOR_RESOURCES, SERVER);
        reportResultStatus(testName, result);
        reportExpectedResult(testName, result);
        reportUnexpectedResult(testName, result, STANDALONE_INSTANCE_NAME,
                CLUSTER_NAME, INSTANCE1_NAME);
    }

    public void testListConnectorResourceTargetDomain() {
        String testName = "testListConnectorResourceTargetDomain";

        {
        AsadminReturn result = asadminWithOutput(LIST_CONNECTOR_RESOURCES, DOMAIN);
        reportResultStatus(testName, result);
        reportExpectedResult(testName, result);
        }

        {
        AsadminReturn result = asadminWithOutput(LIST_CONNECTOR_RESOURCES);
        reportResultStatus(testName, result);
        reportExpectedResult(testName, result, "");
        }

        {
        AsadminReturn result = asadminWithOutput(LIST_CONNECTOR_RESOURCES, SERVER);
        reportResultStatus(testName, result);
        reportExpectedResult(testName, result, "");
        }

        {
        AsadminReturn result = asadminWithOutput(LIST_CONNECTOR_RESOURCES, CLUSTER_NAME);
        reportResultStatus(testName, result);
        reportExpectedResult(testName, result,"");
        }

        {
        AsadminReturn result = asadminWithOutput(LIST_CONNECTOR_RESOURCES, STANDALONE_INSTANCE_NAME);
        reportResultStatus(testName, result);
        reportExpectedResult(testName, result, "");
        }
    }

    public void testListConnectorResourceTargetCluster() {
        String testName = "testListConnectorResourceTargetCluster";
        AsadminReturn result = asadminWithOutput(LIST_CONNECTOR_RESOURCES, CLUSTER_NAME);
        reportResultStatus(testName, result);
        reportExpectedResult(testName, result);
        reportUnexpectedResult(testName, result, INSTANCE1_NAME, INSTANCE2_NAME);
        reportUnexpectedResult(testName, result, STANDALONE_INSTANCE_NAME);
    }

    public void testListConnectorResourceTargetInstance1() {
        String testName = "testListConnectorResourceTargetInstance1";
        AsadminReturn result = asadminWithOutput(LIST_CONNECTOR_RESOURCES, INSTANCE1_NAME);
//        reportFailureResultStatus(testName, result);
//        reportExpectedFailureResult(testName, result, "not allowed");
        reportResultStatus(testName, result);
        reportUnexpectedResult(testName, result, STANDALONE_INSTANCE_NAME, INSTANCE2_NAME);
    }

    public void testListConnectorResourceTargetInstance2() {
        String testName = "testListConnectorResourceTargetInstance2";
        AsadminReturn result = asadminWithOutput(LIST_CONNECTOR_RESOURCES, INSTANCE2_NAME);
//        reportFailureResultStatus(testName, result);
//        reportExpectedFailureResult(testName, result, "not allowed");
        reportResultStatus(testName, result);
        reportUnexpectedResult(testName, result, STANDALONE_INSTANCE_NAME, INSTANCE1_NAME );
    }

    public void testListConnectorResourceTargetStandaloneInstance() {
        String testName = "testListConnectorResourceTargetStandaloneInstance";
        AsadminReturn result = asadminWithOutput(LIST_CONNECTOR_RESOURCES, STANDALONE_INSTANCE_NAME);
        reportResultStatus(testName, result);
        reportExpectedResult(testName, result);
        reportExpectedResult(testName, result, STANDALONE_INSTANCE_NAME);
        reportUnexpectedResult(testName, result, INSTANCE1_NAME, INSTANCE2_NAME, CLUSTER_NAME);
    }

    public void testListConnectorResourceTargetStandaloneInstanceNothingToList() {
        String testName = "testListConnectorResourceTargetStandaloneInstanceNothingToList";
        AsadminReturn result = asadminWithOutput(LIST_CONNECTOR_RESOURCES, STANDALONE_INSTANCE_NAME);
        reportResultStatus(testName, result);
        reportExpectedResult(testName, result, "");
    }

    public void testListConnectorResourceTargetClusterNothingToList() {
        String testName = "testListConnectorResourceTargetClusterNothingToList";
        AsadminReturn result = asadminWithOutput(LIST_CONNECTOR_RESOURCES, CLUSTER_NAME);
        reportResultStatus(testName, result);
        reportExpectedResult(testName, result, "");
    }

    public void testListConnectorResourceTargetClusteredInstance2NothingToList() {
        String testName = "testListConnectorResourceTargetClusteredInstance2NothingToList";
        AsadminReturn result = asadminWithOutput(LIST_CONNECTOR_RESOURCES, INSTANCE2_NAME);
        reportResultStatus(testName, result);
        reportExpectedResult(testName, result, "");
    }

    public void testListConnectorResourceTargetClusteredInstance1NothingToList() {
        String testName = "testListConnectorResourceTargetClusteredInstance1NothingToList";
        AsadminReturn result = asadminWithOutput(LIST_CONNECTOR_RESOURCES, INSTANCE1_NAME);
        reportResultStatus(testName, result);
        reportExpectedResult(testName, result, "");
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
