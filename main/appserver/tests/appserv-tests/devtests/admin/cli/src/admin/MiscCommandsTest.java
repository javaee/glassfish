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

/**
 *
 * This will test miscellaneous commands that don't have a dedicated test file
 * @author Tom Mueller
 */
public class MiscCommandsTest extends AdminBaseDevTest {

    @Override
    protected String getTestDescription() {
        return "Miscellaneous Commands Test";
    }

    public static void main(String[] args) {
        new MiscCommandsTest().runTests();    }

    private void runTests() {
        testVersion();
        testMulticastValidator();
        stat.printSummary();
    }

    private void testVersion() {

        final String tn = "version-";
        report(tn + "dasdown-norm", asadmin("version"));
        report(tn + "dasdown-local", asadmin("version", "--local"));
        report(tn + "JIRA15552-das-stopped", asadmin("version", "--local", "--terse", "--verbose"));
        startDomain();
        report(tn + "dasup-norm", asadmin("version"));
        report(tn + "dasup-local", asadmin("version", "--local"));
        report(tn + "JIRA15552-das-running", asadmin("version", "--local", "--terse", "--verbose"));
        stopDomain();
    }

    private void testMulticastValidator() {
        final int defaultSeconds = 20;

        long time0 = System.currentTimeMillis();

        // should not fail if multicast is not available
        asadmin("validate-multicast");
        long time1 = System.currentTimeMillis();

        // should take at least 20 seconds
        boolean success = (time1-time0) > (1000 * defaultSeconds);
        report("validate-multicast-timing", success);

        // now with params
        final String port = "2049";
        final String address = "228.9.3.3";
        final String period = "900";
        final int seconds = 5;
        time0 = System.currentTimeMillis();
        AsadminReturn ret = asadminWithOutput("validate-multicast",
            "--multicastport", port,
            "--multicastaddress", address,
            "--sendperiod", period,
            "--timeout", String.valueOf(seconds));
        time1 = System.currentTimeMillis();
        String out = ret.outAndErr;
        report("validate-multicast-param-port",
            out.contains(port));
        report("validate-multicast-param-address",
            out.contains(address));
        report("validate-multicast-param-period",
            out.contains(period));
        report("validate-multicast-param-seconds",
            out.contains(String.valueOf(seconds)));

        // should only take a little over 5 seconds
        int atLeast = seconds - 1;
        int notThisLong = seconds + 8; // wide berth here
        report("validate-multicast-param-timing-under",
            (time1-time0) > 1000*atLeast);
        report("validate-multicast-param-timing-over",
            (time1-time0) < 1000*notThisLong);
    }
}
