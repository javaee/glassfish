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

import java.net.InetAddress;
import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.Set;
import java.util.HashMap;
import java.util.ArrayList;
import java.util.Arrays;

/**
 * This class adds devtests for create-node-ssh, delete-node-ssh,
 * create-node-config and delete-node-config.
 *
 * @author Joe Di Pol
 */
public class NodeTest extends AdminBaseDevTest {

    private static final String NL = System.getProperty("line.separator");

    private String thisHost = null;
    private String thisUser = null;
    private File productInstallRoot = null;

    // This table maps command line options to property names. The property
    // name in the table will be precedded with nodes.node.<nodename>.
    // when used.
    String propPrefix = "nodes.node.";
    String[] propTable = {
        "--nodehost", "node-host",
        "--installdir", "install-dir",
        "--nodedir", "node-dir",
        "--sshport", "ssh-connector.ssh-port",
        "--sshuser", "ssh-connector.ssh-auth.user-name",
        "--sshkeyfile", "ssh-connector.ssh-auth.keyfile",
        // Not supported yet
        "--sshnodehost", "ssh-connector.ssh-auth.node-host",
        "--sshpassphrase", "ssh-connector.ssh-auth.passphrase",
        "--sshpassword", "ssh-connector.ssh-auth.password"
    };

    // A map form of the above array table.
    Map<String, String> propMap;

    NodeTest () {
        try {
            thisHost = InetAddress.getLocalHost().getHostName();
        }
        catch (Exception e) {
            thisHost = "localhost";
        }

        File glassfishHome = null;
        try {
            glassfishHome = getGlassFishHome().getCanonicalFile();
        } catch (IOException e) {
            glassfishHome = getGlassFishHome().getAbsoluteFile();
        }

        productInstallRoot = glassfishHome.getParentFile();

        thisUser = System.getProperty("user.name");

        // Maps command line options to property names used in domain.xml
        propMap = arrayToMap(propTable);
    }


    @Override
    protected String getTestDescription() {
        return "Tests Node configuration using create-node-*/delete-node-*";
    }

    public static void main(String[] args) {
        new NodeTest().runTests();
    }

    private void runTests() {
        startDomain();
        testLocalHostNode();
        testCreateNodeSsh();
        testCreateNodeConfig();
        testValidateNodeCommand();
        testBasicCluster();
        stopDomain();
        stat.printSummary();
    }

    /*
     * Tests create-node-ssh and delete-node-ssh
     */
    private void testCreateNodeSsh() {
        final String testNamePrefixCreate = "createNodeSsh-";
        final String testNamePrefixDelete = "deleteNodeSsh-";
        final String testNamePrefixValidate = "validateNodeSsh-";
        final String testNamePrefixValidateDelete = "validateDeleteNodeSsh-";
        final String nodeNamePrefix = "n_ssh_";

        // List of CLI options to test create-node-ssh with.
        // Each row represents one test. We run create-node-ssh with the
        // options and then verify the results.
        // We use --force to suppress validation errors by create-node-ssh
        // since for this test we just care that the values we provide are
        // reflected in the configurtion.
        String[][] testTable = {
            {"--nodehost", thisHost, "--installdir", productInstallRoot.getAbsolutePath(),
              "--force", "true"},
            {"--nodehost", thisHost, "--installdir", productInstallRoot.getAbsolutePath(),
              "--sshuser", thisUser,
              "--sshkeyfile", "/any/old/path",
              "--sshport", "22",
              "--nodedir", "/another/any/old/path",
              "--force", "true"},
        };

        for (int i = 0; i < testTable.length; i++) {
            String testName = String.format("%s%d", testNamePrefixCreate, i);
            String nodeName = String.format("%s%d", nodeNamePrefix, i);

            // Prepend the arguments with create-node-ssh and append the
            // node name.
            ArrayList<String> args =
                    new ArrayList<String>(Arrays.asList(testTable[i]));
            args.add(0, "create-node-ssh");
            args.add(nodeName);
            String[] argsArray = new String[args.size()];
            argsArray = args.toArray(argsArray);

            // Run create-node-ssh command
            report(testName, asadmin(argsArray));

            // Validate node
            testName = String.format("%s%d", testNamePrefixValidate, i);
            try {
                validateNode(nodeName, arrayToMap(testTable[i]));
                report(testName, true);
            } catch (IllegalArgumentException e) {
                System.out.println("ERROR: " + e.getMessage());
                report(testName, false);
            }

            // Delete node
            testName = String.format("%s%d", testNamePrefixDelete, i);
            report(testName, asadmin("delete-node-ssh", nodeName));

            // Make sure node is gone
            testName = String.format("%s%d", testNamePrefixValidateDelete, i);
            report(testName, !asadmin("get", propPrefix + nodeName + "." + "name"));
        }
    }

    /*
     * Tests create-node-config and delete-node-config
     */
    private void testCreateNodeConfig() {
        final String testNamePrefixCreate = "createNodeConfig-";
        final String testNamePrefixDelete = "deleteNodeConfig-";
        final String testNamePrefixValidate = "validateNodeConfig-";
        final String testNamePrefixValidateDelete = "validateDeleteNodeConfig-";
        final String nodeNamePrefix = "n_config_";

        String nodeName = String.format("%s%d", nodeNamePrefix, 0);

        // Create a config node
        String testName = testNamePrefixCreate + "0";
        report(testName, asadmin("create-node-config", nodeName));

        // Make sure it is there by getting its name
        testName = testNamePrefixValidate + "0";
        report(testName, asadmin("get", propPrefix + nodeName + "." + "name"));

        // Delete it
        testName = testNamePrefixDelete + "0";
        report(testName, asadmin("delete-node-config", nodeName));

        // Make sure it is gone by trying to get its name
        testName = testNamePrefixValidateDelete + "0";
        report(testName, !asadmin("get", propPrefix + nodeName + "." + "name"));
    }

    private void testLocalHostNode() {
        final String LOCALHOST = "localhost-domain1";

        // Verify localhost node exists
        report("check-for-node-localhost", asadmin("get", propPrefix +
                LOCALHOST + "." + "name"));

        // Validate localhost nodehost
        String testName = "check-for-node-localhost-nodehost";
        String property = propPrefix + LOCALHOST + "." + "node-host";
        AsadminReturn ret = asadminWithOutput("get", property);
        // Parse the asadmin output to get the property value
        String propertyValue = getPropertyValue(property, ret.out);
        if (propertyValue.equals("localhost")) {
            report(testName, true);
        } else {
            System.out.printf("ERROR: node %s: %s == %s. Expected %s\n",
                    LOCALHOST, property, propertyValue, "localhost");
            report(testName, false);
        }

        // Verify you can't delete node "localhost"
        report("delete-node-localhost-neg",
                ! asadmin("delete-node-ssh", LOCALHOST));

        // Verify you can't create node "localhost"
        report("create-node-localhost-neg",
                ! asadmin("create-node-config", LOCALHOST));
    }

    private void testBasicCluster() {
        final String LOCALHOST = "localhost-domain1";
        final String CNAME = "ccc_node_test_1";
        final String INAME1 = "iii_node_test_1";
        final String INAME2 = "iii_node_test_2";

        // Do a basic cluster test. Create a cluster with two
        // local instances. Start the cluster, stop it, then remove
        // everything.
        report("node-create-cluster", asadmin("create-cluster", CNAME));

        report("node-create-instance1", asadmin("create-instance",
                        "--node", LOCALHOST,
                        "--cluster", CNAME,
                        INAME1));

        report("node-create-instance2", asadmin("create-instance",
                        "--node", LOCALHOST,
                        "--cluster", CNAME,
                        INAME2));

        System.out.printf("Starting cluster %s\n", CNAME);
        report("node-start-cluster1", asadmin("start-cluster", CNAME));

        AsadminReturn ret = asadminWithOutput("list-instances", "--long");
        System.out.printf("After start-cluster list-instances returned:\n%s\n",
                ret.out);

        report("node-check-instance1", isInstanceRunning(INAME1));
        report("node-check-instance2", isInstanceRunning(INAME2));
        report("node-check-cluster1", isClusterRunning(CNAME));

        report("node-stop-cluster1", asadmin("stop-cluster", CNAME));

        ret = asadminWithOutput("list-instances", "--long");
        System.out.printf("After stop-cluster list-instances returned:\n%s\n",
                ret.out);

        report("node-check-stopped-instance1", ! isInstanceRunning(INAME1));
        report("node-check-stopped-instance2", ! isInstanceRunning(INAME2));
        report("node-check-stopped-cluster1", ! isClusterRunning(CNAME));

        report("node-delete-instance1", asadmin("delete-instance", INAME1));
        report("node-delete-instance2", asadmin("delete-instance", INAME2));
        report("node-delete-cluster1", asadmin("delete-cluster", CNAME));
    }

    /*
     * Tests create-node-config and delete-node-config
     */
    private void testValidateNodeCommand() {

        String testNamePrefix = "node-validate-command-";
        String nodeName = "n_manhattan";
        String INSTALL_DIR = "/foo/bar";
        String NODE_DIR = "/foo/bar/node/dir";
        String NODE_HOST = "foobar.com";
        String NODE_HOST_2 = "FooBar.com";

        // Create a config node
        report(testNamePrefix + "create", asadmin("create-node-config", nodeName));

        // Validate node and add nodehost, installdir and nodedir
        report(testNamePrefix + "_validate_add_host", asadmin("_validate-node",
                "--nodehost", NODE_HOST,
                "--installdir", INSTALL_DIR,
                "--nodedir", NODE_DIR,
                nodeName));

        // Second call should validate fields.
        report(testNamePrefix + "_validate_validate_all", asadmin("_validate-node",
                "--nodehost", NODE_HOST,
                "--installdir", INSTALL_DIR,
                "--nodedir", NODE_DIR,
                nodeName));

        // If hostname changes case that's OK
        report(testNamePrefix + "_validate_ok_host", asadmin("_validate-node",
                "--nodehost", NODE_HOST_2,
                "--installdir", INSTALL_DIR,
                "--nodedir", NODE_DIR,
                nodeName));

        // If hostname changes that's not OK
        report(testNamePrefix + "_validate_bad_host", ! asadmin("_validate-node",
                "--nodehost", "fffoobar.com",
                "--installdir", INSTALL_DIR,
                "--nodedir", NODE_DIR,
                nodeName));

        // If installdir changes that's not OK
        report(testNamePrefix + "_validate_bad_installdir", ! asadmin("_validate-node",
                "--nodehost", NODE_HOST,
                "--installdir", "/fooooooo/bar",
                "--nodedir", NODE_DIR,
                nodeName));

        // If nodedir changes that's not OK
        report(testNamePrefix + "_validate_bad_nodedir", ! asadmin("_validate-node",
                "--nodehost", NODE_HOST,
                "--installdir", INSTALL_DIR,
                "--nodedir", "/foooo/bar",
                nodeName));

        // If node doesn't exist that is not OK
        report(testNamePrefix + "_validate_missing_node", ! asadmin("_validate-node",
                "bogusnodename"));

        // Delete it
        report(testNamePrefix + "_delete", asadmin("delete-node-config", nodeName));
    }

    /**
     * Validate that a created node's configuration matches what was passed
     * on the command line.
     *
     * @param nodeName Name of node
     * @param inputParameters Parameters that were passed to create-node-ssh
     */
    private void validateNode(String nodeName, Map<String, String> inputParameters)
        throws IllegalArgumentException {

        Set<String> keys = inputParameters.keySet();
        StringBuilder eMsg = new StringBuilder();
        boolean error = false;
        boolean first = true;

        // Loop through all input options. Keys are the CLI options
        for (String k : keys) {
            String option = k;
            String value = inputParameters.get(k);
            if ( !propMap.containsKey(k)) {
                // Looks like this CLI option is not something we verify
                // against the configuration (like --force)
                continue;
            }

            // Create the domain.xml property name and get the property
            String property = propPrefix + nodeName + "." + propMap.get(k);
            AsadminReturn ret = asadminWithOutput("get", property);

            // Parse the asadmin output to get the property value
            String propertyValue = getPropertyValue(property, ret.out);

            // Check results
            if (ret.returnValue == false || ! propertyValue.equals(value)) {
                error = true;
                if (!first) {
                    eMsg.append(NL);
                    first = false;
                }
                if (ret.returnValue == false) {
                    // asadmin had an error
                    eMsg.append("Could not get property " + property +
                        ". asadmin says: " + ret.outAndErr);
                } else {
                    // Property value did not match what we expected
                    eMsg.append("Property " + property + " with value " +
                        propertyValue + " does not match cli option " +
                        option + " with value " + value);
                }
                first = false;
            } else {
                //System.out.printf("Property %s had correct value %s\n", property, propertyValue);
            }
        }

        if (error) {
            throw new IllegalArgumentException(eMsg.toString());
        }
    }

    /**
     * Converts a Map into an array where the first item is the
     * first key, the second item is the first value, etc.
     */
    private String[] mapToArray(Map<String, String> m) {

        // Array to hold keys and values
        String [] results = new String[m.size() * 2];

        Set<String> keys = m.keySet();

        int i = 0;
        for (String k : keys) {
            results[i] = k;
            i++;
            results[i] = m.get(k);
            i++;
        }
        return results;
    }

    /**
     * Converts an array where the first item is the
     * first key, the second item is the first value, etc.
     * into a Map.
     */
    private Map<String, String> arrayToMap(String [] a) {

        // Map to hold keys and values
        Map<String, String> results = new HashMap<String, String>();

        for (int i = 0; i < a.length; ) {
            results.put(a[i], a[i + 1]);
            i += 2;
        }
        return results;
    }

    /**
     * Extract a property value from the output of asadmin get
     * @param property  Name of property to return value for
     * @param buffer    Buffer that contains 'asadmin get' output
     * @return  Property value in buffer, or null if property not found
     */
    private String getPropertyValue(String property, String buffer) {

        int index = buffer.lastIndexOf(property + "=");
        String propertyValue = buffer.substring(index + property.length() + 1);

        return propertyValue.trim();
    }
}
