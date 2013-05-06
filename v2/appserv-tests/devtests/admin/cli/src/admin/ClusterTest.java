/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 1997-2011 Oracle and/or its affiliates. All rights reserved.
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

import java.io.File;
import javax.xml.xpath.XPathConstants;

/*
 * Dev test for create/delete/list cluster
 * @author Bhakti Mehta
 */
public class ClusterTest extends AdminBaseDevTest {

    boolean runGMSTests = true;

    public static void main(String[] args) {
        new ClusterTest().runTests();
    }

    private boolean asadmin(int numTries, int sleepBetweenCalls, final String... args) {
        for (int count = 0; count < numTries; ++count) {
            AsadminReturn ret = asadminWithOutput(args);

            if (ret.returnValue)
                return true;

            TestUtils.writeErrorToDebugLog(ret, "asadmin multi-try.  Try # " + (count + 1));
            try {
                Thread.sleep(sleepBetweenCalls);
            }
            catch (Exception e) {
                // ignore
            }
        }
        return false;
    }

    @Override
    protected String getTestDescription() {
        return "Unit test for create/delete/list cluster";
    }

    public void runTests() {
        startDomain();
        checkIfMulticastIsAvailable();

        String xpathExpr = "count" + "(" + "/domain/clusters/cluster" + ")";
        double startingNumberOfClusters = 0.0;
        Object o = evalXPath(xpathExpr, XPathConstants.NUMBER);
        if (o instanceof Double) {
            startingNumberOfClusters = (Double) o;
        }

        // should fail since cluster not created yet
        report("get-health-no-cluster-before", !asadmin("get-health", "cl1"));

        report("create-cluster", asadmin("create-cluster", "cl1"));

        testGetHealthEmptyCluster();

        //create-cluster using existing config
        report("create-cluster-with-config", asadmin("create-cluster",
                "--config", "cl1-config",
                "cl2"));

        //check for duplicates
        report("create-cluster-duplicates", !asadmin("create-cluster", "cl1"));

        //create-cluster using non existing config
        report("create-cluster-nonexistent-config", !asadmin("create-cluster",
                "--config", "junk-config",
                "cl3"));

        //create-cluster using systemproperties
        report("create-cluster-system-props", asadmin("create-cluster",
                "--systemproperties", "foo=bar",
                "cl4"));

        AsadminReturn ret = asadminWithOutput("get", "clusters.cluster.cl4.system-property.foo.name");
        boolean success = ret.outAndErr.indexOf("clusters.cluster.cl4.system-property.foo.name=foo") >= 0;
        report("check-cluster-syspropname", success);

        ret = asadminWithOutput("get", "clusters.cluster.cl4.system-property.foo.value");
        success = ret.outAndErr.indexOf("clusters.cluster.cl4.system-property.foo.value=bar") >= 0;
        report("check-cluster-syspropvalue", success);

        //evaluate using xpath that there are 3 elements in the domain.xml

        o = evalXPath(xpathExpr, XPathConstants.NUMBER);
        if (o instanceof Double) {
            report("evaluation-xpath-create-cluster", o.equals(new Double(3.0 + startingNumberOfClusters)));
        }
        else {
            report("evaluation-xpath-create-cluster", false);
        }

        //list-clusters
        report("list-clusters", asadmin("list-clusters"));
        testDeleteClusterWithInstances();
        testClusterWithObsoleteOptions();
        testEndToEndDemo();
        testListClusters();
        testDynamicReconfigEnabledFlag();
        testGetSetListCommands();
        testRestartRequired();
        testInfraCLIs();
        testGMSSetGetValues();
        cleanup();
        stopDomain();
        stat.printSummary();
    }

    /*
     * Stop instance, check health, restart instance, and check again.
     * This method relies on another for the actual call. It will wait
     * for a bit for a positive result since the GMS notifications
     * may take a few seconds to be sent.
     */
    private void testGetHealthStopRestartInstance(String c, String i) {
        if (!runGMSTests)
            return;

        final String stopped = "stopped";
        final String started = "started";
        final int tries = 6;
        final int sleepSeconds = 10;
        boolean success = false;

        asadmin("stop-local-instance", i);
        for (int x = 0; x < tries; x++) {
            sleep(sleepSeconds);
            printf("Checking instance health for instance %s, expect %s",
                    i, stopped);
            success = checkInstanceHealth(c, i, stopped);
            if (success) {
                break;
            }
        }
        report("get-health-instance-stopped", success);

        success = false;
        asadmin("start-local-instance", i);
        for (int x = 0; x < tries; x++) {
            sleep(sleepSeconds);
            printf("Checking instance health for instance %s, expect %s",
                    i, started);
            success = checkInstanceHealth(c, i, started);
            if (success) {
                break;
            }
        }
        report("get-health-instance-started", success);
    }

    /*
     * Restart the domain, wait, and check health of instances. This
     * method expects that they're all in Started state.
     */
    private void testGetHealthRestartedDomain(String c, String i) {
        if (!runGMSTests)
            return;

        final int tries = 6;
        final int sleepSeconds = 10;
        final String started = "started";
        boolean success = false;

        asadmin("restart-domain");
        for (int x = 0; x < tries; x++) {
            sleep(sleepSeconds);
            success = checkInstanceHealth(c, i, started);
            if (success) {
                break;
            }
        }
        report("get-health-das-restart", success);
    }

    private void testGetHealthInstancesNotStarted(String c) {
        if (!runGMSTests)
            return;

        final String state = "not started";
        String out = asadminWithOutput("get-health", c).outAndErr;
        boolean success = out.indexOf(state) > 0;
        report("get-health-instances-not-running", success);
    }

    /*
     * Given a status and instance in cluster cl1, reports whether or
     * not that status is returned.
     */
    private boolean checkInstanceHealth(String cluster,
            String instanceName, String status) {

        final String expected = String.format("%s %s",
                instanceName, status);
        String out = asadminWithOutput("get-health", cluster).outAndErr;
        return out.indexOf(expected) >= 0;
    }

    private void testGetHealthEmptyCluster() {
        AsadminReturn retVal = asadminWithOutput("get-health", "cl1");
        final String expected = "No instances found for cluster cl1";
        boolean success = retVal.outAndErr.indexOf(expected) >= 0;
        report("get-health-empty-cluster", success);
    }

    /*
     * Use the validate-multicast command to see if multicast is working for the
     * local machine.  It is known not to work on Oracle VPN. If it's not
     * working, skip the GMS tests.
     */
    private void checkIfMulticastIsAvailable() {
        AsadminReturn retVal = asadminWithOutput("validate-multicast", "--timeout", "1");
        final String expected = "Received data from";
        if (retVal.outAndErr.indexOf(expected) == -1) {
            runGMSTests = false;
            System.out.println("WARNING: multicast unavailable, skipping GMS tests.");
        }
    }

    private void testListClusters() {
        final String testName = "issue12249-";

        final String cname = "12249-cl";
        final String iname = "12249-ins";
        report(testName + "create-cl", asadmin("create-cluster", cname));
        for (int i = 0; i < 3; i++) {
            report(testName + "create-li" + i, asadmin("create-local-instance", "--cluster", cname, iname + i));

        }
        report(testName + "list-cl", !isClusterRunning(cname));

        testGetHealthInstancesNotStarted(cname);

        for (int i = 0; i < 3; i++) {
            report(testName + "start-li" + i, asadmin("start-local-instance", iname + i));
        }
        AsadminReturn ret = asadminWithOutput("list-instances", "--long");
        AsadminReturn lc = asadminWithOutput("list-clusters");
        report(testName + "list-cl1", isClusterRunning(cname));

        testGetHealthStopRestartInstance(cname, iname + 1);
        testGetHealthRestartedDomain(cname, iname + 0);

        report(testName + "stop-one", asadmin("stop-local-instance", iname + 1));
        report(testName + "list-cl2", isClusterPartiallyRunning(cname));
        report(testName + "start-one", asadmin("start-local-instance", iname + 1));

        for (int i = 0; i < 3; i++) {
            report(testName + "stop-again" + i, asadmin("stop-local-instance", iname + i));
            report(testName + "delete-li" + i, asadmin("delete-local-instance", iname + i));


        }
        report(testName + "delete-cl", asadmin("delete-cluster", cname));

    }

    private void testClusterWithObsoleteOptions() {
        final String cluster = "obscl";
        final String testName = "obsoleteOpts-";
        //Create the cluster with all of the obsolete options
        //That should not fail

        //Create cluster with obsolete option --haagentport
        report(testName + "create-cl1", asadmin("create-cluster", "--haagentport", "4567", cluster));
        report(testName + "check-cl1", asadmin("get", "clusters.cluster." + cluster));
        report(testName + "delete-cl1", asadmin("delete-cluster", cluster));

        //create the cluster with obsolete opts --hosts
        report(testName + "create-cl2", asadmin("create-cluster", "--hosts", "junk", cluster));
        //asadmin get should  return the cluster
        report(testName + "check-cl2", asadmin("get", "clusters.cluster." + cluster));
        report(testName + "delete-cl2", asadmin("delete-cluster", cluster));

        //create the cluster with obsolete opts   --haadminpassword
        report(testName + "create-cl3", asadmin("create-cluster", "--haadminpassword", "junk", cluster));
        //asadmin get should return the cluster
        report(testName + "check-cl3", asadmin("get", "clusters.cluster." + cluster));
        report(testName + "delete-cl3", asadmin("delete-cluster", cluster));

        //create the cluster with obsolete opts   --haadminpasswordfile
        report(testName + "create-cl4", asadmin("create-cluster", "--haadminpasswordfile", "junk", cluster));
        //asadmin get should return the cluster
        report(testName + "check-cl4", asadmin("get", "clusters.cluster." + cluster));
        report(testName + "delete-cl4", asadmin("delete-cluster", cluster));

        //create the cluster with obsolete opts   --devicesize
        report(testName + "create-cl5", asadmin("create-cluster", "--devicesize", "200", cluster));
        //asadmin get should  return the cluster
        report(testName + "check-cl5", asadmin("get", "clusters.cluster." + cluster));
        report(testName + "delete-cl5", asadmin("delete-cluster", cluster));

        //create the cluster with obsolete opts   --haproperty
        report(testName + "create-cl6", asadmin("create-cluster", "--haproperty", "foo", cluster));
        //asadmin get should  return the cluster
        report(testName + "check-cl6", asadmin("get", "clusters.cluster." + cluster));
        report(testName + "delete-cl6", asadmin("delete-cluster", cluster));

        //create the cluster with obsolete opts   --autohadb
        report(testName + "create-cl7", asadmin("create-cluster", "--autohadb", "foo", cluster));
        //asadmin get should  return the cluster
        report(testName + "check-cl7", asadmin("get", "clusters.cluster." + cluster));
        report(testName + "delete-cl7", asadmin("delete-cluster", cluster));


    }

    private void testDeleteClusterWithInstances() {
        //test for issue 12172
        final String iname = "xyz1";
        final String cluster = "cl7";
        final String testName = "issue-12172-";
        report(testName + "create-cl", asadmin("create-cluster", cluster));
        report(testName + "create-l-i", asadmin("create-local-instance", "--cluster", cluster, iname));
        report(testName + "delete-cl-with-instance", !asadmin("delete-cluster", cluster));
        report(testName + "delete-l-i", asadmin("delete-local-instance", iname));
        //check if there is no server-ref property in the cluster element
        report(testName + "check-serverRef", !asadmin("get", "clusters.cluster." + cluster + ".server-ref." + iname));
        report(testName + "delete-cl-no-ins", asadmin("delete-cluster", cluster));
    }

    /*
     * This is a test based on the MS1 demo of the basic clustering infrastructure.
     * See http://wiki.glassfish.java.net/Wiki.jsp?page=3.1MS1ClusteringDemo
     */
    private void testEndToEndDemo() {
        final String tn = "end-to-end-";

        final String cname = "eec1";
        final String dasurl = "http://localhost:8080/";
        final String i1url = "http://localhost:18080/";
        final String i1name = "eein1-with-a-very-very-very-long-name";
        final String i2url = "http://localhost:28080/";
        final String i2name = "eein2";
        final String dasmurl = "http://localhost:4848/management/domain/";

        // create a cluster and two instances
        report(tn + "create-cluster", asadmin("create-cluster", cname));
        report(tn + "create-local-instance1", asadmin("create-local-instance",
                "--cluster", cname, "--systemproperties",
                "HTTP_LISTENER_PORT=18080:HTTP_SSL_LISTENER_PORT=18181:IIOP_SSL_LISTENER_PORT=13800:"
                + "IIOP_LISTENER_PORT=13700:JMX_SYSTEM_CONNECTOR_PORT=17676:IIOP_SSL_MUTUALAUTH_PORT=13801:"
                + "JMS_PROVIDER_PORT=18686:ASADMIN_LISTENER_PORT=14848", i1name));
        report(tn + "create-local-instance2", asadmin("create-local-instance",
                "--cluster", cname, "--systemproperties",
                "HTTP_LISTENER_PORT=28080:HTTP_SSL_LISTENER_PORT=28181:IIOP_SSL_LISTENER_PORT=23800:"
                + "IIOP_LISTENER_PORT=23700:JMX_SYSTEM_CONNECTOR_PORT=27676:IIOP_SSL_MUTUALAUTH_PORT=23801:"
                + "JMS_PROVIDER_PORT=28686:ASADMIN_LISTENER_PORT=24848", i2name));

        // start the instances
        report(tn + "start-local-instance1", asadmin("start-local-instance", i1name));
        report(tn + "start-local-instance2", asadmin("start-local-instance", i2name));

        // check that the instances are there
        report(tn + "list-instances", asadmin("list-instances"));
        report(tn + "getindex1", matchString("GlassFish Server", getURL(i1url)));
        report(tn + "getindex2", matchString("GlassFish Server", getURL(i2url)));

        // To check fix for 12494 and stop such regressions
        // deploy to default server before deploy to cluster and undeploy
        // after undeploy from cluster
        File dasapp = new File("resources", "servletonly.war");
        report(tn + "DAS-deploy", asadmin("deploy", dasapp.getAbsolutePath()));
        report(tn + "DAS-getapp1", matchString("So what is your lucky number?", getURL(dasurl + "war/servletonly")));
        String x = getURL(dasurl + "war/servletonly");

        // deploy an application to the cluster
        File webapp = new File("resources", "helloworld.war");
        report(tn + "CLUSTER-deploy", asadmin("deploy", "--target", cname, webapp.getAbsolutePath()));

        report(tn + "CLUSTER-getapp1", matchString("Hello", getURL(i1url + "helloworld/hi.jsp")));
        String s1 = getURL(i2url + "helloworld/hi.jsp");
        report(tn + "CLUSTER-getapp2", matchString("Hello", s1));

        report(tn + "CLUSTER-undeploy", asadmin("undeploy", "--target", cname, "helloworld"));
        report(tn + "CLUSTER-get-del-app1", !matchString("Hello", getURL(i1url + "helloworld/hi.jsp")));

        report(tn + "DAS-undeploy", asadmin("undeploy", "servletonly"));
        report(tn + "DAS-get-del-app1", !matchString("So what is your lucky number?", getURL(i1url + "war/servletonly")));

        String s = getURL(dasmurl + "servers/server");
        report(tn + "getREST3a", matchString(i1name, s));
        report(tn + "getREST3b", matchString(i2name, s));
        report(tn + "getREST3c", matchString("server", s));

        // dynamic configuration

        // create several resources
        report(tn + "create-jdbc-connection-pool", asadmin("create-jdbc-connection-pool",
                "--datasourceclassname", "org.apache.derby.jdbc.ClientDataSource",
                "--restype", "javax.sql.XADataSource",
                "--target", cname, "sample_jdbc_pool"));
        report(tn + "create-iiop-listener", asadmin("create-iiop-listener",
                "--target", cname,
                "--listeneraddress", "192.168.1.100",
                "--iiopport", "1400", "sample_iiop_listener"));
        report(tn + "create-connector-connection-pool", asadmin("create-connector-connection-pool",
                "--target", cname,
                "--raname", "jmsra",
                "--connectiondefinition", "javax.jms.QueueConnectionFactory",
                "jms/qConnPool"));

        // delete the resources
        report(tn + "delete-jdbc-connection-pool", asadmin("delete-jdbc-connection-pool",
                "--target", cname, "sample_jdbc_pool"));
        report(tn + "delete-iiop-listener", asadmin("delete-iiop-listener",
                "--target", cname, "sample_iiop_listener"));
        report(tn + "delete-connector-connection-pool", asadmin("delete-connector-connection-pool",
                "--target", cname, "jms/qConnPool"));

        // stop the instances
        report(tn + "stop-local-instance1", asadmin("stop-local-instance", i1name));
        report(tn + "stop-local-instance2", asadmin("stop-local-instance", i2name));

        // delete the instances and the cluster
        report(tn + "delete-local-instance1", asadmin("delete-local-instance", i1name));
        report(tn + "delete-local-instance2", asadmin("delete-local-instance", i2name));
        report(tn + "delete-cluster", asadmin("delete-cluster", cname));

    }

    private void sleep(int n) {
        try {
            // Give instances time to come down
            Thread.sleep(n * 1000);
        }
        catch (InterruptedException e) {
        }

    }
    /*
     * Test for dynamic-reconfig-enabled flag
     */

    private void testDynamicReconfigEnabledFlag() {
        final String tn = "dref-";

        final String cname = "dec1";
        final String dasurl = "http://localhost:8080/";
        final String i1url = "http://localhost:18080/";
        final String i1name = "dein1";
        final String i2url = "http://localhost:28080/";
        final String i2name = "dein2";

        // create a cluster and two instances
        report(tn + "create-cluster", asadmin("create-cluster", cname));
        report(tn + "create-local-instance1", asadmin("create-local-instance",
                "--cluster", cname, "--systemproperties",
                "HTTP_LISTENER_PORT=18080:HTTP_SSL_LISTENER_PORT=18181:IIOP_SSL_LISTENER_PORT=13800:"
                + "IIOP_LISTENER_PORT=13700:JMX_SYSTEM_CONNECTOR_PORT=17676:IIOP_SSL_MUTUALAUTH_PORT=13801:"
                + "JMS_PROVIDER_PORT=18686:ASADMIN_LISTENER_PORT=14848", i1name));
        report(tn + "create-local-instance2", asadmin("create-local-instance",
                "--cluster", cname, "--systemproperties",
                "HTTP_LISTENER_PORT=28080:HTTP_SSL_LISTENER_PORT=28181:IIOP_SSL_LISTENER_PORT=23800:"
                + "IIOP_LISTENER_PORT=23700:JMX_SYSTEM_CONNECTOR_PORT=27676:IIOP_SSL_MUTUALAUTH_PORT=23801:"
                + "JMS_PROVIDER_PORT=28686:ASADMIN_LISTENER_PORT=24848", i2name));

        // start the instances
        report(tn + "start-local-instance1", asadmin("start-local-instance", i1name));
        report(tn + "start-local-instance2", asadmin("start-local-instance", i2name));




        sleep(15);




        // check that the instances are there
        report(tn + "list-instances", asadmin("list-instances"));
        report(tn + "getindex1", matchString("GlassFish Server", getURL(i1url)));
        report(tn + "getindex2", matchString("GlassFish Server", getURL(i2url)));

        // Set dynamic reconfig enabled flag for c1 to false
        report(tn + "set-dyn-recfg-flag", asadmin("set", "configs.config." + cname + "-config.dynamic-reconfiguration-enabled=false"));

        // deploy an application to the cluster
        File webapp = new File("resources", "helloworld.war");
        report(tn + "CLUSTER-deploy", asadmin("deploy", "--target", cname, webapp.getAbsolutePath()));

        // Ensure that the app is not available in the instances
        report(tn + "CLUSTER-getapp1-dynrecfg-disabled-beforerestart", !matchString("Hello", getURL(i1url + "helloworld/hi.jsp")));
        report(tn + "CLUSTER-getapp2-dynrecfg-disabled-beforerestart", !matchString("Hello", getURL(i2url + "helloworld/hi.jsp")));

        // restart the instance 1 and ensure that app is on instance1 only
        report(tn + "stop-local-instance1", asadmin("stop-local-instance", i1name));
        report(tn + "start-local-instance1", asadmin("start-local-instance", i1name));
        report(tn + "CLUSTER-getapp1-dr-disabled-afterrestart", matchString("Hello", getURL(i1url + "helloworld/hi.jsp")));
        report(tn + "CLUSTER-getapp2-dr-disabled-beforerestart", !matchString("Hello", getURL(i2url + "helloworld/hi.jsp")));

        // restart the instance 2 and ensure that app is on both instances
        report(tn + "stop-local-instance2", asadmin("stop-local-instance", i2name));
        report(tn + "start-local-instance2", asadmin("start-local-instance", i2name));
        report(tn + "CLUSTER-getapp1-dr-disabled-afterrestart", matchString("Hello", getURL(i1url + "helloworld/hi.jsp")));
        report(tn + "CLUSTER-getapp2-dr-disabled-afterrestart", matchString("Hello", getURL(i2url + "helloworld/hi.jsp")));

        //Undeploy the app; ensure that the app is still available
        report(tn + "CLUSTER-undeploy", asadmin("undeploy", "--target", cname, "helloworld"));
        report(tn + "CLUSTER-getapp1-dr-disabled-beforerestart", matchString("Hello", getURL(i1url + "helloworld/hi.jsp")));
        report(tn + "CLUSTER-getapp2-dr-disabled-beforerestart", matchString("Hello", getURL(i2url + "helloworld/hi.jsp")));

        // restart the instance 1 and ensure that app is gone on instance1 only
        report(tn + "stop-local-instance1", asadmin("stop-local-instance", i1name));
        report(tn + "start-local-instance1", asadmin("start-local-instance", i1name));
        report(tn + "CLUSTER-getapp1-dr-disabled-afterrestart", !matchString("Hello", getURL(i1url + "helloworld/hi.jsp")));
        report(tn + "CLUSTER-getapp2-dr-disabled-beforerestart", matchString("Hello", getURL(i2url + "helloworld/hi.jsp")));

        // restart the instance 2 and ensure that app is gone on both instances
        report(tn + "stop-local-instance2", asadmin("stop-local-instance", i2name));
        report(tn + "start-local-instance2", asadmin("start-local-instance", i2name));
        report(tn + "CLUSTER-getapp1-dr-disabled-afterrestart", !matchString("Hello", getURL(i1url + "helloworld/hi.jsp")));
        report(tn + "CLUSTER-getapp2-dr-disabled-afterrestart", !matchString("Hello", getURL(i2url + "helloworld/hi.jsp")));

        // Set dynamic reconfig enabled flag for c1 to true
        report(tn + "set-dyn-recfg-flag", asadmin("set", "configs.config." + cname + "-config.dynamic-reconfiguration-enabled=true"));

        // deploy an application to the cluster
        report(tn + "CLUSTER-deploy", asadmin("deploy", "--target", cname, webapp.getAbsolutePath()));

        // Ensure that the app is available in the instances
        report(tn + "CLUSTER-getapp1-dr-enabled", matchString("Hello", getURL(i1url + "helloworld/hi.jsp")));
        report(tn + "CLUSTER-getapp2-dr-enabled", matchString("Hello", getURL(i2url + "helloworld/hi.jsp")));

        //Undeploy the app; ensure that the app is not available
        report(tn + "CLUSTER-undeploy", asadmin("undeploy", "--target", cname, "helloworld"));
        report(tn + "CLUSTER-getapp1-dr-enabled", !matchString("Hello", getURL(i1url + "helloworld/hi.jsp")));
        report(tn + "CLUSTER-getapp2-dr-enabled", !matchString("Hello", getURL(i2url + "helloworld/hi.jsp")));

        // Cleanup
        report(tn + "stop-local-instance1", asadmin("stop-local-instance", i1name));
        report(tn + "stop-local-instance2", asadmin("stop-local-instance", i2name));
        report(tn + "delete-local-instance1", asadmin("delete-local-instance", i1name));
        report(tn + "delete-local-instance2", asadmin("delete-local-instance", i2name));
        report(tn + "delete-cluster", asadmin("delete-cluster", cname));
    }

    /*
     * Test for get, set, list commands
     */
    private void testGetSetListCommands() {
        final String tn = "getsetlist-";

        final String cname = "gslc1";
        final String dasurl = "http://localhost:8080/";
        final String i1url = "http://localhost:18080/";
        final String i1name = "gslin1";
        final String i2url = "http://localhost:28080/";
        final String i2name = "gslin2";
        final String i3url = "http://localhost:38080/";
        final String i3name = "gslin3";

        // create a cluster and two instances
        report(tn + "create-cluster", asadmin("create-cluster", cname));
        report(tn + "create-local-instance1", asadmin("create-local-instance",
                "--cluster", cname, "--systemproperties",
                "HTTP_LISTENER_PORT=18080:HTTP_SSL_LISTENER_PORT=18181:IIOP_SSL_LISTENER_PORT=13800:"
                + "IIOP_LISTENER_PORT=13700:JMX_SYSTEM_CONNECTOR_PORT=17676:IIOP_SSL_MUTUALAUTH_PORT=13801:"
                + "JMS_PROVIDER_PORT=18686:ASADMIN_LISTENER_PORT=14848", i1name));
        report(tn + "create-local-instance2", asadmin("create-local-instance",
                "--cluster", cname, "--systemproperties",
                "HTTP_LISTENER_PORT=28080:HTTP_SSL_LISTENER_PORT=28181:IIOP_SSL_LISTENER_PORT=23800:"
                + "IIOP_LISTENER_PORT=23700:JMX_SYSTEM_CONNECTOR_PORT=27676:IIOP_SSL_MUTUALAUTH_PORT=23801:"
                + "JMS_PROVIDER_PORT=28686:ASADMIN_LISTENER_PORT=24848", i2name));
        report(tn + "create-local-instance3", asadmin("create-local-instance",
                "--systemproperties",
                "HTTP_LISTENER_PORT=38080:HTTP_SSL_LISTENER_PORT=38181:IIOP_SSL_LISTENER_PORT=33800:"
                + "IIOP_LISTENER_PORT=33700:JMX_SYSTEM_CONNECTOR_PORT=37676:IIOP_SSL_MUTUALAUTH_PORT=33801:"
                + "JMS_PROVIDER_PORT=38686:ASADMIN_LISTENER_PORT=34848", i3name));

        // start the instances
        report(tn + "start-cluster", asadmin("start-cluster", cname));
        report(tn + "start-local-instance3", asadmin("start-local-instance", i3name));


        sleep(10);




        // check that the instances are there
        report(tn + "getindex1", matchString("GlassFish Server", getURL(i1url)));
        report(tn + "getindex2", matchString("GlassFish Server", getURL(i2url)));
        report(tn + "getindex3", matchString("GlassFish Server", getURL(i3url)));

        // check if list lists all configs created
        AsadminReturn ret = asadminWithOutput("list", "configs.config");
        boolean success = ret.outAndErr.indexOf("configs.config." + cname + "-config") >= 0;
        report("list-cluster-config", success);
        success = ret.outAndErr.indexOf("configs.config." + i3name + "-config") >= 0;
        report("list-instance-config", success);
        success = ret.outAndErr.indexOf("configs.config." + i2name + "-config") < 0;
        report("list-instance-config-error-test", success);

        // Check is get/set gets replicated
        ret = asadminWithOutput("get", "clusters.cluster." + cname);
        success = ret.outAndErr.indexOf("clusters.cluster." + cname + ".gms-enabled=true") >= 0;
        report("get-cluster-gms-attr", success);

        ret = asadminWithOutput("set", "clusters.cluster." + cname + ".gms-enabled=false");
        ret = asadminWithOutput("get", "clusters.cluster." + cname + ".gms-enabled");
        success = ret.outAndErr.indexOf("clusters.cluster." + cname + ".gms-enabled=false") >= 0;
        report("get-cluster-gms-attr-after-reset", success);

        ret = asadminWithOutput("get", "clusters.cluster." + cname + ".gms-enabled");
        success = ret.outAndErr.indexOf("clusters.cluster." + cname + ".gms-enabled=false") >= 0;
        report("get-target-gms-attr-after-reset1", success);

        // Test fix for 12880 : short path names in get/set
        File webapp = new File("resources", "helloworld.war");
        report(tn + "in3-deploy", asadmin("deploy", "--target", i3name, webapp.getAbsolutePath()));
        ret = asadminWithOutput("get", i3name + ".application-ref.*");
        success = ret.outAndErr.indexOf(i3name + ".application-ref.helloworld.enabled=true") >= 0;
        report("get-enable-attr-before-reset", success);
        ret = asadminWithOutput("set", i3name + ".application-ref.helloworld.enabled=false");
        ret = asadminWithOutput("get", i3name + ".application-ref.*");
        success = ret.outAndErr.indexOf(i3name + ".application-ref.helloworld.enabled=false") >= 0;
        report("get-enable-attr-after-reset1", success);
        ret = asadminWithOutput("set", i3name + ".application-ref.helloworld.enabled=true");
        ret = asadminWithOutput("get", "servers.server." + i3name + ".application-ref.*");
        success = ret.outAndErr.indexOf("servers.server." + i3name + ".application-ref.helloworld.enabled=true") >= 0;
        report("get-enable-attr-before-reset2", success);
        report(tn + "in3-undeploy", asadmin("undeploy", "--target", i3name, "helloworld"));

        // Test Get, Set, List for monitoring option
        ret = asadminWithOutput("list", "-m", i3name + ".*");
        success = ret.outAndErr.indexOf(i3name + ".jvm") < 0;
        report("list-without-enabling-monitoring", success);
        ret = asadminWithOutput("set", i3name + "-config.monitoring-service.module-monitoring-levels.jvm=HIGH");
        ret = asadminWithOutput("list", "-m", i3name + ".*");
        success = ret.outAndErr.indexOf(i3name + ".jvm") >= 0;
        report("list-after-enabling-monitoring", success);
        ret = asadminWithOutput("list", "-m", i2name + ".*");
        success = ret.outAndErr.indexOf(i2name + ".jvm") < 0;
        report("list-without-enabling-monitoring-in2", success);
        ret = asadminWithOutput("get", i3name + "-config.monitoring-service.module-monitoring-levels.jvm");
        success = ret.outAndErr.indexOf(i3name + "-config.monitoring-service.module-monitoring-levels.jvm=HIGH") >= 0;
        report("get-after-enabling-monitoring-without-m", success);

        // Cleanup
        report(tn + "stop-local-instance1", asadmin("stop-local-instance", i1name));
        report(tn + "stop-local-instance2", asadmin("stop-local-instance", i2name));
        report(tn + "stop-local-instance3", asadmin("stop-local-instance", i3name));
        report(tn + "delete-local-instance1", asadmin("delete-local-instance", i1name));
        report(tn + "delete-local-instance2", asadmin("delete-local-instance", i2name));
        report(tn + "delete-local-instance3", asadmin("delete-local-instance", i3name));
        report(tn + "delete-cluster", asadmin("delete-cluster", cname));
    }

    /*
     * Test for restart required
     */
    private void testRestartRequired() {
        final String tn = "resreq-";

        final String cname = "resreqcluster";
        final String i1name = "resreq1";
        final String i2name = "resreq2";
        final String i3name = "resreq3";

        // create a cluster and two instances
        report(tn + "create-cluster", asadmin("create-cluster", cname));
        report(tn + "create-local-instance1", asadmin("create-local-instance",
                "--cluster", cname, i1name));
        report(tn + "create-local-instance2", asadmin("create-local-instance",
                "--cluster", cname, i2name));
        report(tn + "create-local-instance3", asadmin("create-local-instance",
                i3name));

        // start the instances
        report(tn + "start-local-instance1", asadmin("start-local-instance", i1name));
        report(tn + "start-local-instance2", asadmin("start-local-instance", i2name));
        report(tn + "start-local-instance3", asadmin("start-local-instance", i3name));

        // check that the instances are there
        AsadminReturn ret = asadminWithOutput("list-instances");
        boolean success = checkListInstancesOutputIfRunning(ret.outAndErr, i1name);
        report(tn + "test-in1-running", success);
        success = checkListInstancesOutputIfRunning(ret.outAndErr, i2name);
        report(tn + "test-in2-running", success);
        success = checkListInstancesOutputIfRunning(ret.outAndErr, i3name);
        report(tn + "test-in3-running", success);

        // Set dynamic reconfig enabled flag for c1 to false
        report(tn + "set-dyn-recfg-flag", asadmin("set", "configs.config." + cname + "-config.dynamic-reconfiguration-enabled=false"));

        // Execute command
        ret = asadminWithOutput("create-jdbc-connection-pool",
                "--datasourceclassname",
                "org.apache.derby.jdbc.ClientDataSource", "--restype",
                "javax.sql.XADataSource", "--target", cname, "testPool");
        success = ret.outAndErr.indexOf("WARNING") >= 0;
        report(tn + "test-dyn-recfg-disabled", success);

        // Test instance states
        ret = asadminWithOutput("list-instances", i1name);
        success = ret.outAndErr.indexOf("[pending config changes are:") >= 0;
        report(tn + "test-in1-requires-restart", success);
        ret = asadminWithOutput("list-instances", i2name);
        success = ret.outAndErr.indexOf("[pending config changes are:") >= 0;
        report(tn + "test-in2-requires-restart", success);
        ret = asadminWithOutput("list-instances", i3name);
        success = checkListInstancesOutputIfRunning(ret.outAndErr, i3name);
        report(tn + "test-in3-does-not-require-restart", success);

        // Test failed command being appended
        // Execute command
        ret = asadminWithOutput("create-jdbc-connection-pool",
                "--datasourceclassname",
                "org.apache.derby.jdbc.ClientDataSource", "--restype",
                "javax.sql.XADataSource", "--target", cname, "testPool2");
        success = ret.outAndErr.indexOf("WARNING") >= 0;
        report(tn + "test-dyn-recfg-disabled", success);

        // Test instance states
        ret = asadminWithOutput("list-instances", i1name);
        success = ret.outAndErr.indexOf("create-jdbc-connection-pool testPool;") >= 0;
        report(tn + "test-in1-has-1st-failed-cmd", success);
        success = ret.outAndErr.indexOf("create-jdbc-connection-pool testPool2") >= 0;
        report(tn + "test-in1-has-2nd-failed-cmd", success);
        ret = asadminWithOutput("list-instances", i2name);
        success = ret.outAndErr.indexOf("create-jdbc-connection-pool testPool;") >= 0;
        report(tn + "test-in2-has-1st-failed-cmd", success);
        success = ret.outAndErr.indexOf("create-jdbc-connection-pool testPool2") >= 0;
        report(tn + "test-in2-has-2nd-failed-cmd", success);
        ret = asadminWithOutput("list-instances", i3name);
        success = checkListInstancesOutputIfRunning(ret.outAndErr, i3name);
        report(tn + "test-in3-does-not-require-restart", success);

        // Test that restart-required persisted across DAS restarts
        asadminWithOutput("restart-domain");
        // Test instance states
        ret = asadminWithOutput("list-instances", i1name);
        success = ret.outAndErr.indexOf("create-jdbc-connection-pool testPool;") >= 0;
        report(tn + "test-in1-state-after-restart", success);
        success = ret.outAndErr.indexOf("create-jdbc-connection-pool testPool2") >= 0;
        report(tn + "test-in1-state-after-restart", success);
        ret = asadminWithOutput("list-instances", i2name);
        success = ret.outAndErr.indexOf("create-jdbc-connection-pool testPool;") >= 0;
        report(tn + "test-in2-state-after-restart", success);
        success = ret.outAndErr.indexOf("create-jdbc-connection-pool testPool2") >= 0;
        report(tn + "test-in2-state-after-restart", success);
        ret = asadminWithOutput("list-instances", i3name);
        success = checkListInstancesOutputIfRunning(ret.outAndErr, i3name);
        report(tn + "test-in3-state-after-restart", success);

        // Test that instance restart, clears restart-required flag
        report(tn + "stop-local-instance1", asadmin("stop-local-instance", i1name));
        report(tn + "stop-local-instance2", asadmin("stop-local-instance", i2name));

        // the following line occasionally fails in Windows.  Do multi-try and gather
        // diagnostic info...
        report(tn + "start-local-instance1", asadmin(5, 10000, "start-local-instance", i1name));
        report(tn + "start-local-instance2", asadmin(5, 10000, "start-local-instance", i2name));
        // Test instance states
        ret = asadminWithOutput("list-instances");
        success = checkListInstancesOutputIfRunning(ret.outAndErr, i1name);
        report(tn + "test-in1-running", success);
        success = checkListInstancesOutputIfRunning(ret.outAndErr, i2name);
        report(tn + "test-in2-running", success);

        // Cleanup
        report(tn + "set-dyn-recfg-flag", asadmin("set", "configs.config." + cname + "-config.dynamic-reconfiguration-enabled=true"));
        asadmin("delete-jdbc-connection-pool", "testPool");
        asadmin("delete-jdbc-connection-pool", "testPool2");
        report(tn + "stop-local-instance1", asadmin("stop-local-instance", i1name));
        report(tn + "stop-local-instance2", asadmin("stop-local-instance", i2name));
        report(tn + "stop-local-instance3", asadmin("stop-local-instance", i3name));
        report(tn + "delete-local-instance1", asadmin("delete-local-instance", i1name));
        report(tn + "delete-local-instance2", asadmin("delete-local-instance", i2name));
        report(tn + "delete-local-instance3", asadmin("delete-local-instance", i3name));
        report(tn + "delete-cluster", asadmin("delete-cluster", cname));
    }

    /*
     * Test for infra commands *system-properties, *profiler, *jvmoptions
     */
    private void testInfraCLIs() {
        final String tn = "infracli-";

        final String cname = "infrac1";
        final String i1name = "infrai1";
        final String i2name = "infrai2";
        final String i3name = "infrai3";

        // create a cluster and two instances
        report(tn + "create-cluster", asadmin("create-cluster", cname));
        report(tn + "create-local-instance1", asadmin("create-local-instance",
                "--cluster", cname, i1name));
        report(tn + "create-local-instance2", asadmin("create-local-instance",
                "--cluster", cname, i2name));
        report(tn + "create-local-instance2", asadmin("create-local-instance",
                i3name));

        // start the instances
        report(tn + "start-local-instance1", asadmin("start-local-instance", i1name));
        report(tn + "start-local-instance2", asadmin("start-local-instance", i2name));
        report(tn + "start-local-instance2", asadmin("start-local-instance", i3name));

        // create jvm-options command
        report(tn + "create-jvm-option-das", asadmin("create-jvm-options", "-Ddas=server"));
        report(tn + "create-jvm-option-cluster", asadmin("create-jvm-options",
                "--target", cname, "-Dcl=in1in2"));
        report(tn + "create-jvm-option-instance", asadmin("create-jvm-options",
                "--target", i3name, "-Din3=in3"));

        // Test list output
        AsadminReturn ret = asadminWithOutput("list-jvm-options");
        boolean success = ret.outAndErr.indexOf("-Ddas=server") >= 0;
        report(tn + "test-das-has-das-jvm-option", success);
        success = ret.outAndErr.indexOf("-Dcl=in1in2") < 0;
        report(tn + "test-das-has-no-cl-jvm-option", success);
        success = ret.outAndErr.indexOf("-Din3=in3") < 0;
        report(tn + "test-das-has-no-in3-jvm-option", success);
        ret = asadminWithOutput("list-jvm-options", "--target", cname);
        success = ret.outAndErr.indexOf("-Ddas=server") < 0;
        report(tn + "test-cluster-has-no-das-jvm-option", success);
        success = ret.outAndErr.indexOf("-Dcl=in1in2") >= 0;
        report(tn + "test-cluster-has-cl-jvm-option", success);
        success = ret.outAndErr.indexOf("-Din3=in3") < 0;
        report(tn + "test-cluster-has-no-in3-jvm-option", success);
        ret = asadminWithOutput("list-jvm-options", "--target", i3name);
        success = ret.outAndErr.indexOf("-Ddas=server") < 0;
        report(tn + "test-instance-has-no-das-jvm-option", success);
        success = ret.outAndErr.indexOf("-Dcl=in1in2") < 0;
        report(tn + "test-instance-has-no-cl-jvm-option", success);
        success = ret.outAndErr.indexOf("-Din3=in3") >= 0;
        report(tn + "test-instance-has-in3-jvm-option", success);

        // delete jvm-options command
        report(tn + "delete-jvm-option-das", asadmin("delete-jvm-options", "-Ddas=server"));
        report(tn + "delete-jvm-option-cluster", asadmin("delete-jvm-options",
                "--target", cname, "-Dcl=in1in2"));
        report(tn + "delete-jvm-option-instance", asadmin("delete-jvm-options",
                "--target", i3name, "-Din3=in3"));

        // Test list output
        ret = asadminWithOutput("list-jvm-options");
        success = ret.outAndErr.indexOf("-Ddas=server") < 0;
        report(tn + "test-das-has-no-das-jvm-option", success);
        success = ret.outAndErr.indexOf("-Dcl=in1in2") < 0;
        report(tn + "test-das-has-no-cl-jvm-option", success);
        success = ret.outAndErr.indexOf("-Din3=in3") < 0;
        report(tn + "test-das-has-no-in3-jvm-option", success);
        ret = asadminWithOutput("list-jvm-options", "--target", cname);
        success = ret.outAndErr.indexOf("-Ddas=server") < 0;
        report(tn + "test-cluster-has-no-das-jvm-option", success);
        success = ret.outAndErr.indexOf("-Dcl=in1in2") < 0;
        report(tn + "test-cluster-has-no-cl-jvm-option", success);
        success = ret.outAndErr.indexOf("-Din3=in3") < 0;
        report(tn + "test-cluster-has-no-in3-jvm-option", success);
        ret = asadminWithOutput("list-jvm-options", "--target", i3name);
        success = ret.outAndErr.indexOf("-Ddas=server") < 0;
        report(tn + "test-instance-has-no-das-jvm-option", success);
        success = ret.outAndErr.indexOf("-Dcl=in1in2") < 0;
        report(tn + "test-instance-has-no-cl-jvm-option", success);
        success = ret.outAndErr.indexOf("-Din3=in3") < 0;
        report(tn + "test-instance-has-no-in3-jvm-option", success);

        // Test create-jvm fails on clustered instances
        report(tn + "create-jvm-option-clustered-instance", !asadmin("create-jvm-options", "--target", i2name, "-Dcl=in1in2"));

        // create-system-properties command
        report(tn + "create-system-properties-das", asadmin("create-system-properties", "das=server"));
        report(tn + "create-system-properties-cluster", asadmin("create-system-properties", "--target", cname, "cl=in1in2"));
        report(tn + "create-system-properties-clusteredins", asadmin("create-system-properties", "--target", i2name, "clins=in2"));
        report(tn + "create-system-properties-instance", asadmin("create-system-properties", "--target", i3name, "in3=in3"));

        // Test list output
        ret = asadminWithOutput("list-system-properties");
        success = ret.outAndErr.indexOf("das=server") >= 0;
        report(tn + "test-das-has-das-system-properties", success);
        success = ret.outAndErr.indexOf("cl=in1in2") < 0;
        report(tn + "test-das-has-no-cl-system-properties", success);
        success = ret.outAndErr.indexOf("clins=in2") < 0;
        report(tn + "test-das-has-no-clins-system-properties", success);
        success = ret.outAndErr.indexOf("in3=in3") < 0;
        report(tn + "test-das-has-no-in3-system-properties", success);
        ret = asadminWithOutput("list-system-properties", cname);
        success = ret.outAndErr.indexOf("das=server") < 0;
        report(tn + "test-cluster-has-no-das-system-properties", success);
        success = ret.outAndErr.indexOf("cl=in1in2") >= 0;
        report(tn + "test-cluster-has-cl-system-properties", success);
        success = ret.outAndErr.indexOf("clins=in2") < 0;
        report(tn + "test-das-has-no-clins-system-properties", success);
        success = ret.outAndErr.indexOf("in3=in3") < 0;
        report(tn + "test-cluster-has-no-in3-system-properties", success);
        ret = asadminWithOutput("list-system-properties", i3name);
        success = ret.outAndErr.indexOf("das=server") < 0;
        report(tn + "test-instance-has-no-das-system-properties", success);
        success = ret.outAndErr.indexOf("cl=in1in2") < 0;
        report(tn + "test-instance-has-no-cl-system-properties", success);
        success = ret.outAndErr.indexOf("clins=in2") < 0;
        report(tn + "test-das-has-no-clins-system-properties", success);
        success = ret.outAndErr.indexOf("in3=in3") >= 0;
        report(tn + "test-instance-has-in3-system-properties", success);
        ret = asadminWithOutput("list-system-properties", i2name);
        success = ret.outAndErr.indexOf("das=server") < 0;
        report(tn + "test-instance-has-no-das-system-properties", success);
        success = ret.outAndErr.indexOf("cl=in1in2") < 0;
        report(tn + "test-instance-has-no-cl-system-properties", success);
        success = ret.outAndErr.indexOf("clins=in2") >= 0;
        report(tn + "test-das-has-clins-system-properties", success);
        success = ret.outAndErr.indexOf("in3=in3") < 0;
        report(tn + "test-instance-has-in3-system-properties", success);


        // delete system-properties command
        report(tn + "delete-system-property-das", asadmin("delete-system-property", "das"));
        report(tn + "delete-system-property-cluster", asadmin("delete-system-property", "--target", cname, "cl"));
        report(tn + "delete-system-property-instance", asadmin("delete-system-property", "--target", i3name, "in3"));
        report(tn + "delete-system-property-instance", asadmin("delete-system-property", "--target", i2name, "clins"));

        // Test list output
        ret = asadminWithOutput("list-system-properties");
        success = ret.outAndErr.indexOf("das=server") < 0;
        report(tn + "test-das-has-no-das-system-properties", success);
        success = ret.outAndErr.indexOf("cl=in1in2") < 0;
        report(tn + "test-das-has-no-cl-system-properties", success);
        success = ret.outAndErr.indexOf("clins=in2") < 0;
        report(tn + "test-das-has-no-clins-system-properties", success);
        success = ret.outAndErr.indexOf("in3=in3") < 0;
        report(tn + "test-das-has-no-in3-system-properties", success);
        ret = asadminWithOutput("list-system-properties", "--target", cname);
        success = ret.outAndErr.indexOf("das=server") < 0;
        report(tn + "test-cluster-has-no-das-system-properties", success);
        success = ret.outAndErr.indexOf("cl=in1in2") < 0;
        report(tn + "test-cluster-has-no-cl-system-properties", success);
        success = ret.outAndErr.indexOf("clins=in2") < 0;
        report(tn + "test-das-has-no-clins-system-properties", success);
        success = ret.outAndErr.indexOf("in3=in3") < 0;
        report(tn + "test-cluster-has-no-in3-system-properties", success);
        ret = asadminWithOutput("list-system-properties", "--target", i3name);
        success = ret.outAndErr.indexOf("das=server") < 0;
        report(tn + "test-instance-has-no-das-system-properties", success);
        success = ret.outAndErr.indexOf("cl=in1in2") < 0;
        report(tn + "test-instance-has-no-cl-system-properties", success);
        success = ret.outAndErr.indexOf("clins=in2") < 0;
        report(tn + "test-das-has-no-clins-system-properties", success);
        success = ret.outAndErr.indexOf("in3=in3") < 0;
        report(tn + "test-instance-has-no-in3-system-properties", success);
        ret = asadminWithOutput("list-system-properties", "--target", i2name);
        success = ret.outAndErr.indexOf("das=server") < 0;
        report(tn + "test-instance-has-no-das-system-properties", success);
        success = ret.outAndErr.indexOf("cl=in1in2") < 0;
        report(tn + "test-instance-has-no-cl-system-properties", success);
        success = ret.outAndErr.indexOf("clins=in2") < 0;
        report(tn + "test-das-has-no-clins-system-properties", success);
        success = ret.outAndErr.indexOf("in3=in3") < 0;
        report(tn + "test-instance-has-no-in3-system-properties", success);

        // Cleanup
        report(tn + "stop-local-instance1", asadmin("stop-local-instance", i1name));
        report(tn + "stop-local-instance2", asadmin("stop-local-instance", i2name));
        report(tn + "stop-local-instance3", asadmin("stop-local-instance", i3name));
        report(tn + "delete-local-instance1", asadmin("delete-local-instance", i1name));
        report(tn + "delete-local-instance2", asadmin("delete-local-instance", i2name));
        report(tn + "delete-local-instance3", asadmin("delete-local-instance", i3name));
        report(tn + "delete-cluster", asadmin("delete-cluster", cname));
    }

    private void testGMSSetGetValues() {
        final String tn = "setgetGMSvaluescli-";
        final String cname = "gmscl";
        final String i1name = "i1";


        //create-cluster for gms set get testing
        report(tn + "create-cluster-" + cname, asadmin("create-cluster",
                "--multicastport", "2231",
                "--multicastaddress", "228.9.1.3", cname));

        // create an instance
        report(tn + "create-instance-" + i1name, asadmin("create-instance",
                "--node", "localhost-domain1", "--cluster", cname,
                "--systemproperties", "GMS-BIND-INTERFACE-ADDRESS-" + cname + "=192.168.10.1:GMS_LISTENER_PORT-" + cname + "=9492",
                i1name));

        String expected = "true";
        String dottedattributename = "clusters.cluster." + cname + ".gms-enabled";
        AsadminReturn ret = asadminWithOutput("get", dottedattributename);
        boolean success = ret.outAndErr.indexOf(dottedattributename + "=" + expected) >= 0;
        report(tn + "test-get-gms-enabled=" + expected, success);

        expected = "false";
        dottedattributename = "clusters.cluster." + cname + ".gms-enabled";
        ret = asadminWithOutput("set", dottedattributename + "=" + expected);
        ret = asadminWithOutput("get", dottedattributename);
        success = ret.outAndErr.indexOf(dottedattributename + "=" + expected) >= 0;
        report(tn + "test-set-get-gms-enabled=" + expected, success);

        expected = "228.9.1.3";
        dottedattributename = "clusters.cluster." + cname + ".gms-multicast-address";
        ret = asadminWithOutput("get", dottedattributename);
        success = ret.outAndErr.indexOf(dottedattributename + "=" + expected) >= 0;
        report(tn + "test-get-gms-multicast-address=" + expected, success);

        expected = "229.10.2.2";
        dottedattributename = "clusters.cluster." + cname + ".gms-multicast-address";
        ret = asadminWithOutput("set", dottedattributename + "=" + expected);
        ret = asadminWithOutput("get", dottedattributename);
        success = ret.outAndErr.indexOf(dottedattributename + "=" + expected) >= 0;
        report(tn + "test-set-get-gms-multicast-address=" + expected, success);

        expected = "2231";
        dottedattributename = "clusters.cluster." + cname + ".gms-multicast-port";
        ret = asadminWithOutput("get", dottedattributename);
        success = ret.outAndErr.indexOf(dottedattributename + "=" + expected) >= 0;
        report(tn + "test-get-gms-multicast-port=" + expected, success);

        expected = "3388";
        dottedattributename = "clusters.cluster." + cname + ".gms-multicast-port";
        ret = asadminWithOutput("set", dottedattributename + "=" + expected);
        ret = asadminWithOutput("get", dottedattributename);
        success = ret.outAndErr.indexOf(dottedattributename + "=" + expected) >= 0;
        report(tn + "test-set-get-gms-multicast-port=" + expected, success);

        expected = "3";
        dottedattributename = "configs.config." + cname + "-config.group-management-service.failure-detection.max-missed-heartbeats";
        ret = asadminWithOutput("get", dottedattributename);
        success = ret.outAndErr.indexOf(dottedattributename + "=" + expected) >= 0;
        report(tn + "test-get-max-missed-heartbeats=" + expected, success);

        expected = "4";
        dottedattributename = "configs.config." + cname + "-config.group-management-service.failure-detection.max-missed-heartbeats";
        ret = asadminWithOutput("set", dottedattributename + "=" + expected);
        ret = asadminWithOutput("get", dottedattributename);
        success = ret.outAndErr.indexOf(dottedattributename + "=" + expected) >= 0;
        report(tn + "test-set-get-max-missed-heartbeats=" + expected, success);

        expected = "2000";
        dottedattributename = "configs.config." + cname + "-config.group-management-service.failure-detection.heartbeat-frequency-in-millis";
        ret = asadminWithOutput("get", dottedattributename);
        success = ret.outAndErr.indexOf(dottedattributename + "=" + expected) >= 0;
        report(tn + "test-get-heartbeat-frequency-in-millis=" + expected, success);

        expected = "3111";
        dottedattributename = "configs.config." + cname + "-config.group-management-service.failure-detection.heartbeat-frequency-in-millis";
        ret = asadminWithOutput("set", dottedattributename + "=" + expected);
        ret = asadminWithOutput("get", dottedattributename);
        success = ret.outAndErr.indexOf(dottedattributename + "=" + expected) >= 0;
        report(tn + "test-set-get-heartbeat-frequency-in-millis=" + expected, success);

        expected = "1500";
        dottedattributename = "configs.config." + cname + "-config.group-management-service.failure-detection.verify-failure-waittime-in-millis";
        ret = asadminWithOutput("get", dottedattributename);
        success = ret.outAndErr.indexOf(dottedattributename + "=" + expected) >= 0;
        report(tn + "test-get-heartbeat-frequency-in-millis=" + expected, success);

        expected = "2611";
        dottedattributename = "configs.config." + cname + "-config.group-management-service.failure-detection.verify-failure-waittime-in-millis";
        ret = asadminWithOutput("set", dottedattributename + "=" + expected);
        ret = asadminWithOutput("get", dottedattributename);
        success = ret.outAndErr.indexOf(dottedattributename + "=" + expected) >= 0;
        report(tn + "test-set-get-verify-failure-waittime-in-millis=" + expected, success);

        expected = "10000";
        dottedattributename = "configs.config." + cname + "-config.group-management-service.failure-detection.verify-failure-connect-timeout-in-millis";
        ret = asadminWithOutput("get", dottedattributename);
        success = ret.outAndErr.indexOf(dottedattributename + "=" + expected) >= 0;
        report(tn + "test-get-verify-failure-connect-timeout-in-millis=" + expected, success);

        expected = "21111";
        dottedattributename = "configs.config." + cname + "-config.group-management-service.failure-detection.verify-failure-connect-timeout-in-millis";
        ret = asadminWithOutput("set", dottedattributename + "=" + expected);
        ret = asadminWithOutput("get", dottedattributename);
        success = ret.outAndErr.indexOf(dottedattributename + "=" + expected) >= 0;
        report(tn + "test-set-get-verify-failure-connect-timeout-in-millis=" + expected, success);

        expected = "GMS-BIND-INTERFACE-ADDRESS-" + cname;
        String expected2 = "129.166.10.1";
        String setValue = expected + "=" + expected2;
        dottedattributename = "servers.server.server.system-property.GMS-BIND-INTERFACE-ADDRESS-" + cname + ".name";
        ret = asadminWithOutput("create-system-properties", setValue);
        ret = asadminWithOutput("get", dottedattributename);
        success = ret.outAndErr.indexOf(dottedattributename + "=" + expected) >= 0;
        report(tn + "test-create-system-properties-get-das-GMS-BIND-INTERFACE-ADDRESS-name=" + expected, success);
        dottedattributename = "servers.server.server.system-property.GMS-BIND-INTERFACE-ADDRESS-" + cname + ".value";
        ret = asadminWithOutput("get", dottedattributename);
        success = ret.outAndErr.indexOf(dottedattributename + "=" + expected2) >= 0;
        report(tn + "test-create-system-properties-get-das-GMS-BIND-INTERFACE-ADDRESS-value=" + expected2, success);

        expected = "${GMS_LISTENER_PORT-" + cname + "}";
        dottedattributename = "clusters.cluster." + cname + ".property.GMS_LISTENER_PORT";
        ret = asadminWithOutput("get", dottedattributename);
        success = ret.outAndErr.indexOf(dottedattributename + "=" + expected) >= 0;
        report(tn + "test-get-GMS_LISTENER_PORT=" + expected, success);

        expected = "${GMS-BIND-INTERFACE-ADDRESS-" + cname + "}";
        dottedattributename = "clusters.cluster." + cname + ".gms-bind-interface-address";
        ret = asadminWithOutput("get", dottedattributename);
        success = ret.outAndErr.indexOf(dottedattributename + "=" + expected) >= 0;
        report(tn + "test-get-gms-bind-interface-address=" + expected, success);

        expected = "192.168.10.1";
        dottedattributename = "servers.server." + i1name + ".system-property.GMS-BIND-INTERFACE-ADDRESS-" + cname + ".value";
        ret = asadminWithOutput("get", dottedattributename);
        success = ret.outAndErr.indexOf(dottedattributename + "=" + expected) >= 0;
        report(tn + "test-get--instance-GMS-BIND-INTERFACE-ADDRESS-value=" + expected, success);

        expected = "193.169.11.2";
        dottedattributename = "servers.server." + i1name + ".system-property.GMS-BIND-INTERFACE-ADDRESS-" + cname + ".value";
        ret = asadminWithOutput("set", dottedattributename + "=" + expected);
        ret = asadminWithOutput("get", dottedattributename);
        success = ret.outAndErr.indexOf(dottedattributename + "=" + expected) >= 0;
        report(tn + "test-set-get-instance-GMS-BIND-INTERFACE-ADDRESS-value=" + expected, success);

        expected = "GMS-BIND-INTERFACE-ADDRESS-" + cname;
        dottedattributename = "servers.server." + i1name + ".system-property.GMS-BIND-INTERFACE-ADDRESS-" + cname + ".name";
        ret = asadminWithOutput("get", dottedattributename);
        success = ret.outAndErr.indexOf(dottedattributename + "=" + expected) >= 0;
        report(tn + "test-get-instance-GMS-BIND-INTERFACE-ADDRESS-name=" + expected, success);

        expected = "9492";
        dottedattributename = "servers.server." + i1name + ".system-property.GMS_LISTENER_PORT-" + cname + ".value";
        ret = asadminWithOutput("get", dottedattributename);
        success = ret.outAndErr.indexOf(dottedattributename + "=" + expected) >= 0;
        report(tn + "test-get-GMS_LISTENER_PORT-" + cname + ".value=" + expected, success);

        expected = "8583";
        dottedattributename = "servers.server." + i1name + ".system-property.GMS_LISTENER_PORT-" + cname + ".value";
        ret = asadminWithOutput("set", dottedattributename + "=" + expected);
        ret = asadminWithOutput("get", dottedattributename);
        success = ret.outAndErr.indexOf(dottedattributename + "=" + expected) >= 0;
        report(tn + "test-set-get-GMS_LISTENER_PORT-" + cname + ".value=" + expected, success);

        // Cleanup
        report(tn + "delete-instance", asadmin("delete-instance", i1name));

        report(tn + "delete-cluster", asadmin("delete-cluster", cname));
    }

    @Override
    public void cleanup() {
        //Cleanup the code so that tests run successfully next time
        report("delete-cl1", asadmin("delete-cluster", "cl1"));
        report("delete-cl2", asadmin("delete-cluster", "cl2"));
        report("delete-cl1-config", asadmin("delete-config", "cl1-config"));
        report("delete-cl3", !asadmin("delete-cluster", "cl3")); // should not have been created
        report("delete-cl4", asadmin("delete-cluster", "cl4"));

        report("get-health-no-cluster-after", !asadmin("get-health", "cl1"));

        AsadminReturn ret = asadminWithOutput("list-clusters");
        String s = (ret.out == null) ? "" : ret.out.trim();

        // make sure none of OUR clusters are in there.  Other clusters that are
        // in the user's domain are OK...

        boolean success = s.indexOf("cl1") < 0
                && s.indexOf("cl2") < 0
                && s.indexOf("cl3") < 0
                && s.indexOf("cl4") < 0;

        if (!success) {
            System.out.println("IT 12153 is apparently not fixed!!  \nLet's try a restart and call list-clusters again...");
            asadmin("restart-domain");
            asadmin("list-clusters");
        }
        else
            report("verify-list-of-zero-clusters", success);
    }

    boolean checkListInstancesOutputIfRunning(String output, String iname) {
        return output.split(iname + " +running").length == 2;
    }
}
