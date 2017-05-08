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

import com.sun.appserv.test.BaseDevTest.AsadminReturn;
import java.io.*;
import static admin.monitoring.Constants.*;

/**
 * Test fixed issues from JIRA
 * These tests depend on monitoring being disabled
 * @author Byron Nevins
 */
public class EarlyJira extends MonTest {
    @Override
    void runTests(TestDriver driver) {
        setDriver(driver);
        report(true, "Hello from Early JIRA Tests!");
        verifyMonOff();
        test15203();
    }

    /*
     * @author Byron Nevins
     * Sanity test -- are the levles all OFF ??
     */
    private void verifyMonOff() {
        String prepend = "verifyMonOff::";
        final String value = OFF;

        for(String cat : MON_CATEGORIES) {
            report(doesGetMatch(das + cat, value), prepend + "das::" + cat);
            report(doesGetMatch(cluster + cat, value), prepend + "mon-cluster::" + cat);
            report(doesGetMatch(standalone + cat, value), prepend + "standalone::" + cat);
        }
    }

    /*
     * @author Byron Nevins
     */
    private void test15203() {
        String prepend = "15203::";

        AsadminReturn aar = asadminWithOutput("get", "-m", STAR);
        report(checkForString(aar, "No monitoring data to report.", 3), prepend + "verify special message all 3 --");
        report(!checkForString(aar, "No monitoring data to report.", 4), prepend + "verify 3 only");

        for (String iname : INSTANCES) {
            aar = asadminWithOutput("get", "-m", makestar(iname));
            report(checkForString(aar, "No monitoring data to report."), prepend + "verify special message " + iname);
        }
    }

    // what a pain!
    private String makestar(String iname) {
        if (isWindows)
            return "\"" + iname + ".*\"";
        else
            return iname + ".*";
    }
    String das = "configs.config.server-config.monitoring-service.module-monitoring-levels.";
    String cluster = "configs.config.mon-cluster-config.monitoring-service.module-monitoring-levels.";
    String standalone = "configs.config.standalone-i3-config.monitoring-service.module-monitoring-levels.";
}
