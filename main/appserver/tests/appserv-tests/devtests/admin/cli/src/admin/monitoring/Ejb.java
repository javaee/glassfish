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

import com.sun.appserv.test.BaseDevTest.AsadminReturn;
import java.io.*;
import static admin.monitoring.Constants.*;

/**
 * Tests ejb monitoring.  Note that this requires a running JavaDB database.
 * @author Byron Nevins
 */
public class Ejb extends MonTest {
    @Override
    void runTests(TestDriver driver) {
        setDriver(driver);
        report(true, "Hello from EJB Monitoring Tests!");
        someTests();
        moreTests();
        testMyEjb();
    }

    void someTests() {
        report(!wget(28080, "connapp1webmod1/ConnectorServlet1?sleepTime=25"), "hit conapp1URL");
        report(!wget(28081, "connapp1webmod1/ConnectorServlet1?sleepTime=25"), "hit conapp1URL");
        deploy(CLUSTER_NAME, blackBoxRar);
        createConnectionPool();
        createConnectionResource();
        deploy(CLUSTER_NAME, conApp1);

        report(wget(28080, "connapp1webmod1/ConnectorServlet1?sleepTime=25"), "hit conapp1URL on 28080-");
        report(wget(28081, "connapp1webmod1/ConnectorServlet1?sleepTime=25"), "hit conapp1URL on 28081-");

        verifyList(CLUSTERED_INSTANCE_NAME1 + ".resources.MConnectorPool.numconnused-name", "NumConnUsed");
        verifyList(CLUSTERED_INSTANCE_NAME2 + ".resources.MConnectorPool.numconnused-name", "NumConnUsed");
    }

    void moreTests() {
        final String uri = "ejbsfapp1/SFApp1Servlet1?sleepTime=12&attribute=cachemisses";
        final String getmArg = ".applications.ejbsfapp1.ejbsfapp1ejbmod1\\.jar.SFApp1EJB1.bean-cache.*";
        final String getmKey = ".applications.ejbsfapp1.ejbsfapp1ejbmod1\\.jar.SFApp1EJB1.bean-cache.numpassivations-count";

        report(!wget(28080, uri), "hit ejbsfapp1URL on 28080-");
        report(!wget(28081, uri), "hit ejbsfapp1URL on 28081-");
        deploy(CLUSTER_NAME, ejbsfapp1);

        report(wget(28080, uri), "hit ejbsfapp1URL on 28080-");
        report(wget(28081, uri), "hit ejbsfapp1URL on 28081-");
        report(verifyGetm(CLUSTERED_INSTANCE_NAME1 + getmArg, getmKey), "ejbbeantest-in1");
        report(verifyGetm(CLUSTERED_INSTANCE_NAME2 + getmArg, getmKey), "ejbbeantest-in2");
        //report(verifyGetm(getmKey), "ejbbeantest-getm-ok");
    }

    private void testMyEjb() {
        final String uri = "MyEjb-war/MyEjbServlet";
        final String arg = ".applications.MyEjb.MyEjb-ejb\\.jar.MySessionBean.bean-methods.getMessage.methodstatistic-count";
        final String getmArgInstance1 = CLUSTERED_INSTANCE_NAME1 + arg;
        final String getmArgInstance2 = CLUSTERED_INSTANCE_NAME2 + arg;

        deploy(CLUSTER_NAME, myejbear);

        // We looking for get -m to return something like this:
        // clustered-i1.applications.MyEjb.MyEjb-ejb\.jar.MySessionBean.bean-methods.getMessage.methodstatistic-count = 8
        for (int i = 0; i < 10; i++) {
            verifyGetm(getmArgInstance1, getmArgInstance1 + " = " + i);
            verifyGetm(getmArgInstance2, getmArgInstance2 + " = " + i);
            report(wget(28080, uri), "hit MyEjbServlet on 28080-");
            report(wget(28081, uri), "hit MyEjbServlet on 28081-");
        }
    }

    private boolean verifyGetm(String arg, String key) {
        AsadminReturn ret = asadminWithOutput("get", "-m", arg);
        return matchString(key, ret.outAndErr);
    }

    private void createConnectionPool() {
        report(asadmin("create-connector-connection-pool",
                "--raname", "blackbox-tx",
                "--connectiondefinition", "javax.sql.DataSource",
                "--property", "DatabaseName=sun-appserv-samples:PortNumber=1527:serverName=localhost:connectionAttributes=;create\\=true:password=APP:user=APP",
                "MConnectorPool"),
                "createMConnectorPool");
    }

    private void createConnectionResource() {
        report(asadmin("create-connector-resource", "--poolname", "MConnectorPool", "--target", CLUSTER_NAME, "eis/ConnectorMonitoring"),
                "createConnectorResource");
    }

    private void verifyList(String name, String desiredValue) {
        AsadminReturn ret = asadminWithOutput("list", "-m", name);
        report(matchString(desiredValue, ret.outAndErr), "verify-list");
    }
    private static final File blackBoxRar = new File(RESOURCES_DIR, "blackbox-tx.rar");
    private static final File conApp1 = new File(RESOURCES_DIR, "conapp1.ear");
    private static final File ejbsfapp1 = new File(RESOURCES_DIR, "ejbsfapp1.ear");
    private static final File myejbear = new File(BUILT_RESOURCES_DIR, "MyEjb/dist/MyEjb.ear");
}
/**
 * NOTES
 *
 * asadmin get -m server.applications.MyEjb.MyEjb-ejb\.jar.MySessionBean.bean-methods.getMessage.methodstatistic-count
 */
