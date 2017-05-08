/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2011-2017 Oracle and/or its affiliates. All rights reserved.
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

import static admin.monitoring.Constants.*;

/**
 * Enable Monitoring 
 * @author Byron Nevins
 */
public class Enabler extends MonTest {
    @Override
    void runTests(TestDriver driver) {
        setDriver(driver);
        report(true, "Enabler says HELLLLLLOOOOO!!!");
        verifyDefaultMainFlags();
        testEnableCommand();
        testSetLevels();
        turnUpMonitoringFullBlast();
    }

    private void turnUpMonitoringFullBlast() {
        for (String config : CONFIG_NAMES) {
            String dot = createDottedAttribute(config, "dtrace");
            report(doesGetMatch(dot, "false"), "-verify-disabled-");
            report(asadmin("enable-monitoring", "--target", config, "--dtrace=true"), "enable-dtrace");
            report(doesGetMatch(dot, "true"), "-verify-enabled-");
            enableMonitoringUsingSet(config, HIGH);
        }
    }

    private void testSetLevels() {
        for (String config : CONFIG_NAMES)
            for (String level : LEVELS)
                enableMonitoringUsingSet(config, level);
    }

    private void verifyDefaultMainFlags() {
        verifyDefaultMainFlags("server");
        verifyDefaultMainFlags(STAND_ALONE_INSTANCE_NAME);
        verifyDefaultMainFlags(CLUSTER_NAME);
    }

    private void verifyDefaultMainFlags(String serverOrClusterName) {
        String reportName = "verify-main-flags " + serverOrClusterName;

        report(doesGetMatch(createDottedAttribute(serverOrClusterName, "dtrace"), "false"),
                reportName + "-dtrace-");
        report(doesGetMatch(createDottedAttribute(serverOrClusterName, "monitoring"), "true"),
                reportName + "-monitoring-");
        report(doesGetMatch(createDottedAttribute(serverOrClusterName, "mbean"), "true"),
                reportName + "-mbean-");
    }

    private void testEnableCommand() {
        testEnableCommand("server");
        testEnableCommand(CLUSTER_NAME);
        testEnableCommand(STAND_ALONE_INSTANCE_NAME);
    }

    private void testEnableCommand(String serverOrClusterName) {
        testDtraceEnableCommand(serverOrClusterName);
        testMbeanEnableCommand(serverOrClusterName);
        testMonitoringEnableCommand(serverOrClusterName);
    }

    private void testDtraceEnableCommand(String serverOrClusterName) {
        // verify off, enable it, verify on, disable, verify off
        String reportName = "dtrace-enable-test ";
        String dot = createDottedAttribute(serverOrClusterName, "dtrace");
        report(doesGetMatch(dot, "false"), reportName + "-verify-disabled-");
        report(asadmin("enable-monitoring", "--target", serverOrClusterName, "--dtrace=true"), reportName + "enable-dtrace");
        report(doesGetMatch(dot, "true"), reportName + "-verify-enabled-");
        report(asadmin("enable-monitoring", "--target", serverOrClusterName, "--dtrace=false"), reportName + "disable-dtrace");
        report(doesGetMatch(dot, "false"), reportName + "-verify-disabled-");
    }

    private void testMbeanEnableCommand(String serverOrClusterName) {
        // verify on, disable it, verify off, enable, verify on
        String reportName = "mbean-enable-test ";
        String dot = createDottedAttribute(serverOrClusterName, "mbean");
        report(doesGetMatch(dot, "true"), reportName + "-verify-enabled-");
        report(asadmin("enable-monitoring", "--target", serverOrClusterName, "--mbean=false"), reportName + "disable-mbean");
        report(doesGetMatch(dot, "false"), reportName + "-verify-disabled-");
        report(asadmin("enable-monitoring", "--target", serverOrClusterName, "--mbean=true"), reportName + "enable-mbean");
        report(doesGetMatch(dot, "true"), reportName + "-verify-enabled-");
    }

    private void testMonitoringEnableCommand(String serverOrClusterName) {
        // verify on, disable it, verify off, enable, verify on
        String reportName = "monitoring-enable-test ";
        String dot = createDottedAttribute(serverOrClusterName, "monitoring");
        report(doesGetMatch(dot, "true"), reportName + "-verify-enabled-");
        report(asadmin("disable-monitoring", "--target", serverOrClusterName), reportName + "disable-all");
        report(doesGetMatch(dot, "false"), reportName + "-verify-disabled-");
        report(asadmin("enable-monitoring", "--target", serverOrClusterName), reportName + "enable-all");
        report(doesGetMatch(dot, "true"), reportName + "-verify-enabled-");
    }

    private void enableMonitoringUsingSet(String serverOrClusterName, String value) {
        final String metName = "enableUsingSet";
        String desiredValue = "=" + value;
        String metFullName = metName + "-set-" + desiredValue + "-";

        for (String monItem : MON_CATEGORIES) {
            String fullitemname = createDottedLevel(serverOrClusterName, monItem);
            String reportname = metFullName + monItem;
            report(asadmin("set", (fullitemname + desiredValue)), reportname);
            report(doesGetMatch(fullitemname, value), reportname + "-verified");
        }
    }
}
