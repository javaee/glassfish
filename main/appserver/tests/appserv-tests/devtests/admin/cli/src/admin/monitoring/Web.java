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
 * Tests web monitoring. 
 * @author Carla Mott 
 */
public class Web extends MonTest {
    @Override
    void runTests(TestDriver driver) {
        setDriver(driver);
        report(true, "Hello from Web Monitoring Tests!");
        basicTests();
	jspTests();
        testMyWeb();
    }

    void basicTests() {
        report(!wget(28080, "HelloWeb/"), "hit HelloWebURL on 28080 before deploy");
        report(!wget(28081, "HelloWeb/"), "hit HelloWebURL on 28081 before deploy");
        deploy(CLUSTER_NAME, hellowar);

        //next commands increment the session count
        report(wget(28080, "HelloWeb/"), "hit HelloWebURL on 28080 after deploy");
        report(wget(28081, "HelloWeb/"), "hit HelloWebURL on 28081 after deploy");
        report(verifyGetm(CLUSTERED_INSTANCE_NAME1+".applications.HelloWeb.server.totalservletsloadedcount-count", "4" ), "totalservletsloadedcount test-1");
        report(verifyGetm(CLUSTERED_INSTANCE_NAME2+".applications.HelloWeb.server.totalservletsloadedcount-count", "4" ), "totalservletsloadedcount test-1");

    }

    void jspTests() {
        final String uri = "HelloWeb";
        final String uriRepsonse = "HelloWeb/response.jsp";
        final String getmArg = ".applications.HelloWeb.server.totaljspcount-count";
        final String getmKey = "1";

	report(wget(28080, uri), "jspload on 28080-");  // commands increment the session count
        report(wget(28081, uri), "jspload on 28081-");

        report(verifyGetm(CLUSTERED_INSTANCE_NAME1+getmArg, getmKey), "jsploadtest-1");
        report(verifyGetm(CLUSTERED_INSTANCE_NAME2+getmArg, getmKey), "jsploadtest-2");

        report(wget(28080, uriRepsonse), "jspload on 28080-");  // commands increment the session count
        report(wget(28081, uriRepsonse), "jspload on 28081-");

        report(verifyGetm(CLUSTERED_INSTANCE_NAME1+getmArg, "2"), "jsploadtest-1");
        report(verifyGetm(CLUSTERED_INSTANCE_NAME2+getmArg, "2"), "jsploadtest-2");
    }

    private void testMyWeb() {
        final String uri = "HelloWeb/HelloWorld";
        final String uri2 = "HelloWeb/MyServlet";
	final String sessionCount =".applications.HelloWeb.server.sessionstotal-count"; 
        final String activatedCount =".web.session.activatedsessionstotal-count";


        // Count is 3 by now because of previous tests in this suite
            report(wget(28080, uri), "hit HelloWorld  on 28080-");
            report(wget(28081, uri), "hit HelloWorld on 28081-");
            report(verifyGetm(CLUSTERED_INSTANCE_NAME1+sessionCount, "4" ), "HelloWorld get session count-test-1");
            report(verifyGetm(CLUSTERED_INSTANCE_NAME2+sessionCount, "4" ), "HelloWorld get session count-test-2");


            report(wget(28080, uri), "hit HelloWorld - again");
            report(wget(28081, uri), "hit HelloWorld - again");
            report(verifyGetm(CLUSTERED_INSTANCE_NAME1+sessionCount, "5" ), "second HelloWorld get session count-test-1");
            report(verifyGetm(CLUSTERED_INSTANCE_NAME2+sessionCount, "5" ), "second HelloWorld get session count-test-test-2");

            report(wget(28080, uri2), "hit MyServlet on 28080-");
            report(wget(28081, uri2), "hit MyServlet on 28081-");
            report(verifyGetm(CLUSTERED_INSTANCE_NAME1+sessionCount, "6" ), "MyServlet get session-test-1");
            report(verifyGetm(CLUSTERED_INSTANCE_NAME2+sessionCount, "6" ), "MyServlet get session-test-2");

            report(wget(28080, uri2), "hit MyServlet on 28080- again");
            report(wget(28081, uri2), "hit MyServlet on 28081- again");
            report(verifyGetm(CLUSTERED_INSTANCE_NAME1+sessionCount, "7" ), "second MyServlet get session-test-1");
            report(verifyGetm(CLUSTERED_INSTANCE_NAME2+sessionCount, "7" ), "second MyServlet get session-test-2");
    }

    private boolean verifyGetm(String arg, String key) {
        AsadminReturn ret = asadminWithOutput("get", "-m", arg);
        return matchString(key, ret.outAndErr);
    }

    private static final File hellowar = new File(RESOURCES_DIR, "HelloWeb.war");
}
