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
            host0 = InetAddress.getLocalHost().getHostName();
        }
        catch (Exception e) {
            host0 = "localhost";
        }
        host = host0;
        System.out.println("Host= " + host);
        glassFishHome = getGlassFishHome();
        domainHome = new File(glassFishHome, "domains/domain1");    // yes it is hard-coded!!
        // it does NOT need to exist -- do not insist!
        instancesHome = new File(new File(glassFishHome, "nodeagents"), host);
        printf("GF HOME = " + glassFishHome);
    }

    public static void main(String[] args) {
        new InstanceTest().run();
    }

    @Override
    protected String getTestName() {
        return "instance";
    }

    @Override
    protected String getTestDescription() {
        return "Unit test for create/delete/list instance";
    }

    public void run() {
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

    private void createStartStopDelete() {
        String metname = "createStartStopDelete";
        String iname = generateInstanceName();

        report(metname + "-nodir-xxxx", !checkInstanceDir(iname));
        asadmin("stop-local-instance", iname); // in case it's running?!?
        report(metname + "-nodir", !checkInstanceDir(iname));
        report(metname + "-create", asadmin("create-local-instance", iname));
        report(metname + "-yesdir", checkInstanceDir(iname));

        report(metname + "-start", asadmin("start-local-instance", iname));
        report(metname + "-list-instances", isInstanceRunning(iname));
        report(metname + "-stop", asadmin("stop-local-instance", iname));

        if (!asadmin("delete-local-instance", iname)) {
            if (File.separatorChar == '\\') {
                System.out.println("&&&&&&&&&   SKIPPING TWO  TESTS  $$$$$$$$$$$$$$");
                for (int i = 0; i < 25; i++) {
                    System.out.println("*****  FIX ISSUE 12160 -- I can't delete the instance!! ***** ");
                }
            }
        }
        else {
            report(metname + "-delete", true);
            report(metname + "-no-dir-again", !checkInstanceDir(iname));
        }
    }

    /**
     *
     * typical output as og 6/6/10
    C:\glassfishv3\glassfish\nodeagents\vaio>asadmin list-instances
    Instance Name   Host                           Admin Port      Current State
    ---------------|------------------------------|---------------|--------------------
    i20             vaio                           24848           Uptime: 9 minutes, 36 seconds
    in_879669       vaio                           24848           Not Running
     */
    private boolean isInstanceRunning(String iname) {
        AsadminReturn ret = asadminWithOutput("list-instances");
        String[] lines = ret.out.split("[\r\n]");

        for (String line : lines) {
            if (line.indexOf(iname) >= 0) {
                printf("Line from list-instances = " + line);
                return line.indexOf("Uptime") >= 0;
            }
        }
        return false;
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
        report("create-instance-success", asadmin("create-instance", "--nodeagent", "mrbean", iname));
        report("create-instance-regdas", asadminWithOutput("get", "servers.server." + iname));
        report("create-instance-config", asadminWithOutput("get", "configs.config." + iname + "-config"));

        AsadminReturn ret = asadminWithOutput("get", "servers.server." + iname + ".config-ref");
        boolean success = ret.outAndErr.indexOf("servers.server." + iname + ".config-ref=" + iname + "-config") >= 0;
        report("create-instance-configref", success);

        ret = asadminWithOutput("get", "servers.server." + iname + ".node-agent-ref");
        success = ret.outAndErr.indexOf("servers.server." + iname + ".node-agent-ref=mrbean") >= 0;
        report("create-instance-nodeagentref", success);

        report("create-instance-existsAlready", !asadmin("create-instance", "--nodeagent", "mrbean", iname));

        createAdminCommandSystemProperties();
        createAdminCommandClusterConfig();
    }

    private void createAdminCommandSystemProperties() {
        printf("Call remote AdminCommand create-instance with system properties");
        String iname = "instancewithsysprops";

        report("create-instance-sysprops", asadminWithOutput("create-instance", "--nodeagent",
                "mrbean", "--systemproperties", "prop1=valA:prop2=valB:prop3=valC", iname));

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

        report("create-instance-forcluster", asadmin("create-instance", "--nodeagent",
                "mrbean", "--cluster", "jencluster", iname));

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
        report("create-instance-nodeagentRequired", !asadmin("create-instance", iname));
        report("create-instance-nosuchcluster", !asadmin("create-instance", "--nodeagent", "mrbean", "--cluster", "nosuchcluster", iname));
        report("create-instance-nosuchconfig", !asadmin("create-instance", "--nodeagent", "mrbean", "--config", "nosuchconfig", iname));
        report("create-instance-clusterandconfig", !asadmin("create-instance", "--nodeagent", "mrbean", "--cluster", "c1", "--config", "config1", iname));
    }

    private void deleteAdminCommandFail() {
        printf("Call remote AdminCommand delete-instance with bad params");
        String iname = "nosuchinstance";
        report("delete-instance-fail", !asadmin("delete-instance", iname));
    }

    private boolean checkInstanceDir(String name) {
        File inf = new File(instancesHome, name);
        boolean exists = inf.isDirectory();
        return exists;
    }

    private boolean checkDasProperties() {
        File dasFile = new File(instancesHome, "agent" + File.separator + "config" + File.separator + "das.properties");
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
    private final File instancesHome;
    private final File domainHome;
    private static String[] instanceNames;
    private static final int DEFAULT_NUM_TESTS = 2;

}
