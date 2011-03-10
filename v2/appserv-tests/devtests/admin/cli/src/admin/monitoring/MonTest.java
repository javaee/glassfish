/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2011 Sun Microsystems, Inc. All rights reserved.
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
package admin.monitoring;

import com.sun.appserv.test.BaseDevTest.AsadminReturn;
import static admin.monitoring.Constants.*;

/**
 * ALL Monitoring DevTests must implement this interface
 * @author Byron Nevins
 */
abstract class MonTest {
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
    
    final void deleteDomain() {
        if (asadminHasString(DOMAIN_NAME, "list-domains")) {
            stopDomain();
            report(asadmin("delete-domain", DOMAIN_NAME), "delete domain");
        }
        else
            report(true, "no need to delete domain");
    }

    final void verifyDomain(boolean exists) {
        if (exists)
            report(asadminHasString(DOMAIN_NAME, "list-domains"), DOMAIN_NAME + " exists");
        else
            report(!asadminHasString(DOMAIN_NAME, "list-domains"), DOMAIN_NAME + " doesn't exist");
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

    final void createInstances() {
        verifyInstances(false);
        report(asadmin("create-local-instance", "--cluster", CLUSTER_NAME, CLUSTERED_INSTANCE_NAME1),
                "created " + CLUSTERED_INSTANCE_NAME1);
        report(asadmin("create-local-instance", "--cluster", CLUSTER_NAME, CLUSTERED_INSTANCE_NAME2),
                "created " + CLUSTERED_INSTANCE_NAME2);
        report(asadmin("create-local-instance", STAND_ALONE_INSTANCE_NAME),
                "created " + STAND_ALONE_INSTANCE_NAME);
        verifyInstances(true);
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
        verifyInstances(true);

        for (String iname : INSTANCES) {
            report(asadmin("start-local-instance", iname),
                    "start-instance " + iname);
        }
    }

    final void stopInstances() {
        verifyInstances(true);

        for (String iname : INSTANCES) {
            report(asadmin("stop-local-instance", iname),
                    "stop-instance " + iname);
        }
    }

    final void deleteInstances() {
        stopInstances();

        for (String iname : INSTANCES) {
            report(asadmin("delete-local-instance", iname),
                    "delete instance " + iname);
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
        if (!ok(a) || !ok(b))
            return false;
        // in case you forget the correct order of args
        return (b.indexOf(a) >= 0) || (a.indexOf(b) >= 0);
    }

    static boolean ok(String s) {
        return s != null && s.length() > 0;
    }
    ///////////////////////////////////////////////////////////////
    ///   private below
    ///////////////////////////////////////////////////////////////

    private String join(String[] names) {
        StringBuilder sb = new StringBuilder(name);

        for (String name : names)
            sb.append(SEP).append(name);
        return sb.toString();
    }
    private TestDriver driver;
    private String name;
    private static final String SEP = "::";
}
