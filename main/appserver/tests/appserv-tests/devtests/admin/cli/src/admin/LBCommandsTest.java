/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2010-2011 Oracle and/or its affiliates. All rights reserved.
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

import java.io.*;
import java.net.*;
import java.security.MessageDigest;

/*
 * Dev test for load balancer administration commands.
 * @author Yamini K B
 */
public class LBCommandsTest extends AdminBaseDevTest {

    public LBCommandsTest() {
        String host0 = null;

        try {
            host0 = InetAddress.getLocalHost().getHostName();
        }
        catch (Exception e) {
            host0 = LOCALHOST;
        }
        host = host0;
        System.out.println("Host= " + host);
        glassFishHome = getGlassFishHome();
        System.out.println("GF HOME = " + glassFishHome);
    }

    public static void main(String[] args) {
        new LBCommandsTest().run();
    }

    @Override
    public String getTestName() {
        return "LB commands";
    }

    @Override
    protected String getTestDescription() {
        return "Developer tests for load balancer administration";
    }

    @Override
    public void subrun() {
        asadmin("start-domain");
        createInstances();

        File loadbalancerXml = new File("resources", "loadbalancer.xml");

        int i = 1;
        //create,list LB config
        runTest(i++ + ".create-http-lb-config", asadmin("create-http-lb-config", LB_CONFIG));
        AsadminReturn ret = asadminWithOutput("list-http-lb-configs");
        boolean success = ret.out.indexOf(LB_CONFIG) >= 0;
        runTest(i++ + ".list-http-lb-configs", success);

        runTest(i++ + ".re-create-http-lb-config", !asadmin("create-http-lb-config", LB_CONFIG));

        //deleting cluster-ref when lb doesn't reference it should fail
        runTest(i++ + ".delete-http-lb-cluster-ref", !asadmin("delete-http-lb-ref", CONFIG_OPTION, LB_CONFIG, CLUSTER));

        //test options
        runTest(i++ + ".create-http-lb-cluster-ref", !asadmin("create-http-lb-ref", CLUSTER));
        runTest(i++ + ".create-http-lb-cluster-ref", !asadmin("create-http-lb-ref", CONFIG_OPTION, LB_CONFIG, LB_NAME_OPTION, LB_NAME, CLUSTER));

        //create/delete cluster-ref for LB
        runTest(i++ + ".create-http-lb-cluster-ref", asadmin("create-http-lb-ref", CONFIG_OPTION, LB_CONFIG, CLUSTER));
        runTest(i++ + ".re-create-http-lb-cluster-ref", !asadmin("create-http-lb-ref", CONFIG_OPTION, LB_CONFIG, CLUSTER));

        //creating server-ref when cluster-ref entry is already present should fail
        runTest(i++ + ".create-http-lb-server-ref", !asadmin("create-http-lb-ref", CONFIG_OPTION, LB_CONFIG, STANDALONE_INSTANCE1));

        //enable/disable clusters for LB
        runTest(i++ + ".enable-http-lb-server-for-cluster", asadmin("enable-http-lb-server", CLUSTER));
        runTest(i++ + ".disable-http-lb-server-for-cluster", asadmin("disable-http-lb-server", CLUSTER));

        runTest(i++ + ".delete-http-lb-config", !asadmin("delete-http-lb-config", LB_CONFIG));

        //re-create HC
        runTest(i++ + ".re-create-http-health-checker-for-cluster", !asadmin("create-http-health-checker", CONFIG_OPTION, LB_CONFIG, TIMEOUT_OPTION, "30", INTERVAL_OPTION, "5", CLUSTER));
        //re-create HC without specifying lb config name
        runTest(i++ + ".re-create-http-health-checker-for-cluster", !asadmin("create-http-health-checker", TIMEOUT_OPTION, "30", INTERVAL_OPTION, "5", CLUSTER));

        ret = asadminWithOutput("list-http-lb-configs", LB_CONFIG);
        success = ret.out.indexOf(CLUSTER) >= 0;
        runTest(i++ + ".list-http-lb-configs", success);

        ret = asadminWithOutput("list-http-lb-configs", CLUSTER);
        success = ret.out.indexOf(LB_CONFIG) >= 0;
        runTest(i++ + ".list-http-lb-configs", success);

        runTest(i++ + ".delete-http-health-checker-for-cluster", asadmin("delete-http-health-checker", CONFIG_OPTION, LB_CONFIG, CLUSTER));
        runTest(i++ + ".delete-http-health-checker-for-cluster", !asadmin("delete-http-health-checker", CONFIG_OPTION, LB_CONFIG, CLUSTER));

        //test options
        runTest(i++ + ".delete-http-lb-cluster-ref", !asadmin("delete-http-lb-ref", CLUSTER));
        runTest(i++ + ".delete-http-lb-cluster-ref", !asadmin("delete-http-lb-ref", CONFIG_OPTION, LB_CONFIG, LB_NAME_OPTION, LB_NAME, CLUSTER));

        runTest(i++ + ".delete-http-lb-cluster-ref", asadmin("delete-http-lb-ref", CONFIG_OPTION, LB_CONFIG, CLUSTER));

        //create server-ref for LB
        runTest(i++ + ".create-http-lb-server-ref", asadmin("create-http-lb-ref", CONFIG_OPTION, LB_CONFIG, STANDALONE_INSTANCE2));
        runTest(i++ + ".create-http-lb-server-ref", asadmin("create-http-lb-ref", CONFIG_OPTION, LB_CONFIG, STANDALONE_INSTANCE1));

        //creating cluster-ref when server-ref already exists should fail
        runTest(i++ + ".create-http-lb-cluster-ref", !asadmin("create-http-lb-ref", CONFIG_OPTION, LB_CONFIG, CLUSTER));

        ret = asadminWithOutput("list-http-lb-configs", LB_CONFIG);
        success = ret.out.indexOf(STANDALONE_INSTANCE1) >= 0;
        runTest(i++ + ".list-http-lb-configs", success);

        ret = asadminWithOutput("list-http-lb-configs", STANDALONE_INSTANCE2);
        success = ret.out.indexOf(LB_CONFIG) >= 0;
        runTest(i++ + ".list-http-lb-configs", success);

        runTest(i++ + ".re-create-http-lb-server-ref", !asadmin("create-http-lb-ref", CONFIG_OPTION, LB_CONFIG, STANDALONE_INSTANCE1));

        //enable/disable servers for LB
        runTest(i++ + ".enable-http-lb-server", asadmin("enable-http-lb-server", STANDALONE_INSTANCE2));

        runTest(i++ + ".delete-http-lb-server-ref", !asadmin("delete-http-lb-ref", CONFIG_OPTION, LB_CONFIG, STANDALONE_INSTANCE2));

        runTest(i++ + ".disable-http-lb-server", asadmin("disable-http-lb-server", STANDALONE_INSTANCE2));
        runTest(i++ + ".enable-http-lb-server", asadmin("enable-http-lb-server", STANDALONE_INSTANCE2));

        //delete/create health checker
        runTest(i++ + ".delete-http-health-checker", asadmin("delete-http-health-checker", CONFIG_OPTION, LB_CONFIG, STANDALONE_INSTANCE2));
        runTest(i++ + ".delete-http-health-checker", !asadmin("delete-http-health-checker", CONFIG_OPTION, LB_CONFIG, STANDALONE_INSTANCE2));
        runTest(i++ + ".create-http-health-checker", asadmin("create-http-health-checker", CONFIG_OPTION, LB_CONFIG, TIMEOUT_OPTION, "30", INTERVAL_OPTION, "5", STANDALONE_INSTANCE2));
        runTest(i++ + ".re-create-http-health-checker-for-server", !asadmin("create-http-health-checker", CONFIG_OPTION, LB_CONFIG, TIMEOUT_OPTION, "30", INTERVAL_OPTION, "5", STANDALONE_INSTANCE2));

        runTest(i++ + ".disable-http-lb-server", asadmin("disable-http-lb-server", STANDALONE_INSTANCE2));
        runTest(i++ + ".disable-http-lb-server", asadmin("disable-http-lb-server", STANDALONE_INSTANCE1));

        //delete server-ref for LB, but no apps
        runTest(i++ + ".delete-http-lb-server-ref", asadmin("delete-http-lb-ref", CONFIG_OPTION, LB_CONFIG, STANDALONE_INSTANCE2));

        //configure weights
        runTest(i++ + ".configure-lb-weight", asadmin("configure-lb-weight", CLUSTER_OPTION, CLUSTER, "cl1-ins1=2:cl1-ins2=3:cl1-ins3=5"));

        //configure weight for non-existing instance
        runTest(i++ + ".configure-lb-weight", !asadmin("configure-lb-weight", CLUSTER_OPTION, CLUSTER, "foo=10"));

        //configure weight for standalone instance
        runTest(i++ + ".configure-lb-weight", !asadmin("configure-lb-weight", CLUSTER_OPTION, CLUSTER, "ins1=10"));

        // deploy an application to the cluster
        File webapp = new File("resources", "helloworld.war");
        asadmin("deploy", "--target", CLUSTER, webapp.getAbsolutePath());
        asadmin("create-application-ref", "--target", CLUSTER2, "helloworld");
        asadmin("create-application-ref", "--target", STANDALONE_INSTANCE1, "helloworld");
        asadmin("create-application-ref", "--target", STANDALONE_INSTANCE2, "helloworld");

        //disable/enable application for LB
        runTest(i++ + ".enable-http-lb-application", asadmin("enable-http-lb-application", NAME_OPTION , "helloworld", STANDALONE_INSTANCE1));

        runTest(i++ + "create-http-lb-server-ref", asadmin("create-http-lb-ref", CONFIG_OPTION, LB_CONFIG, STANDALONE_INSTANCE2));
        runTest(i++ + ".delete-http-lb-server-ref", !asadmin("delete-http-lb-ref", CONFIG_OPTION, LB_CONFIG, STANDALONE_INSTANCE2));

        //delete server-ref's for LB after app and server is disabled
        runTest(i++ + ".disable-http-lb-application", asadmin("disable-http-lb-application", NAME_OPTION , "helloworld", STANDALONE_INSTANCE1));
        runTest(i++ + ".disable-http-lb-application", asadmin("disable-http-lb-application", NAME_OPTION , "helloworld", STANDALONE_INSTANCE2));

        runTest(i++ + ".disable-http-lb-server", asadmin("disable-http-lb-server", STANDALONE_INSTANCE2));

        runTest(i++ + ".delete-http-lb-server-ref", asadmin("delete-http-lb-ref", CONFIG_OPTION, LB_CONFIG, STANDALONE_INSTANCE1));
        runTest(i++ + ".delete-http-lb-server-ref", asadmin("delete-http-lb-ref", CONFIG_OPTION, LB_CONFIG, STANDALONE_INSTANCE2));

        runTest(i++ + ".enable-http-lb-application", asadmin("enable-http-lb-application", NAME_OPTION , "helloworld", STANDALONE_INSTANCE1));
        runTest(i++ + ".enable-http-lb-application", asadmin("enable-http-lb-application", NAME_OPTION , "helloworld", STANDALONE_INSTANCE2));

        //disable/enable application for LB
        runTest(i++ + ".enable-http-lb-application-for-cluster", asadmin("enable-http-lb-application", NAME_OPTION , "helloworld", CLUSTER));
        //deleting cluster ref while app is enabled won't fail
        runTest(i++ + ".create-http-lb-cluster-ref", asadmin("create-http-lb-ref", CONFIG_OPTION, LB_CONFIG, CLUSTER));
        runTest(i++ + ".delete-http-lb-cluster-ref", asadmin("delete-http-lb-ref", CONFIG_OPTION, LB_CONFIG, CLUSTER));

        runTest(i++ + ".disable-http-lb-server", asadmin("disable-http-lb-server", CLUSTER));
        runTest(i++ + ".disable-http-lb-application-for-cluster", asadmin("disable-http-lb-application", NAME_OPTION , "helloworld", CLUSTER));

        //delete ref after app is disabled, should pass
        //runTest(i++ + ".delete-http-lb-cluster-ref", asadmin("delete-http-lb-ref", CONFIG_OPTION, LB_CONFIG, CLUSTER));

        runTest(i++ + ".enable-http-lb-application-for-cluster", asadmin("enable-http-lb-application", NAME_OPTION , "helloworld", CLUSTER));

        //delete the Lb config
        runTest(i++ + ".delete-http-lb-config", asadmin("delete-http-lb-config", LB_CONFIG));

        //create lb-config with target specified
        runTest(i++ + ".create-http-lb-config", asadmin("create-http-lb-config", "--target", STANDALONE_INSTANCE1, LB_CONFIG));

        //re-create should fail
        runTest(i++ + ".create-http-lb-config", !asadmin("create-http-lb-config", "--target", STANDALONE_INSTANCE1, LB_CONFIG));

        //will fail since it contains refs
        runTest(i++ + ".delete-http-lb-config", !asadmin("delete-http-lb-config", LB_CONFIG));

        //remove the refs and delete
        runTest(i++ + ".disable-http-lb-server", asadmin("disable-http-lb-server", STANDALONE_INSTANCE1));
        runTest(i++ + ".disable-http-lb-application-for-server", asadmin("disable-http-lb-application", NAME_OPTION , "helloworld", STANDALONE_INSTANCE1));
        runTest(i++ + ".delete-http-lb-server-ref", asadmin("delete-http-lb-ref", CONFIG_OPTION, LB_CONFIG, STANDALONE_INSTANCE1));
        runTest(i++ + ".delete-http-lb-config", asadmin("delete-http-lb-config", LB_CONFIG));

        //create the load balancer
        runTest(i++ + ".create-http-lb", asadmin("create-http-lb", DEVICEHOST_OPTION, LOCALHOST, DEVICEPORT_OPTION, "9000", LB_NAME));
        ret = asadminWithOutput("list-http-lbs");
        success = ret.out.indexOf(LB_NAME) >= 0;
        runTest(i++ + ".list-http-lbs", success);

        runTest(i++ + ".create-http-lb", !asadmin("create-http-lb", DEVICEHOST_OPTION, LOCALHOST, DEVICEPORT_OPTION, "9000", LB_NAME));

        //delete the load balancer
        runTest(i++ + ".delete-http-lb", asadmin("delete-http-lb", LB_NAME));

        //create the load balancer using all options
        runTest(i++ + ".create-http-lb", asadmin("create-http-lb", DEVICEHOST_OPTION,
                LOCALHOST, DEVICEPORT_OPTION, "9000", "--sslproxyhost", "myhost",
                "--sslproxyport", "6600", "--target", CLUSTER, "--lbpolicy", "user-defined",
                "--lbpolicymodule", "lbmodule.so", "--healthcheckerurl", "/test",
                "--healthcheckerinterval", "60", "--healthcheckertimeout", "20",
                "--lbenableallinstances", "false", "--lbenableallapplications", "false",
                "--lbweight", "cl1-ins1=2:cl1-ins2=3:cl1-ins3=5", "--responsetimeout", "20",
                "--httpsrouting", "true", "--reloadinterval", "30", "--monitor", "true",
                "--routecookie", "false", "--property", "name1=value1:name2=value2",
                LB_NAME));
        ret = asadminWithOutput("list-http-lbs");
        success = ret.out.indexOf(LB_NAME) >= 0;
        runTest(i++ + ".list-http-lbs", success);

        //delete the load balancer
        runTest(i++ + ".delete-http-lb", asadmin("delete-http-lb", LB_NAME));

        //create lb and a config
        asadmin("create-http-lb", DEVICEHOST_OPTION, LOCALHOST,
                DEVICEPORT_OPTION, "9000", TARGET_OPTION, CLUSTER, LB_NAME);

        //set dummy host for load-balancer xml generation
        asadmin("set", "nodes.node." + LB_NODE1 + ".node-host=" + LB_NODE_HOST1);
        asadmin("set", "nodes.node." + LB_NODE2 + ".node-host=" + LB_NODE_HOST2);

        //export-httplb-config tests
        deleteXML(loadbalancerXml);
        asadmin("export-http-lb-config",LB_NAME_OPTION, LB_NAME,
                loadbalancerXml.getAbsolutePath());
        runTest(i++ + ".export-http-lb-config", validateXML(loadbalancerXml,
                CHECKSUM1));

        deleteXML(loadbalancerXml);
        asadmin("export-http-lb-config", CONFIG_OPTION, LB1_CONFIG,
                loadbalancerXml.getAbsolutePath());
        runTest(i++ + ".export-http-lb-config", validateXML(loadbalancerXml,
                CHECKSUM1));

        deleteXML(loadbalancerXml);
        asadmin("export-http-lb-config", TARGETS_OPTION, CLUSTER + ","
                + CLUSTER2, loadbalancerXml.getAbsolutePath());
        runTest(i++ + ".export-http-lb-config", validateXML(loadbalancerXml,
                CHECKSUM2,CHECKSUM2_2));

        deleteXML(loadbalancerXml);
        asadmin("export-http-lb-config", TARGETS_OPTION, CLUSTER + ","
                + CLUSTER2, PROPERTY_OPTION,
                "response-timeout-in-seconds=30:https-routing=true",
                loadbalancerXml.getAbsolutePath());
        runTest(i++ + ".export-http-lb-config", validateXML(loadbalancerXml,
                CHECKSUM3,CHECKSUM3_2));

        deleteXML(loadbalancerXml);
        asadmin("export-http-lb-config", TARGETS_OPTION, STANDALONE_INSTANCE1
                + "," + STANDALONE_INSTANCE2, loadbalancerXml.getAbsolutePath());
        runTest(i++ + ".export-http-lb-config", validateXML(loadbalancerXml,
                CHECKSUM4, CHECKSUM4_2));

        deleteXML(loadbalancerXml);

        //export-http-lb-config negative tests
        runTest(i++ + ".export-http-lb-config", !asadmin("export-http-lb-config",
                loadbalancerXml.getAbsolutePath()));
        runTest(i++ + ".export-http-lb-config", !asadmin("export-http-lb-config",
                CONFIG_OPTION, "junk-lb-config", loadbalancerXml.getAbsolutePath()));
        runTest(i++ + ".export-http-lb-config", !asadmin("export-http-lb-config",
                LB_NAME_OPTION, "junk-lb-name", loadbalancerXml.getAbsolutePath()));
        runTest(i++ + ".export-http-lb-config", !asadmin("export-http-lb-config",
                TARGETS_OPTION, CLUSTER + "," + STANDALONE_INSTANCE1,
                loadbalancerXml.getAbsolutePath()));
        runTest(i++ + ".export-http-lb-config", !asadmin("export-http-lb-config",
                CONFIG_OPTION, LB1_CONFIG, LB_NAME_OPTION, LB_NAME,
                loadbalancerXml.getAbsolutePath()));

        deleteXML(loadbalancerXml);

        //revert dummy host setting
        asadmin("set", "nodes.node." + LB_NODE1 + ".node-host=" + LOCALHOST);
        asadmin("set", "nodes.node." + LB_NODE2 + ".node-host=" + LOCALHOST);

        //delete-lb
        asadmin("delete-http-lb", LB_NAME);

        //undeploy the app
        asadmin("undeploy", "--target", "domain", "helloworld");

        deleteInstances();
        asadmin("stop-domain");
	stat.printSummary();
    }


    private void createInstances() {
        asadmin("create-node-ssh", NODE_HOST_OPTION, LOCALHOST, FORCE_OPTION,
                TRUE, LB_NODE1);
        asadmin("create-node-ssh", NODE_HOST_OPTION, LOCALHOST, FORCE_OPTION,
                TRUE, LB_NODE2);

        asadmin("create-cluster", CLUSTER);

        asadmin("create-instance", CLUSTER_OPTION, CLUSTER, NODE_OPTION,
                LB_NODE1, INSTANCE1);
        asadmin("create-instance", CLUSTER_OPTION, CLUSTER, NODE_OPTION,
                LB_NODE2, INSTANCE2);
        asadmin("create-instance", CLUSTER_OPTION, CLUSTER, NODE_OPTION,
                LB_NODE1, INSTANCE3);

        asadmin("create-cluster", CLUSTER2);

        asadmin("create-instance", CLUSTER_OPTION, CLUSTER2, NODE_OPTION,
                LB_NODE1, INSTANCE4);
        asadmin("create-instance", CLUSTER_OPTION, CLUSTER2, NODE_OPTION,
                LB_NODE2, INSTANCE5);

        asadmin("create-instance", NODE_OPTION, LB_NODE1, STANDALONE_INSTANCE1);
        asadmin("create-instance", NODE_OPTION, LB_NODE2, STANDALONE_INSTANCE2);
    }

    private void deleteInstances() {
        asadmin("delete-instance", STANDALONE_INSTANCE2);
        asadmin("delete-instance", STANDALONE_INSTANCE1);

        asadmin("delete-instance", INSTANCE3);
        asadmin("delete-instance", INSTANCE2);
        asadmin("delete-instance", INSTANCE1);

        asadmin("delete-cluster", CLUSTER);

        asadmin("delete-instance", INSTANCE5);
        asadmin("delete-instance", INSTANCE4);

        asadmin("delete-cluster", CLUSTER2);

        asadmin("delete-node-ssh", LB_NODE1);
        asadmin("delete-node-ssh", LB_NODE2);
        deleteNodeDirectory(LB_NODE1);
        deleteNodeDirectory(LB_NODE2);
    }

    private void runTest(String tName, boolean status) {
        if(!status) {
            System.out.println("ABOVE TEST = " + tName + " FAILED!!!");
            System.out.println("-------------------------------------------------------");
        }
        report(tName, status);
    }

    private boolean validateXML(File loadbalancerXml, String checkSum) {
        try {
            String lbXmlCheckSum = getMD5Checksum(loadbalancerXml);
            System.out.println("lbXmlCheckSum : " + lbXmlCheckSum);
            if(checkSum.equals(lbXmlCheckSum)){
                return true;
            }
        } catch (Exception ex) {
            System.out.println("Unable to get checksum : " + ex.getMessage());
        }
        return false;
    }

    private boolean validateXML(File loadbalancerXml, String checkSum1, String checkSum2) {
        try {
            String lbXmlCheckSum = getMD5Checksum(loadbalancerXml);
            System.out.println("lbXmlCheckSum : " + lbXmlCheckSum);
            if(checkSum1.equals(lbXmlCheckSum) || checkSum2.equals(lbXmlCheckSum)){
                return true;
            }
        } catch (Exception ex) {
            System.out.println("Unable to get checksum : " + ex.getMessage());
        }
        return false;
    }

    public static byte[] createChecksum(File file) throws Exception {
        BufferedReader reader = new BufferedReader(new FileReader(file));
        MessageDigest complete = MessageDigest.getInstance("MD5");
        String line = null;
        while((line = reader.readLine()) != null) {
            System.out.println(line);
            if(line.contains("This file was generated on:")){
                continue;
            }
            complete.update(line.getBytes());
        }
        reader.close();
        return complete.digest();
    }

    public static String getMD5Checksum(File file) throws Exception {
        byte[] b = createChecksum(file);
        String result = "";
        for (int i = 0; i < b.length; i++) {
            result +=
                    Integer.toString((b[i] & 0xff) + 0x100, 16).substring(1);
        }
        return result;
    }

    private void deleteXML(File loadbalancerXml) {
        if(loadbalancerXml.exists()){
            loadbalancerXml.delete();
        }
    }

    private final String host;
    private final File glassFishHome;
    private static final String CLUSTER = "cl1";
    private static final String INSTANCE1 = "cl1-ins1";
    private static final String INSTANCE2 = "cl1-ins2";
    private static final String INSTANCE3 = "cl1-ins3";
    private static final String CLUSTER2 = "cl2";
    private static final String INSTANCE4 = "cl2-ins1";
    private static final String INSTANCE5 = "cl2-ins2";
    private static final String STANDALONE_INSTANCE1 = "ins1";
    private static final String STANDALONE_INSTANCE2 = "ins2";
    private static final String LB_CONFIG = "lb-config1";
    private static final String LOCALHOST="localhost";
    private static final String LB_NAME="lb1";
    private static final String LB1_CONFIG = "lb1_LB_CONFIG";
    private static final String LB_NODE_HOST1 = "dummy-lb-host1";
    private static final String LB_NODE_HOST2 = "dummy-lb-host2";
    private static final String LB_NODE1 = "lb-node1";
    private static final String LB_NODE2 = "lb-node2";
    private static final String TRUE = "true";
    private static final String CONFIG_OPTION="--config";
    private static final String TIMEOUT_OPTION="--timeout";
    private static final String INTERVAL_OPTION="--interval";
    private static final String NAME_OPTION="--name";
    private static final String CLUSTER_OPTION="--cluster";
    private static final String DEVICEHOST_OPTION="--devicehost";
    private static final String DEVICEPORT_OPTION="--deviceport";
    private static final String NODE_OPTION="--node";
    private static final String PROPERTY_OPTION="--property";
    private static final String NODE_HOST_OPTION="--nodehost";
    private static final String FORCE_OPTION="--force";
    private static final String LB_NAME_OPTION="--lbname";
    private static final String TARGETS_OPTION="--lbtargets";
    private static final String TARGET_OPTION="--target";

    private static final String CHECKSUM1 = "6ac4df0a875e5202f190899e8ccf823c";
    private static final String CHECKSUM2 = "b8f0b333dc1b935d8921a420985953b0";
    private static final String CHECKSUM2_2 = "e954ded13fdaa34209a580e332e8ec70";
    private static final String CHECKSUM3 = "4b5d431750cc251bf1920ce46c38ce38";
    private static final String CHECKSUM3_2 = "2dd33443d61de9c51278d685c58fb57f";
    private static final String CHECKSUM4 = "99d66d54e9749427359e2c4b06f63847";
    private static final String CHECKSUM4_2 = "71f922d8e1e6aafc4d6f99c3ae4edb54";
}
