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

import java.io.*;
import java.net.*;

/*
 * Dev test for create/delete/list instance
 * @author Bhakti Mehta
 * @author Byron Nevins
 */
public class InstanceTest extends AdminBaseDevTest {

    public InstanceTest() {
        int numTests;

        try {
            numTests = Integer.parseInt(System.getProperty("NUM_TESTS"));

            if (numTests < 1) {
                numTests = DEFAULT_NUM_TESTS;
            }
        }
        catch (Exception e) {
            numTests = DEFAULT_NUM_TESTS;
        }

        setupInstances(numTests);
        printf("DEBUG is turned **ON**");
        String host0 = null;

        try {
            //host0 = InetAddress.getLocalHost().getHostName();
            host0 = "localhost";  //when DAS and instance are co-located use localhost
        }
        catch (Exception e) {
            host0 = "localhost";
        }
        host = host0;
        System.out.println("Host= " + host);
        glassFishHome = getGlassFishHome();
        if (isHadas()) {
            // NO MORE NODES DIR!
            // gf/nodes/node1/i1 --->  gf/domains/domain1/i1
            domainHome = new File(glassFishHome, "domains/domain1/server");
            nodeDir = new File(glassFishHome, "domains/");
            // it does NOT need to exist -- do not insist!
            instancesHome = new File(nodeDir, "domain1");
        }
        else {
            domainHome = new File(glassFishHome, "domains/domain1");
            nodeDir = new File(glassFishHome, "nodes");
            // it does NOT need to exist -- do not insist!
            instancesHome = new File(nodeDir, host + "-domain1");
        }
        printf("GF HOME = " + glassFishHome);
    }

    public static void main(String[] args) throws IOException, InterruptedException {
        new InstanceTest().runTests();
    }

    @Override
    protected String getTestDescription() {
        return "Unit test for create/delete/list instance";
    }

    public void runTests() throws IOException, InterruptedException {
        startDomain();
        testDuplicateReportNames();
        testNamesWithSpaces();
        create();
        delete();
        createFail();
        createSysProps();
        testNoCreateForStop();
        createStartStopDelete();
        createAdminCommand();
        deleteAdminCommand();
        testRendezvous();
        testUpgrade();
        testNode();
	testCreateInstanceConfigNode();
        testPortBase();
        invalidConfigRef();
        
        if(!isHadas())
            deleteDirectory(nodeDir);

        stopDomain();
        stat.printSummary();
    }

    private void testNamesWithSpaces() {
        report("Name with many spaces is here hi there", true);
        report("Name with many spaces is here hi there", true);
    }

    private void testDuplicateReportNames() {
        report("duplicate_here", true);
        report("duplicate_here", true);
        report("duplicate_here", true);
    }

    private void testNoCreateForStop() {
        String metname = "testNoCreateForStop";
        String iname = generateInstanceName();

        report(metname + "-nodir-before", !checkInstanceDir(iname));
        asadmin("stop-local-instance", iname); // should NOT create any files
        report(metname + "-nodir-after", !checkInstanceDir(iname));
    }

    private void createStartStopDelete() throws InterruptedException {
        String metname = "createStartStopDelete";
        String iname = generateInstanceName();

        report(metname + "-nodir-xxxx", !checkInstanceDir(iname));
        asadmin("stop-local-instance", iname); // in case it's running?!?
        report(metname + "-nodir", !checkInstanceDir(iname));
        report(metname + "-create", asadmin("create-local-instance", iname));
        report(metname + "-yesdir", checkInstanceDir(iname));

        // see Jira 16232 for details.
        report(metname + "-verifyNotRunning", !isInstanceRunning(iname));
        report(metname + "-startByRestart", asadmin("restart-instance", iname));
        report(metname + "-verifyRunning", isInstanceRunning(iname));
        report(metname + "-restart-instance", asadmin("restart-instance", iname));
        report(metname + "-verifyRunning", isInstanceRunning(iname));
        report(metname + "-stop", asadmin("stop-local-instance", iname));
        report(metname + "-verifyNotRunning", !isInstanceRunning(iname));
        report(metname + "-start", asadmin("start-local-instance", iname));
        report(metname + "-list-instances", isInstanceRunning(iname));
        report(metname + "-restart-local-instance", asadmin("restart-local-instance", iname));
        report(metname + "-verifyRunning", isInstanceRunning(iname));
        report(metname + "-stop", asadmin("stop-local-instance", iname));
        report(metname + "-verifyNotRunning", !isInstanceRunning(iname));
        report(metname + "-delete", asadmin("delete-local-instance", iname));
        report(metname + "-no-dir-again", !checkInstanceDir(iname));
    }


    private void create() {
        printf("Create " + instanceNames.length + " instances");
        for (String iname : instanceNames) {
            report(iname + "-nodir", !checkInstanceDir(iname));
            report(iname + "-create", asadmin("create-local-instance", iname));
            report(iname + "-yesdir", checkInstanceDir(iname));
            String err = checkSpecialConfigDirsExist(iname);

            if (err != null)
                System.out.println("ERROR: " + err);

            report(iname + "-yesspecialdirs", err == null ? true : false); // null is good!!
            report(iname + "-yes-regdas", asadminWithOutput("get", "servers.server." + iname));
            report(iname + "-yes-config", asadminWithOutput("get", "configs.config." + iname + "-config"));
            AsadminReturn ret = asadminWithOutput("get", "servers.server." + iname + ".config-ref");
            boolean success = ret.outAndErr.indexOf("servers.server." + iname + ".config-ref=" + iname + "-config") >= 0;
            report(iname + "-yes-configref", success);
        }
        report("das-properties-exists-after-create", checkDasProperties());
        asadmin("list-instances");
    }

    private void createSysProps() {
        printf("Create local instance with system properties");
        String iname = "localinstancewithsysprops";
        report("create-local-instance-sysprops", asadminWithOutput("create-local-instance",
                "--systemproperties", "prop1=valA:prop2=valB:prop3=valC", iname));

        AsadminReturn ret = asadminWithOutput("get", "servers.server." + iname + ".system-property.prop1.name");
        boolean success = ret.outAndErr.indexOf("servers.server." + iname + ".system-property.prop1.name=prop1") >= 0;
        report("create-local-instance-prop1name", success);

        ret = asadminWithOutput("get", "servers.server." + iname + ".system-property.prop1.value");
        success = ret.outAndErr.indexOf("servers.server." + iname + ".system-property.prop1.value=valA") >= 0;
        report("create-local-instance-prop1value", success);

        ret = asadminWithOutput("get", "servers.server." + iname + ".system-property.prop3.name");
        success = ret.outAndErr.indexOf("servers.server." + iname + ".system-property.prop3.name=prop3") >= 0;
        report("create-local-instance-prop3name", success);

        ret = asadminWithOutput("get", "servers.server." + iname + ".system-property.prop3.value");
        success = ret.outAndErr.indexOf("servers.server." + iname + ".system-property.prop3.value=valC") >= 0;
        report("create-local-instance-prop3value", success);

        report("delete-instance-sysprops", asadmin("delete-local-instance", iname));
    }

    private void createFail() {
        //printf("create-local-instance with wrong host");
        //report("create-local-instance-wronghost", !asadmin("--host", "wronghost", "create-local-instance", "instancefail"));

        printf("create-local-instance with non-existent cluster");
        report("create-local-instance-nosuchcluster", !asadmin("create-local-instance", "--cluster", "nocluster", "noinstance"));
        report("cleanup-failed-c-l-i", !checkInstanceDir("noinstance"));
    }

    private void delete() {
        printf("Delete " + instanceNames.length + " instances");
        for (String iname : instanceNames) {
            report(iname + "-yes-dir", checkInstanceDir(iname));
            report(iname + "-delete", asadmin("delete-local-instance", iname));
            report(iname + "-no-dir-again", !checkInstanceDir(iname));
            report(iname + "-no-regdas", !asadmin("get", "servers.server." + iname));
            report(iname + "-no-config", !asadmin("get", "configs.config." + iname + "-config"));
            report(iname + "-special-dirs-were-deleted", checkSpecialConfigDirsDeleted(iname));
        }

        AsadminReturn ret = asadminWithOutput("list-instances");
        System.out.println(ret.outAndErr);
        boolean success = ret.outAndErr.indexOf("Nothing to list.") >= 0;

        //report("list-instance-after-delete", success);

    }

    private void createAdminCommand() {
        printf("Call remote AdminCommand create-instance");
        String iname = "sugar";
        report("create-instance-success", asadmin("create-instance",
            "--node", "localhost-domain1", iname));
        report("create-instance-regdas", asadminWithOutput("get", "servers.server." + iname));
        report("create-instance-config", asadminWithOutput("get", "configs.config." + iname + "-config"));

        AsadminReturn ret = asadminWithOutput("get", "servers.server." + iname + ".config-ref");
        boolean success = ret.outAndErr.indexOf("servers.server." + iname + ".config-ref=" + iname + "-config") >= 0;
        report("create-instance-configref", success);

        ret = asadminWithOutput("get", "servers.server." + iname + ".node-ref");
        success = ret.outAndErr.indexOf("servers.server." + iname + ".node-ref=localhost") >= 0;
        report("create-instance-node", success);

        report("create-instance-existsAlready", !asadmin("create-instance",
            "--node", "localhost-domain1", iname));

        createAdminCommandSystemProperties();
        createAdminCommandClusterConfig();
    }

    private void createAdminCommandSystemProperties() {
        printf("Call remote AdminCommand create-instance with system properties");
        String iname = "instancewithsysprops";

        report("create-instance-sysprops", asadminWithOutput("create-instance",
                "--node", "localhost-domain1",
                "--systemproperties", "prop1=valA:prop2=valB:prop3=valC", iname));

        AsadminReturn ret = asadminWithOutput("get", "servers.server." + iname + ".system-property.prop1.name");
        boolean success = ret.outAndErr.indexOf("servers.server." + iname + ".system-property.prop1.name=prop1") >= 0;
        report("create-instance-prop1name", success);

        ret = asadminWithOutput("get", "servers.server." + iname + ".system-property.prop1.value");
        success = ret.outAndErr.indexOf("servers.server." + iname + ".system-property.prop1.value=valA") >= 0;
        report("create-instance-prop1value", success);
        
        ret = asadminWithOutput("get", "servers.server." + iname + ".system-property.prop3.name");
        success = ret.outAndErr.indexOf("servers.server." + iname + ".system-property.prop3.name=prop3") >= 0;
        report("create-instance-prop3name", success);

        ret = asadminWithOutput("get", "servers.server." + iname + ".system-property.prop3.value");
        success = ret.outAndErr.indexOf("servers.server." + iname + ".system-property.prop3.value=valC") >= 0;
        report("create-instance-prop3value", success);

        report("delete-instance-sysprops", asadmin("delete-instance", iname));
    }

    private void createAdminCommandClusterConfig() {
        printf("Call remote AdminCommand create-instance for a cluster");
        String iname = "instanceforcluster";

        report("create-instance-cluster", asadmin("create-cluster", "jencluster"));

        report("create-instance-forcluster", asadmin("create-instance",
                "--node", "localhost-domain1", "--cluster", "jencluster", iname));

        AsadminReturn ret = asadminWithOutput("get", "servers.server." + iname + ".config-ref");
        boolean success = ret.outAndErr.indexOf("servers.server." + iname + ".config-ref=jencluster-config") >= 0;
        report("create-instance-clusterconfigref", success);

        report("delete-instance-forcluster", asadmin("delete-instance", iname));
        report("delete-instance-cluster", asadmin("delete-cluster", "jencluster"));
    }

    private void deleteAdminCommand() {
        printf("Call remote AdminCommand delete-instance");
        String iname = "sugar";
        report("delete-instance-success", asadmin("delete-instance", iname));
        report("delete-instance-regdas", !asadmin("get", "servers.server." + iname));
        report("delete-instance-config", !asadmin("get", "configs.config." + iname + "-config"));
    }
    
    private void createAdminCommandFail() {
        printf("Call remote AdminCommand create-instance with bad params");
        String iname = "badapple";
        report("create-instance-nosuchnode", !asadmin("create-instance",
            "--node", "bogus", iname));
        report("create-instance-nosuchcluster", !asadmin("create-instance",
            "--node", "localhost-domain1", "--cluster", "nosuchcluster", iname));
        report("create-instance-nosuchconfig", !asadmin("create-instance",
            "--node", "localhost-domain1", "--config", "nosuchconfig", iname));
        report("create-instance-clusterandconfig", !asadmin("create-instance",
            "--node", "localhost-domain1", "--cluster", "c1", "--config", "config1", iname));
    }

    private void deleteAdminCommandFail() {
        printf("Call remote AdminCommand delete-instance with bad params");
        String iname = "nosuchinstance";
        report("delete-instance-fail", !asadmin("delete-instance", iname));
    }

    private void testRendezvous() {
        String instance = "rendezvousinstance";
        report("create-local-instance-rendezvous", asadmin("create-local-instance", instance));
        AsadminReturn ret = asadminWithOutput("get", "servers.server."+instance+".property.rendezvousOccurred");
        boolean success = ret.outAndErr.indexOf("servers.server."+instance+".property.rendezvousOccurred=true") >= 0;
        report("rendezvous-true-rendezvous", success);
        report("create-local-instance-rendezvousAlready", !asadmin("create-local-instance", instance));
        report("set-rendezvousOccurred-false", asadmin("set", "servers.server."+instance+".property.rendezvousOccurred=false"));
        report("create-local-instance-rendezvousAgain", asadmin("create-local-instance", instance));
        report("delete-local-instance-rendezvous", asadmin("delete-local-instance", instance));
    }

    private void testUpgrade() { //Issue 12736 support creation of local instance from DAS data - rendezvous flag handling
        String instance = "upgradeinstance";
        cleanup(); //remove locahost dir so we can see it gets created here.
        report("register-instance-upgrade", asadmin("_register-instance",
            "--node", "localhost-domain1", instance));
        report("upgradeinstance-registered", asadminWithOutput("get", "servers.server."+instance));
        report("create-local-instance-upgrade", asadmin("create-local-instance", instance));
        report("das-properties-exists-upgrade", checkDasProperties());
        AsadminReturn ret = asadminWithOutput("get", "servers.server."+instance+".property.rendezvousOccurred");
        boolean success = ret.outAndErr.indexOf("servers.server."+instance+".property.rendezvousOccurred=true") >= 0;
        report("rendezvous-true-upgrade", success);
        report("delete-local-instance-upgrade", asadmin("delete-local-instance", instance));
    }

    private void testNode() throws IOException {
        if(isHadas())
            testNodeHadas();
        else
            testNodeTrunk();
    }

    private void testNodeTrunk() throws IOException {
        String installdir = getGlassFishHome().getCanonicalPath();
        String nodedir = installdir + File.separator + "mynodes";
        String node = "n1";
        String instance = "i1";
        
        report("create-local-instance-nosuchnode", !asadmin("create-local-instance",
            "--node", "bogus", "bogusinstance"));

        report("create-node-config-i1n1", asadmin("create-node-config",
             node ));
        report("create-local-instance-i1n1", asadmin("create-local-instance",
            "--nodedir", nodedir , "--node", node, instance ));

        report("check-i1-n1-dir", checkInstanceDir(instance, node, nodedir));
        report("check-i1-n1-dasprops", checkDasProperties(node, nodedir));

        report("start-local-instance-i1n1", asadmin("start-local-instance",
            "--nodedir", nodedir , "--node", node, instance ));

        report("list-instances-i1n1", asadmin("list-instances"));
        report("check-list-instances-n1-run", isInstanceRunning(instance));

        report("stop-local-instance-i1n1", asadmin("stop-local-instance",
            "--nodedir", nodedir , "--node", node, instance ));

        report("list-instances-i1n1", asadmin("list-instances"));
        report("check-list-instances-i1n1-notrun", !isInstanceRunning(instance));

        report("delete-local-instance-n1", asadmin("delete-local-instance",
            "--nodedir", nodedir , "--node", node, instance ));
        report("check-i1n1-dir-deleted", !checkInstanceDir(instance, node, nodedir));

        report("verify-no-instances-i1n1", verifyNoInstances());

        //clean up
        report("delete-node-config-n1", asadmin("delete-node-config", node ));

        deleteDirectory(new File(nodedir));
    }
    private void testNodeHadas() throws IOException {
        String installdir = getGlassFishHome().getCanonicalPath();
        String nodedir = installdir + "/domains";
        String node = "domain1";
        String instance = "i1";

        report("create-local-instance-badparam", !asadmin("create-local-instance",
            "--node", "bogus", "bogusinstance"));

        report("create-local-instance-nosuchdomain", !asadmin("create-local-instance",
            "--domain", "bogus", "bogusinstance"));

        report("create-node-config-i1", asadmin("create-node-config",
             node ));
        report("create-local-instance-i1", asadmin("create-local-instance",
            "--domaindir", nodedir , "--domain", node, instance ));

        report("check-i1-dir", checkInstanceDir(instance, node, nodedir));
        report("check-i1-dasprops", checkDasProperties(node, nodedir));

        report("start-local-instance-i1", asadmin("start-local-instance",
            "--domaindir", nodedir , "--domain", node, instance ));

        report("list-instances-i1", asadmin("list-instances"));
        report("check-list-instances-n1-run", isInstanceRunning(instance));

        report("stop-local-instance-i1", asadmin("stop-local-instance",
            "--domaindir", nodedir , "--domain", node, instance ));

        report("list-instances-i1", asadmin("list-instances"));
        report("check-list-instances-i1-notrun", !isInstanceRunning(instance));

        report("delete-local-instance-i1", asadmin("delete-local-instance",
            "--domaindir", nodedir , "--domain", node, instance ));
        report("check-i1n1-dir-deleted", !checkInstanceDir(instance, node, nodedir));

        report("verify-no-instances-i1n1", verifyNoInstances());

        //clean up
        report("delete-node-config-n1", asadmin("delete-node-config", node ));
    }

    private void testCreateInstanceConfigNode() {
        // see JIRA issue 16579
        String node = "test-node";
        String instance = "test-instance";
	String testName = "create-node-config-offline";

	// Creates config node without the installdir
        report(testName + "0", asadmin("create-node-config",
		"--nodehost", "localhost",node));
	report(testName+"1", asadmin("create-instance", "--node", node, instance));
	// check that installdir was set 
        report(testName+"2",asadmin("get", "nodes.node." + node + ".install-dir"));
	
	//cleanup
	report(testName +"3", asadmin("delete-instance", instance ));
	report(testName +"4", asadmin("delete-node-config", node ));

    }

    private void testPortBase() {
        String instance = "portbaseinstance";
        report("create-local-instance-portbase-junk1", !asadmin("create-local-instance", "--portbase", "junk" ,instance));
        report("create-local-instance-portbase-junk2", !asadmin("create-local-instance", "--portbase", "99999999999" ,instance));
        report("create-local-instance-portbase-junk3", !asadmin("create-local-instance", "--portbase", "-11111111" ,instance));
        report("create-local-instance-portbase-success", asadmin("create-local-instance", "--portbase", "3300", "--checkports", "false" ,instance));
        AsadminReturn ret = asadminWithOutput("get", "servers.server."+instance+".system-property.*");
        boolean success = ret.outAndErr.indexOf("servers.server."+instance+".system-property.HTTP_LISTENER_PORT.value=3380") >= 0;
        report("check-portbase-http-listener-port", success);
        success = ret.outAndErr.indexOf("servers.server."+instance+".system-property.HTTP_SSL_LISTENER_PORT.value=3381") >= 0;
        report("check-portbase-http-ssl-listener-port", success);
        success = ret.outAndErr.indexOf("servers.server."+instance+".system-property.IIOP_SSL_LISTENER_PORT.value=3338") >= 0;
        report("check-portbase-iiop-ssl-listener-port", success);
        success = ret.outAndErr.indexOf("servers.server."+instance+".system-property.JMS_PROVIDER_PORT.value=3376") >= 0;
        report("check-portbase-jms-provider-port", success);
        success = ret.outAndErr.indexOf("servers.server."+instance+".system-property.JMX_SYSTEM_CONNECTOR_PORT.value=3386") >= 0;
        report("check-portbase-jmx-system-connector-port", success);
        success = ret.outAndErr.indexOf("servers.server."+instance+".system-property.IIOP_LISTENER_PORT.value=3337") >= 0;
        report("check-portbase-iiop-listener-port", success);
        success = ret.outAndErr.indexOf("servers.server."+instance+".system-property.IIOP_SSL_MUTUALAUTH_PORT.value=3339") >= 0;
        report("check-portbase-iiop-ssl-mutualauth-port", success);
        success = ret.outAndErr.indexOf("servers.server."+instance+".system-property.ASADMIN_LISTENER_PORT.value=3348") >= 0;
        report("check-portbase-asadmin-listener-port", success);

        //clean up
        report("delete-local-instance-portbase", asadmin("delete-local-instance", instance ));
    }

    private void invalidConfigRef() {
        String inst = "invalidconfigrefinstance";
        String in1 = "in1";
        String dasConfig ="server-config";
        String defaultConfig = "default-config";
        String c1 = "c1";
        String inA = "inA";
        String someConfig = "some-config";
        report("invalid-config-ref-copy-config", asadmin("copy-config",  "server-config", someConfig));
        report("invalid-config-ref-server-config", asadmin("create-local-instance", in1));
        report("invalid-config-ref-create-cluster", asadmin("create-cluster", c1));
        report("invalid-config-ref-create-local-instance", asadmin("create-local-instance", "--cluster", c1, inA));

        //1) not changing config-ref for DAS
        report("invalid-config-ref-cant-change-das-config", !asadmin("set", "servers.server.server.config-ref="+someConfig));

        //2) not allowing config-ref of 'server-config' for non-DAS
        report("invalid-config-ref-server-config-create", !asadmin("create-local-instance", "--config", dasConfig, inst));
        report("invalid-config-ref-server-config-set", !asadmin("set", "servers.server."+in1+".config-ref=" + dasConfig));

        //3) not allowing config-ref of 'default-config'
        report("invalid-config-ref-default-config-create", !asadmin("create-local-instance", "--config", defaultConfig, inst));
        report("invalid-config-ref-default-config-set", !asadmin("set", "servers.server."+in1+".config-ref=" + defaultConfig));

        //4) not allowing changing config-ref of clustered instance
        report("invalid-config-ref-clustered-instance", !asadmin("set", "servers.server."+inA+".config-ref=" + someConfig));

        //5) not allowing changing config-ref to non-existent config
        report("invalid-config-ref-nonexistent", !asadmin("set", "servers.server."+in1+".config-ref=nosuchconfig"));

        //6) not allowing config-ref to be null
        report("invalid-config-ref-null", !asadmin("set", "servers.server."+in1+".config-ref="));

        //cleanup
        report("invalid-config-ref-delete-config", asadmin("delete-config", someConfig));
        report("invalid-config-ref-delete-local-instance-sa", asadmin("delete-local-instance", in1));
        report("invalid-config-ref-delete-local-instance-ci", asadmin("delete-local-instance", inA));
        report("invalid-config-ref-delete-cluster", asadmin("delete-cluster", c1));

    }

    private boolean checkInstanceDir(String name) {
        File inf = new File(instancesHome, name);
        boolean exists = inf.isDirectory();
        return exists;
    }

    private boolean checkDasProperties() {
        File dasFile = new File(instancesHome, "agent/config/das.properties");
        return dasFile.exists();
    }

    private boolean checkInstanceDir(String instance, String node, String nodedir) {
        File inf = new File(nodedir + File.separator + node, instance);
        boolean exists = inf.isDirectory();
        return exists;
    }

    private boolean checkDasProperties(String node, String nodedir) {
        File dasFile = new File(nodedir + File.separator + node, "agent/config/das.properties");
        return dasFile.exists();
    }

    /**
     *
     * @param iname
     * @return a String if in error o/w return null
     */
    private String checkSpecialConfigDirsExist(String iname) {
        File configConfigDir = new File(domainHome, "config/" + iname + "-config");

        if (!configConfigDir.isDirectory())
            return configConfigDir.toString().replace('\\', '/') + " was not created as expected.";

        if (!new File(configConfigDir, "lib/ext").isDirectory())
            return configConfigDir.getPath().replace('\\', '/') + "/lib/ext was not created as expected.";

        if (!new File(configConfigDir, "docroot").isDirectory())
            return configConfigDir.getPath().replace('\\', '/') + "/docroot was not created as expected.";

        return null;
    }

    private boolean checkSpecialConfigDirsDeleted(String iname) {
        File configConfigDir = new File(domainHome, "config/" + iname + "-config");
        return !configConfigDir.exists();
    }

    private static void setupInstances(int num) {
        instanceNames = new String[num];

        for (int i = 0; i < num; i++) {
            instanceNames[i] = "instance_" + i;
        }
    }


    private final String host;
    private final File glassFishHome;
    private final File nodeDir;
    private final File instancesHome;
    private final File domainHome;
    private static String[] instanceNames;
    private static final int DEFAULT_NUM_TESTS = 2;

}
