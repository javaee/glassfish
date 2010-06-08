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

import com.sun.appserv.test.BaseDevTest;
import java.io.*;
import java.net.*;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

/*
 * Dev test for create/delete/list instance
 * @author Bhakti Mehta
 * @author Byron Nevins
 */
public class AdminInfraTest extends BaseDevTest {

    public AdminInfraTest() {
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
        // it does NOT need to exist -- do not insist!
        instancesHome = new File(new File(glassFishHome, "nodeagents"), host);
        printf("GF HOME = " + glassFishHome);
    }

    public static void main(String[] args) {
        new AdminInfraTest().run();
    }

    @Override
    public void report(String name, boolean success) {
        // bnevins june 6 2010

        // crazy base class uses a Map to store these reports.  If you use
        // the same name > 1 time they are ignored and thrown away!!!
        // I went with this outrageous kludge because (1) it is just tests
        // and (2) there are tens of thousands of other files in this harness!!!

        // another issue is hacking off strings after a space.  Makes no sense to me!!

        String name2 = name.replace(' ', '_');
        if (!name2.equals(name)) {
            System.out.println("Found spaces in the name.  Replaced with underscore. "
                    + "before: " + name + ", after: " + name2);
            name = name2;   // don't foul logic below!
        }

        int i = 0;

        while (reportNames.add(name2) == false) {
            name2 = name + i++;
        }

        if (!name2.equals(name)) {
            System.out.println("Duplicate name found (" + name
                    + ") and replaced with: " + name2);
        }

        super.report(name2, success);
    }

    @Override
    public void report(String step, AsadminReturn ret) {
        report(step, ret.returnValue);
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
        try {
            startDomain();
            testDuplicateReportNames();
            testNamesWithSpaces();
            create();
            delete();
            createFail();
            testNoCreateForStop();
            createStartStopDelete();
            stat.printSummary();
        }
        finally {
            stopDomain();
        }
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

    private void startDomain() {
        domain1WasRunning = asadmin("start-domain", "domain1");

        if (domain1WasRunning) {
            printf("\n*******  IGNORE THE SCARY ERROR ABOVE !!!!!!\n"
                    + "domain1 was already running.  It will not be stopped "
                    + "at the end of the tests.\n******\n");
        }
        else {
            printf("domain1 was started.");
        }

    }

    private void stopDomain() {
        if (!domain1WasRunning) {
            report("stop-domain-xxxx", asadmin("stop-domain", "domain1"));
        }
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
        }

        report("list-instance-after-create", asadmin("list-instances"));

        printf("Check " + instanceNames[0] + " is registered to DAS");
        report(instanceNames[0] + "-yes-regdas", asadmin("get", "servers.server." + instanceNames[0]));

        printf("Check " + instanceNames[0] + "-config exists.");
        report(instanceNames[0] + "-yes-config", asadmin("get", "configs.config." + instanceNames[0] + "-config"));

        printf("Check " + instanceNames[0] + " has config-ref set.");
        AsadminReturn ret = asadminWithOutput("get", "servers.server." + instanceNames[0] + ".config-ref");
        System.out.println(ret.outAndErr);
        boolean success = ret.outAndErr.indexOf("servers.server." + instanceNames[0] + ".config-ref=" + instanceNames[0] + "-config") >= 0;
        report(instanceNames[0] + "-yes-configref", success);

        report("das-properties-exists-after-create", checkDasProperties());
    }

    private void createFail() {
        //printf("create-local-instance with wrong host");
        //report("create-local-instance-wronghost", !asadmin("--host", "wronghost", "create-local-instance", "instancefail"));

        printf("create-local-instance with non-existent cluster");
        report("create-local-instance-nosuchcluster", !asadmin("create-local-instance", "--cluster", "nocluster", "noinstance"));
        report("cleanup-failed-c-l-i", checkInstanceDir("noinstance"));
    }

    private void delete() {
        printf("Delete " + instanceNames.length + " instances");
        for (String iname : instanceNames) {
            report(iname + "-yes-dir", checkInstanceDir(iname));
            report(iname + "-delete", asadmin("delete-local-instance", iname));
            report(iname + "-no-dir-again", !checkInstanceDir(iname));
            report(iname + "-no-regdas", !asadmin("get", "servers.server." + iname));
            report(iname + "-no-config", !asadmin("get", "configs.config." + iname + "-config"));
        }

        AsadminReturn ret = asadminWithOutput("list-instances");
        System.out.println(ret.outAndErr);
        boolean success = ret.outAndErr.indexOf("Nothing to list.") >= 0;

        //report("list-instance-after-delete", success);

    }

    private boolean checkInstanceDir(String name) {
        File inf = new File(instancesHome, name);
        boolean exists = inf.isDirectory();
        String existsString = exists ? "DOES exist" : "does NOT exist";
        return exists;
    }

    private boolean checkDasProperties() {
        File dasFile = new File(instancesHome, "agent" + File.separator + "config" + File.separator + "das.properties");
        return dasFile.exists();
    }

    private String generateInstanceName() {
        String s = "" + System.currentTimeMillis();
        s = s.substring(4, 10);
        return "in_" + s;
    }

    private void printf(String fmt, Object... args) {
        if (DEBUG) {
            System.out.printf("**** DEBUG MESSAGE ****  " + fmt + "\n", args);
        }
    }
    private final String host;
    private final File glassFishHome;
    private final File instancesHome;
    private boolean domain1WasRunning;
    private final static boolean DEBUG;
    private static final String[] instanceNames;
    private final SortedSet<String> reportNames = new TreeSet<String>();
    private static final int NUM_INSTANCES = 1;
    private final static boolean isHudson = Boolean.parseBoolean(System.getenv("HUDSON"));

    static {
        String name = System.getProperty("user.name");

        if (name != null && name.equals("bnevins"))
            DEBUG = true;
        else if(isHudson)
            DEBUG = true;
        else if(Boolean.parseBoolean(System.getenv("AS_DEBUG")))
            DEBUG = true;
        else
            DEBUG = false;

        instanceNames = new String[NUM_INSTANCES];

        for (int i = 0; i < NUM_INSTANCES; i++) {
            instanceNames[i] = "instance_" + i;
        }
    }
}
