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

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathFactory;
import org.w3c.dom.Document;

/**
 * This class adds devtests for copy-config,delete-config, list-configs commands
 * @author Bhakti Mehta
 */
public class ConfigTests extends AdminBaseDevTest {

    @Override
    protected String getTestDescription() {
        return "Tests Configs using the copy/delete/list config commands.";
    }

    public static void main(String[] args) {
        new ConfigTests().runTests();
    }

    private void runTests() {
        startDomain();
        testCopyDeleteListConfig();
        testDeleteConfig();
        testConfigSysProps();
        testConcurrentConfigAccess();
        testConfigChangeEvents();
        stopDomain();
        stat.printSummary();
    }

    /*
     * This tests a few copy config and delete listconfig options
     */
    private void testCopyDeleteListConfig() {
        //Create cluster, copy its config
        final String testName = "copyDelConfig-";
        final String cluster = "ccl";
        final String destconfig = "destconf";
        report(testName +"create-cl", asadmin("create-cluster", cluster));

        //copy config using existing config
        report(testName+"copy-config", asadmin("copy-config",  cluster+"-config",
                destconfig));

        //check if config is copied
        report (testName+"get", asadmin("get","configs.config."+
                destconfig));
        //check if config is copied   use list-configs
        AsadminReturn ret = asadminWithOutput("list-configs");
        report (testName+"list-configs",ret.out.indexOf(destconfig)!=-1 );

        //delete the config  referenced by cluster should throw error
        report("delete-conf-inuse", !asadmin("delete-config", cluster+"-config"));

        //delete the cluster
        report("delete-cl1", asadmin("delete-cluster", cluster));

        //delete the config
        report("delete-config", asadmin("delete-config", destconfig));

        //try to delete nonexistent config should throw error
        report("delete-config1", !asadmin("delete-config", "junk"));
    }

    /*
     * This tests additional tests like deleting server-config, default-config
     */
    private void testDeleteConfig(){
        final String testName = "deleteConfig";

        //delete default config should throw error
        report(testName+"default-config", !asadmin("delete-config", "default-config"));

        //delete server-config should throw error
        report(testName+"server-config", !asadmin("delete-config", "server-config"));
    }

    /*
     * This tests additional tests to add system props
     */
    private void testConfigSysProps(){
        final String testName = "configSysProps";

        report(testName+"copy-config", !asadmin("copy-config","system-properties","foo=bar", "default-config","myconfig"));

    }

    /*
     * This tests that notifications are delivered for config changes.
     */
    private void testConfigChangeEvents() {
        final String tn = "configChange-";
        AsadminReturn ret;

        report(tn+"create-prop1", asadmin("create-system-properties", "--target", "domain", "someport=1010"));
        report(tn+"create-nl", asadmin("create-network-listener", "--listenerport", "${someport}", "--protocol", "http-listener-1", "somelistener"));
        report(tn+"create-vs", asadmin("create-virtual-server", "--hosts", "localhost", "--networklisteners", "somelistener", "somevs"));
        ret = asadminWithOutput("_get-host-and-port", "--virtualserver", "somevs");
        report(tn+"check-hap1", ret.out.indexOf("1010") != -1);

        report(tn+"setconfig", asadmin("create-system-properties", "--target", "server-config", "someport=2020"));
        ret = asadminWithOutput("_get-host-and-port", "--virtualserver", "somevs");
        report(tn+"check-hap2", ret.out.indexOf("2020") != -1);

        report(tn+"create-prop2", asadmin("create-system-properties", "someport=3030"));
        ret = asadminWithOutput("_get-host-and-port", "--virtualserver", "somevs");
        report(tn+"check-hap3", ret.out.indexOf("3030") != -1);

        report(tn+"delvs", asadmin("delete-virtual-server", "somevs"));
        report(tn+"delnl", asadmin("delete-network-listener", "somelistener"));
        report(tn+"delsp1", asadmin("delete-system-property", "someport"));
        report(tn+"delsp2", asadmin("delete-system-property", "--target", "domain", "someport"));
        report(tn+"delsp1", asadmin("delete-system-property", "--target", "server-config", "someport"));
    }

    /*
     * This tests that the configuration can be modified by multiple asadmin
     * command concurrently without corrupting the domain.xml or missing any
     * updates.
     */
    class AsadminThread extends Thread {
        String cmd;
        String arg;
        boolean result;
        public AsadminThread(String cmd, String arg) {
            this.cmd = cmd;
            this.arg = arg;
        }
        @Override
        public void run() {
            result = asadmin(cmd, arg);
        }
    }
    static private interface ArgGen {
        public String gen(int i);
    }
    private boolean runNCommands(String testName, int num, String cmd, ArgGen arg) {
        AsadminThread threads[] = new AsadminThread[num];
        // create the threads
        for (int i = 0; i < num; i++) {
            threads[i] = new AsadminThread(cmd, arg.gen(i));
        }
        // run them
        for (Thread t : threads) t.start();
        // wait for them to complete
        boolean result = true;
        try {
            for (int i = 0; i < num; i++) {
                threads[i].join();
                report(testName + i, threads[i].result);
                result &= threads[i].result;
            }

        }
        catch (InterruptedException ie) {
            ie.printStackTrace();
            return false;
        }
        return result;
    }

    /*
     * Check to see if num system-properties are either there (if there is true)
     * or not there in the DAS domain.xml.
     */
    private boolean checkForProps(int num, boolean there) {
        try {
            DocumentBuilderFactory domFactory = DocumentBuilderFactory.newInstance();
            domFactory.setNamespaceAware(true); // never forget this!
            DocumentBuilder builder = domFactory.newDocumentBuilder();
            Document doc = builder.parse(getDASDomainXML());
            XPathFactory factory = XPathFactory.newInstance();
            XPath xpath = factory.newXPath();
            boolean pass = true;
            for (int i = 0; i < num; i++) {
                String xpathExpr = "//system-property[@name='foo" + i + "']/attribute::value";
                XPathExpression xexpr = xpath.compile(xpathExpr);
                Object o = xexpr.evaluate(doc, XPathConstants.STRING);
                String expect = there ? "bar" + i : "";
                pass &= o instanceof String && expect.equals((String)o);
            }
            return pass;
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    private void testConcurrentConfigAccess() {
        String tn = "concurrent-";
        int num = 10;
        // create num system properties at the same time
        report(tn + "createprops", runNCommands(tn + "set-", num, "create-system-properties",
                new ArgGen() { public String gen(int i) { return "foo" + i + "=bar" + i; }}));

        // check that the properties were created properly
        report(tn + "allwritten", checkForProps(num, true));

        // delete the properties
        report(tn + "delprops", runNCommands(tn + "del-", num, "delete-system-property",
                new ArgGen() { public String gen(int i) { return "foo" + i; }}));

        // check that the properties were deleted properly
        report(tn + "alldeleted", checkForProps(num, false));
    }
}
