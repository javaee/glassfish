/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2012-2017 Oracle and/or its affiliates. All rights reserved.
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

import java.util.HashMap;
import java.util.Map;

/**
 * Testing configuration modularity related commands.
 *
 * @author Masoud Kalali
 */
public class ConfigurationModularityTest extends AdminBaseDevTest {

    String EJB_MULTI_PART_CONFIG =
            "At location: domain/configs/server-config\n"
            + "<ejb-container>\n"
            + "  <ejb-timer-service/>\n"
            + "</ejb-container>\n"
            + "At location: domain/resources\n"
            + "    <jdbc-resource pool-name=\"__TimerPool\" jndi-name=\"jdbc/__TimerPool\" object-type=\"system-admin\"></jdbc-resource>\n"
            + "At location: domain/resources\n"
            + "    <jdbc-connection-pool datasource-classname=\"org.apache.derby.jdbc.EmbeddedXADataSource\" res-type=\"javax.sql.XADataSource\" name=\"__TimerPool\">\n"
            + "      <property name=\"databaseName\" value=\"${com.sun.aas.instanceRoot}/lib/databases/ejbtimer\"></property>\n"
            + "      <property name=\"connectionAttributes\" value=\";create=true\"></property>\n"
            + "    </jdbc-connection-pool>\n"
            + "At location: domain/servers/server[server]\n"
            + "<resource-ref ref=\"jdbc/__TimerPool\"></resource-ref>";
    String DEFAULT_EJB_CONTAINER_SINGLE_PART =
            "<ejb-container>"
            + "<ejb-timer-service/>"
            + "</ejb-container>";
    String DEFAULT_WEB_CONTAINER_COMMAND_OUT =
            "<web-container>\n"
            + "  <session-config>\n"
            + "    <session-manager>\n"
            + "      <manager-properties/>\n"
            + "    </session-manager>\n"
            + "    <session-properties/>\n"
            + "  </session-config>\n"
            + "</web-container>";
    String DEFAULT_WEB_CONTAINER_COMMAND_OUT_ALTER_1 =
            "<web-container>\n"
            + "  <session-config>\n"
            + "    <session-manager>\n"
            + "      <manager-properties/>\n"
            + "      <store-properties/>\n"
            + "    </session-manager>\n"
            + "    <session-properties/>\n"
            + "  </session-config>\n"
            + "</web-container>";
    private static final String DEFAULT_JMS_SERVICE =
            "<jms-service default-jms-host=\"default_JMS_host\" type=\"EMBEDDED\">\n" +
                "  <jms-host port=\"7676\" host=\"localhost\" name=\"default_JMS_host\"/>\n" +
                "</jms-service>";
    private static final String DEFAULT_TRANSACTION_SERVICE = "<transaction-service automatic-recovery=\"true\"/>";
    Map<String, String> serviceToTest = new HashMap<String, String>(4);

    public ConfigurationModularityTest() {
        serviceToTest.put("ejb-container", DEFAULT_EJB_CONTAINER_SINGLE_PART);
        serviceToTest.put("web-container", DEFAULT_WEB_CONTAINER_COMMAND_OUT);
        serviceToTest.put("transaction-service", DEFAULT_TRANSACTION_SERVICE);
        serviceToTest.put("jms-service", DEFAULT_JMS_SERVICE);
    }

    @Override
    protected String getTestDescription() {
        return "Unit test for Zero Config Command line features";
    }

    public static void main(String[] args) throws InterruptedException {
        new ConfigurationModularityTest().runTests();
    }

    private void runTests() {
        startDomain();
        cleanUpModuleConfigsIfExist();
        checkCreateModuleConfigCommand();
        checkDeleteModuleConfigCommand();
//        checkCreateModuleConfigDryRunCommand();
//        checkDryRunNotCausingMerger();
        checkGetActiveConfigCommand();
        stopDomain();
        stat.printSummary();
    }

    private void cleanUpModuleConfigsIfExist() {

        for (Map.Entry<String, String> entry : serviceToTest.entrySet()) {
            String serviceName = entry.getKey();
            asadminWithOutput("delete-module-config", serviceName);
        }
    }

    private void checkCreateModuleConfigCommand() {
        int i = 0;
        for (Map.Entry<String, String> entry : serviceToTest.entrySet()) {
            i++;
            String serviceName = entry.getKey();
            report("Case " + i + ": creating default configuration for: " + serviceName, asadmin("create-module-config", serviceName));
        }
    }

    private void checkDeleteModuleConfigCommand() {
        int i = 0;
        for (Map.Entry<String, String> entry : serviceToTest.entrySet()) {
            i++;
            String serviceName = entry.getKey();
            report("Case " + i + ": delete default configuration for: " + serviceName, asadmin("delete-module-config", serviceName));
        }
    }

    private void checkCreateModuleConfigDryRunCommand() {
        int i = 0;
        for (Map.Entry<String, String> entry : serviceToTest.entrySet()) {
            i++;
            String serviceName = entry.getKey();
            String serviceConfig = entry.getValue();
            AsadminReturn returnee = asadminWithOutput("create-module-config", "--dryRun", serviceName);
            if (serviceName.equals("web-container")) {
                compareResponseReport("Case " + i + ": Checking the dryRun for: " + serviceName, returnee, serviceConfig,
                        DEFAULT_WEB_CONTAINER_COMMAND_OUT_ALTER_1);
            } else {
                compareResponseReport("Case " + i + ": Checking the dryRun for: " + serviceName, returnee, serviceConfig);
            }
        }
    }

    private void checkDryRunNotCausingMerger() {

        asadmin("create-module-config", "--dryRun", "ejb-container");
        AsadminReturn returnee = asadminWithOutput("delete-module-config", "ejb-container");
        report("Checking Dry Run not Causing Merger: ", !returnee.returnValue);
    }

    private void checkGetActiveConfigCommand() {
        String customizedJMServiceConfig =
                "<jms-service init-timeout-in-seconds=\"120\" default-jms-host=\"default_JMS_host\" type=\"EMBEDDED\">\n"
                + "  <jms-host port=\"7676\" host=\"localhost\" name=\"default_JMS_host\"/>\n"
                + "</jms-service>";

//        asadmin("delete-module-config", "web-container");
//        //get the active config for web container which should be the default config
//        AsadminReturn returnee = asadminWithOutput("get-active-config", "web-container");
//        compareResponseReport("Case 1: getting active config for a default web-container not present in domain.xml ",
//                returnee, DEFAULT_WEB_CONTAINER_COMMAND_OUT, DEFAULT_WEB_CONTAINER_COMMAND_OUT_ALTER_1);
//
//
//        asadmin("delete-module-config", "jms-service");
//        asadmin("create-module-config", "jms-service");
//        AsadminReturn returnee2 = asadminWithOutput("get-active-config", "jms-service");
//        compareResponseReport("Case 2: getting active config for jms-service present in domain.xml ",
//                returnee2, DEFAULT_JMS_SERVICE);
//
//        asadmin("set", "server-config.jms-service.init-timeout-in-seconds=120");
//        AsadminReturn returnee3 = asadminWithOutput("get-active-config", "jms-service");
//        compareResponseReport("Case 3: getting a customized active configuration for jms-service",
//                returnee3, customizedJMServiceConfig);

    }

    private void compareResponseReport(String testName, AsadminReturn result, String... expected) {
        boolean testResult = false;
        for (int i = 0; i < expected.length; i++) {
            String string = expected[i];
            if (result.out.contains(string)) {
                testResult = true;
                break;
            }
        }
        report(testName, testResult);
    }
}
