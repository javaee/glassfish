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

package admin.monitoring;

import static admin.monitoring.Constants.*;

/**
 * Setup the outside world for Monitoring Dev Tests
 * @author Byron Nevins
 */
public class Setup extends MonTest {
    @Override
    void runTests(TestDriver driver) {
        setDriver(driver);
        report(true, "Setup here!!!");
        createDomain();
        startDomain();
        setupJvmMemory();
        report(asadmin("stop-domain", DOMAIN_NAME), "stop-with-new-jvm-memory");
        startDomain();
        test13723();
        createCluster();
        createInstances();
        startInstances();
    }

    /**
     * The test is here - not in Jira.java because we can NOT have any instances
     * running for this test to work.
     * list -m "*" did not work
     * list -m "server.*" was needed
     */
    private void test13723() {
        final String prepend = "test13723::";

        // before the fix server.jvm would NOT be in there
        report(!checkForString(asadminWithOutput("list", "-m", STAR), "server.jvm"),
                prepend + "just star");
        report(!checkForString(asadminWithOutput("list", "-m", SERVERDOTSTAR), "server.jvm"),
                prepend + "server dot star");

        report(asadmin("enable-monitoring", "--modules", "jvm=HIGH"), prepend + "enable-mon");
        report(checkForString(asadminWithOutput("list", "-m", STAR), "server.jvm"),
                prepend + "just star");
        report(checkForString(asadminWithOutput("list", "-m", SERVERDOTSTAR), "server.jvm"),
                prepend + "server dot star");

        // return to original state!
        report(asadmin("enable-monitoring", "--modules", "jvm=OFF"), prepend + "disable-mon");
        report(!checkForString(asadminWithOutput("list", "-m", STAR), "server.jvm"),
                prepend + "just star");
        report(!checkForString(asadminWithOutput("list", "-m", SERVERDOTSTAR), "server.jvm"),
                prepend + "server dot star");
    }
}
