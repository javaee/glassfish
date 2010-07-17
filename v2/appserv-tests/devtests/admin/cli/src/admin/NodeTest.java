/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Sun Microsystems, Inc. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License. You can obtain
 * a copy of the License at https://glassfish.dev.java.net/public/CDDL+GPL.html
 * or glassfish/bootstrap/legal/LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 *
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at glassfish/bootstrap/legal/LICENSE.txt.
 * Sun designates this particular file as subject to the "Classpath" exception
 * as provided by Sun in the GPL Version 2 section of the License file that
 * accompanied this code.  If applicable, add the following below the License
 * Header, with the fields enclosed by brackets [] replaced by your own
 * identifying information: "Portions Copyrighted [year]
 * [name of copyright owner]"
 *
 * Contributor(s):
 *
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
    private String localHost = "localhost";
    private String glassfishHome = null;

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
        "--sshnodehost", "ssh-connector.ssh-auth.node-host",
        // Not supported yet
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

        try {
            glassfishHome = getGlassFishHome().getCanonicalPath();
        } catch (IOException e) {
            glassfishHome = getGlassFishHome().getAbsolutePath();
        }

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
        testCreateNodeSsh();
        testCreateNodeConfig();
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
            {"--nodehost", thisHost, "--installdir", glassfishHome,
              "--force", "true"},
            {"--nodehost", thisHost, "--installdir", glassfishHome,
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