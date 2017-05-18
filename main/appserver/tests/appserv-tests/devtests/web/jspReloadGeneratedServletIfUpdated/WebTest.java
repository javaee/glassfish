/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 1997-2017 Oracle and/or its affiliates. All rights reserved.
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

import java.io.*;
import java.net.*;
import com.sun.ejte.ccl.reporter.*;

/*
 * Unit test to ensure that servlet that was generated from JSP is reloaded
 * when its class file is updated (without the JSP getting recompiled).
 *
 * This unit test accesses a JSP, which causes the JSP to be compiled to a
 * servlet, and the servlet class to be loaded. The unit test checks the
 * servlet response and makes sure it matches the expected output ("This is
 * my output").
 *
 * The test then updates the servlet class file (without recompiling the
 * JSP!). The updated servlet class file generates a different output ("This
 * is my UPDATED output") than the original class file. The unit test checks
 * that the servlet is reloaded (by comparing its output against the expected
 * modified output) the next time the JSP is accessed.
 */
public class WebTest {

    private static String TEST_NAME;

    private static final String TEST_ROOT_NAME
        = "jsp-reload-generated-servlet-if-updated";

    private static SimpleReporterAdapter stat
        = new SimpleReporterAdapter("appserv-tests");

    private String host;
    private String port;
    private String contextRoot;
    private String run;

    public WebTest(String[] args) {
        host = args[0];
        port = args[1];
        contextRoot = args[2];
        run = args[3];
    }
    
    public static void main(String[] args) {

        stat.addDescription("Unit test to ensure that servlet that "
                            + "was generated from JSP is reloaded when its "
                            + "class file is updated");
        WebTest webTest = new WebTest(args);

        if ("first".equals(webTest.run)) {
            TEST_NAME = TEST_ROOT_NAME + "-first";
        } else {
            TEST_NAME = TEST_ROOT_NAME + "-second";
        }

        try {
            webTest.doTest();
        } catch (Exception ex) {
            ex.printStackTrace();
            stat.addStatus(TEST_NAME, stat.FAIL);
        }

	stat.printSummary();
    }

    public void doTest() throws Exception {

        InputStream is = null;
        BufferedReader input = null;
        try {
            String expectedResponse = null;
            if ("first".equals(run)) {
                expectedResponse = "This is my output";
            } else {
                expectedResponse = "This is my UPDATED output";
            }

            URL url = new URL("http://" + host  + ":" + port
                              + contextRoot + "/jsp/test.jsp");
            System.out.println("Connecting to: " + url.toString());
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.connect();
            int responseCode = conn.getResponseCode();

            if (responseCode != 200) {
                System.err.println("Unexpected return code: " + responseCode);
                stat.addStatus(TEST_NAME, stat.FAIL);
            } else {
                is = conn.getInputStream();
                input = new BufferedReader(new InputStreamReader(is));
                String line = input.readLine();
                if (expectedResponse.equals(line)) {
                    stat.addStatus(TEST_NAME, stat.PASS);
                } else {
                    System.err.println("Wrong response. Expected: " +
                                       expectedResponse + ", received: " + line);
                    stat.addStatus(TEST_NAME, stat.FAIL);
                }
            }
        } finally {
            try {
                if (is != null) is.close();
            } catch (IOException ex) {}
            try {
                if (input != null) input.close();
            } catch (IOException ex) {}
        }
    }
}
