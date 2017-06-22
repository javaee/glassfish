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

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.Socket;
import java.net.URL;

import com.sun.appserv.test.util.results.SimpleReporterAdapter;

/*
 * Unit test for 6346226 ("SessionLockingStandardPipeline.hasFailoverOccurred
 * only supports jroute-id from cookie, not URL").
 *
 * This test requires that security manager be disabled (see build.xml),
 * because the target servlet performs a security-checked operation.
 */
public class WebTest {
    private static final String TEST_NAME = "jroute-id-in-cookie-or-url";
    private static final String EXPECTED_RESPONSE = "jrouteId=1234";
    private static final SimpleReporterAdapter stat = new SimpleReporterAdapter("appserv-tests", TEST_NAME);
    private String host;
    private String port;
    private String contextRoot;

    public WebTest(String[] args) {
        host = args[0];
        port = args[1];
        contextRoot = args[2];
    }

    public static void main(String[] args) {
        stat.addDescription("Unit test for 6346226");
        WebTest webTest = new WebTest(args);
        try {
            boolean success = webTest.doTestURL();
            if (success) {
                webTest.doTestCookie();
            }
        } catch (Exception ex) {
            stat.addStatus("exception found", SimpleReporterAdapter.FAIL);
            ex.printStackTrace();
        }
        stat.printSummary();
    }

    /*
     * @return true on success, false on failure
     */
    public boolean doTestURL() throws Exception {
        URL url = new URL("http://" + host + ":" + port
            + contextRoot + "/TestServlet"
            + ";jsessionid=CFE28BD89B33B59CD7249ACBDA5B479D"
            + ":1234");
        System.out.println("Connecting to: " + url.toString());
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.connect();
        int responseCode = conn.getResponseCode();
        final String testName = "test url";
        if (responseCode != 200) {
            System.err.println("Wrong response code. Expected: 200"
                + ", received: " + responseCode);
            stat.addStatus(testName, SimpleReporterAdapter.FAIL);
            return false;
        } else {
            InputStream is = conn.getInputStream();
            BufferedReader input = new BufferedReader(new InputStreamReader(is));
            String line = input.readLine();
            if (EXPECTED_RESPONSE.equals(line)) {
                stat.addStatus(testName, SimpleReporterAdapter.PASS);
                return true;
            } else {
                System.err.println("Wrong response. Expected: "
                    + EXPECTED_RESPONSE
                    + ", received: " + line);
                stat.addStatus(testName, SimpleReporterAdapter.FAIL);
                return false;
            }
        }
    }

    public void doTestCookie() throws Exception {
        Socket socket = new Socket(host, Integer.parseInt(port));
        OutputStream os = socket.getOutputStream();
        os.write(("GET " + contextRoot + "/TestServlet HTTP/1.0\n").getBytes());
        os.write("Cookie: JROUTE=1234\n".getBytes());
        os.write("\n".getBytes());
        InputStream is = socket.getInputStream();
        BufferedReader bis = new BufferedReader(new InputStreamReader(is));
        String line = null;
        String lastLine = null;
        while ((line = bis.readLine()) != null) {
            lastLine = line;
        }
        final String testName = "test cookie";
        if (EXPECTED_RESPONSE.equals(lastLine)) {
            stat.addStatus(testName, SimpleReporterAdapter.PASS);
        } else {
            System.err.printf("Wrong response. Expected: %s, received: %s\n", EXPECTED_RESPONSE, line);
            stat.addStatus(testName, SimpleReporterAdapter.FAIL);
        }
    }
}
