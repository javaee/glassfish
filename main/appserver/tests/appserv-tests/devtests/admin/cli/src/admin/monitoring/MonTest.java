/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2011-2012 Oracle and/or its affiliates. All rights reserved.
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
package admin.monitoring;

import admin.AdminBaseDevTest;
import admin.TestEnv;
import com.sun.appserv.test.BaseDevTest.AsadminReturn;
import java.io.*;
import java.net.*;
import java.nio.channels.*;
import static admin.monitoring.Constants.*;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * ALL Monitoring DevTests must implement this interface
 *
 * @author Byron Nevins
 */
abstract class MonTest {

    private static boolean stopWaiting = false;
    private static boolean waitedOnce;

    abstract void runTests(TestDriver driver);

    final void setDriver(TestDriver td) {
        driver = td;
        name = getClass().getName();
        name = name.substring(name.lastIndexOf('.') + 1);
    }

    final void report(boolean b, String... names) {
        // throw NPE if you forgot to call setDriver.  This is, after all, just
        // tests!
        if (driver == null) {
            System.out.println("*************************************************");
            System.out.println("*************************************************");
            System.out.println("You forgot to call setDriver for " + getClass().getName());
            System.out.println("*************************************************");
            System.out.println("*************************************************");
            System.out.println("*************************************************");
            Runtime.getRuntime().halt(1);
        }
        driver.report(join(names), b);

        if (!b) {
            handleBadTest();
        }
    }

    final boolean asadmin(String... cmd) {
        return driver.asadmin(cmd);
    }

    final AsadminReturn asadminWithOutput(String... cmd) {
        return driver.asadminWithOutput(cmd);
    }

    final boolean asadminHasString(String find, String... cmd) {
        AsadminReturn ret = driver.asadminWithOutput(cmd);
        return ret.outAndErr.indexOf(find) >= 0;
    }

    final boolean doesGetMatch(String dottedName, String value) {
        return driver.doesGetMatch(dottedName, value);
    }

    final boolean doesGetMatch(String dottedName) {
        return driver.doesGetMatch(dottedName);
    }

    final void deleteDomain() {
        if (asadminHasString(DOMAIN_NAME, "list-domains")) {
            stopDomain();
            copyDomainLog();
            report(asadmin("delete-domain", DOMAIN_NAME), "delete domain");
        }
        else {
            report(true, "no need to delete domain");
        }
    }

    final void verifyDomain(boolean exists) {
        if (exists) {
            report(asadminHasString(DOMAIN_NAME, "list-domains"), DOMAIN_NAME + " exists");
        }
        else {
            report(!asadminHasString(DOMAIN_NAME, "list-domains"), DOMAIN_NAME + " doesn't exist");
        }
    }

    final void verifyDomainIsRunning(boolean wantRunning) {
        boolean isRunning = asadmin("uptime");
        report(wantRunning == isRunning,
                DOMAIN_NAME + (isRunning ? "is " : "isn't ") + "running");
    }

    final void createDomain() {
        verifyDomainIsRunning(false);
        deleteDomain();
        verifyDomain(false);
        report(asadmin("create-domain", DOMAIN_NAME), "created mon-domain");
        verifyDomain(true);
    }

    final void startDomain() {
        verifyDomainIsRunning(false);
        report(asadmin("start-domain", "--debug", DOMAIN_NAME), "started mon-domain in debug mode");
        verifyDomainIsRunning(true);
    }

    final void stopDomain() {
        report(asadmin("stop-domain", DOMAIN_NAME), "stopped mon-domain");
    }

    final void copyDomainLog() {
        InputStream ins = null;
        FileOutputStream outs = null;

        try {
            File inlog = AdminBaseDevTest.getLogFile(installDir, DOMAIN_NAME);
            File outlog = new File(DOMAIN_NAME + "-server.log");

            if (!inlog.exists()) {
                report(false, "Logfile does not exist: " + inlog);
                return;
            }
            ins = new BufferedInputStream(new FileInputStream(inlog));
            outs = new FileOutputStream(outlog);
            ReadableByteChannel inChannel = Channels.newChannel(ins);
            FileChannel outChannel = outs.getChannel();
            outChannel.transferFrom(inChannel, 0, inlog.length());
            report(true, "Copy-" + DOMAIN_NAME + "-log");
        }
        catch (Exception ex) {
            report(false, "Copy-" + DOMAIN_NAME + "-log");
        }
        finally {
            try {
                ins.close();
                outs.close();
            }
            catch (IOException ex) {
                // nothing to do!
            }
        }

    }

    final void createCluster() {
        verifyCluster(false);
        report(asadmin("create-cluster", CLUSTER_NAME), "created cluster");
        verifyCluster(true);
    }

    final void verifyCluster(boolean wantToExist) {
        String list = driver.asadminWithOutput("list-clusters").out;
        boolean doesExist = matchString(list, CLUSTER_NAME);
        report(doesExist == wantToExist, CLUSTER_NAME + (doesExist ? " exists" : " doesn't exist"));
    }

    /*
     * TEMPORARY UNTIL das-branch is merged into trunk. These tests will
     * naturally fail all over the place if you forget to fix them up.
     */
    final void createInstances() {
        verifyInstances(false);

        if (TestEnv.isHadas()) {
            createInstancesHadas();
        }
        else {
            createInstancesTrunk();
        }

        verifyInstances(true);
    }

    final void createInstancesHadas() {
        report(asadmin("create-local-instance", "--cluster", CLUSTER_NAME,
                "--domain", DOMAIN_NAME,
                CLUSTERED_INSTANCE_NAME1),
                "created " + CLUSTERED_INSTANCE_NAME1);
        report(asadmin("create-local-instance", "--cluster", CLUSTER_NAME,
                "--domain", DOMAIN_NAME,
                CLUSTERED_INSTANCE_NAME2),
                "created " + CLUSTERED_INSTANCE_NAME2);
        report(asadmin("create-local-instance",
                "--domain", DOMAIN_NAME,
                STAND_ALONE_INSTANCE_NAME),
                "created " + STAND_ALONE_INSTANCE_NAME);
    }

    final void createInstancesTrunk() {
        report(asadmin("create-local-instance", "--cluster", CLUSTER_NAME,
                CLUSTERED_INSTANCE_NAME1),
                "created " + CLUSTERED_INSTANCE_NAME1);
        report(asadmin("create-local-instance", "--cluster", CLUSTER_NAME,
                CLUSTERED_INSTANCE_NAME2),
                "created " + CLUSTERED_INSTANCE_NAME2);
        report(asadmin("create-local-instance",
                STAND_ALONE_INSTANCE_NAME),
                "created " + STAND_ALONE_INSTANCE_NAME);
    }

    final void verifyInstances(boolean wantToExist) {
        String list = driver.asadminWithOutput("list-instances").out;

        for (String iname : INSTANCES) {
            verifyInstance(iname, list, wantToExist);
        }
    }

    final void verifyInstance(String iname, String list, boolean wantToExist) {
        boolean doesExist = matchString(iname, list);
        report(doesExist == wantToExist, iname + (doesExist ? " exists" : " doesn't exist"));
    }

    final void startInstances() {
        // ugly fast and temporary
        boolean hadas = TestEnv.isHadas();
        verifyInstances(true);

        for (String iname : INSTANCES) {
            if (hadas) {
                report(asadmin("start-local-instance", "--debug",
                        "--domain", DOMAIN_NAME,
                        iname),
                        "start-local-instance --debug" + iname);
            }
            else {
                report(asadmin("start-local-instance", "--debug",
                        iname),
                        "start-local-instance --debug" + iname);
            }
        }
    }

    final void stopInstances() {
        // ugly fast and temporary
        boolean hadas = TestEnv.isHadas();
        verifyInstances(true);

        for (String iname : INSTANCES) {
            if (hadas) {
                report(asadmin("stop-local-instance",
                        "--domain", DOMAIN_NAME,
                        iname),
                        "stop-local-instance " + iname);
            }
            else {
                report(asadmin("stop-local-instance",
                        iname),
                        "stop-local-instance " + iname);
            }
        }
    }

    final void deleteInstances() {
        // ugly fast and temporary
        boolean hadas = TestEnv.isHadas();
        stopInstances();
        // wen 18707 gets fixed this will naturally fail.  Atthat time remove the
        // superfluous --domain arg

        for (String iname : INSTANCES) {
            if (hadas) {
                report(asadmin("delete-local-instance",
                        "--domain", DOMAIN_NAME,
                        iname),
                        "delete-local-instance " + iname);
            }
            else{
                report(asadmin("delete-local-instance",
                        iname),
                        "delete-local-instance " + iname);
            }
        }

    }

    static String createDottedAttribute(String serverOrClusterName, String attribName) {
        return "configs.config."
                + serverOrClusterName
                + "-config.monitoring-service."
                + attribName
                + "-enabled";
    }

    static String createDottedLevel(String serverOrClusterName, String levelName) {
        return "configs.config."
                + serverOrClusterName
                + "-config.monitoring-service."
                + "module-monitoring-levels."
                + levelName;
    }

    static boolean matchString(String a, String b) {
        if (!ok(a) || !ok(b)) {
            return false;
        }
        // in case you forget the correct order of args
        return (b.indexOf(a) >= 0) || (a.indexOf(b) >= 0);
    }

    void setupJvmMemory() {
        report(asadmin("delete-jvm-options", "--target", "server", "\\-Xmx512m"), "remove-Xmx512m-das");
        report(asadmin("delete-jvm-options", "--target", "default-config", "\\-Xmx512m"), "remove-Xmx512m-def-cfg");
        report(asadmin("create-jvm-options", "--target", "server", "\\-Xmx1024m"), "add-Xmx1024m-das");
        report(asadmin("create-jvm-options", "--target", "default-config", "\\-Xmx1024m"), "add-Xmx1024m-def-cfg");
    }

    static boolean ok(String s) {
        return s != null && s.length() > 0;
    }

    final boolean checkForString(AsadminReturn r, String s) {
        return checkForString(r, s, 1);
    }

    final boolean checkForString(AsadminReturn r, String findme, int howMany) {
        if (r.outAndErr == null) {
            return false;
        }

        if (howMany <= 0) {
            report(false, "Bad arg to checkForString");
            return false;
        }

        String output = r.outAndErr;

        if (howMany == 1) {
            return output.indexOf(findme) >= 0;
        }

        final int findmelength = findme.length();

        while (howMany-- > 0) {
            int index = output.indexOf(findme);

            if (index < 0) {
                return false;
            }

            // got them at least the given number
            if (howMany == 0) {
                return true;
            }

            index += findmelength;

            // moved past the end of the string -- not a match
            if (index >= output.length()) {
                return false;
            }

            output = output.substring(index);
        }
        return false;
    }

    final void deploy(File f) {
        deploy("server", f, null);
    }

    final void deploy(String target, File f) {
        deploy(target, f, null);
    }

    final void deploy(String target, File f, String name) {
        String prepend = f.getName() + " "; // if you send in a null pointer -- tough!!
        report(f.isFile() && f.canRead(), prepend + "exists");

        boolean success;

        if (name != null) {
            success = asadmin("deploy", "--target", target, "--name", name, f.getAbsolutePath());
        }
        else {
            success = asadmin("deploy", "--target", target, f.getAbsolutePath());
        }

        report(success, prepend + "deployed OK to " + target);
    }

    /*
     * this implementaion sucks. please improve it!
     */
    static boolean wget(int port, String uri) {
        try {
            URL url = new URL("http://localhost:" + port + "/" + uri);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setReadTimeout(60000);
            conn.getInputStream().close();
            return conn.getResponseCode() == 200;
        }
        catch (Exception ex) {
            return false;
        }
    }

    ///////////////////////////////////////////////////////////////
    ///   private below
    ///////////////////////////////////////////////////////////////
    private String join(String[] names) {
        StringBuilder sb = new StringBuilder(name);

        for (String name : names) {
            sb.append(SEP).append(name);
        }
        return sb.toString();
    }
    private TestDriver driver;
    private String name;
    private static final String SEP = "::";

    private void handleBadTest() {
        // note that we MUST be running with a debug port for this to work!
        // build.xml should have it set...

        if (!WAIT || waitedOnce) {
            return;
        }

        // only do this once!
        waitedOnce = true;

        for (String s : ERROR) {
            System.out.print(s);
        }
        for (int i = 1; i < 600; i++) {
            if (stopWaiting) {
                break;
            }
            try {
                Thread.sleep(1000);

                if (i % 10 == 0) {
                    System.out.println(i);
                }
            }
            catch (InterruptedException ex) {
                // don't care...
            }
        }
        System.out.println("");
    }
    private static final String[] ERROR = new String[]{
        "\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n",
        "***************************************************************\n",
        "***************************************************************\n",
        "***************************************************************\n",
        "*******   TEST ERROR!!   Attach a Debugger NOW at port 9010 \n",
        "*******   To stop the timeout: \n",
        "******* Set the \"stopWaiting\" variable to true in MonTest\n",
        "*******  I'll wait for 60 seconds...    \n",
        "***************************************************************\n",
        "***************************************************************\n",
        "***************************************************************\n\n\n",};
    // you must set this env. variable or sys property to get the JIT debugging to work
    private static final boolean WAIT =
            Boolean.getBoolean("APS_WAIT")
            || Boolean.parseBoolean(System.getenv("APS_WAIT"));
}
