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
 * Unit test for CR 6376017 ("Erroneous values for request.getPathInfo() and
 * request.getPathTranslated()").
 */
public class WebTest {

    private static final String TEST_NAME =
        "servlet-request-getPathInfo-getPathTranslated";

    private static final String EXPECTED_RESPONSE = "/Page1.jsp";

    private static SimpleReporterAdapter stat
        = new SimpleReporterAdapter("appserv-tests");

    private String host;
    private String port;
    private String contextRoot;
    private String appserverHome;
    private String expectedResponse;
    private boolean fail = false;
    private Socket sock = null;

    public WebTest(String[] args) {

        host = args[0];
        port = args[1];
        contextRoot = args[2];
        appserverHome = args[3];

        expectedResponse = appserverHome
                + "/domains/domain1/applications"
                + contextRoot + "-web"
                + EXPECTED_RESPONSE;
    }

    public static void main(String[] args) {
        stat.addDescription("Unit test for CR 6376017");
        WebTest webTest = new WebTest(args);
        webTest.doTest();
        stat.printSummary(TEST_NAME);
    }

    public void doTest() {
     
        try { 
            invoke();
        } catch (Exception ex) {
            ex.printStackTrace();
            fail = true;
        } finally {
            try {
                if (sock != null) {
                    sock.close();
                }
            } catch (IOException ioe) {
                // ignore
            }
        }

        if (fail) {
            stat.addStatus(TEST_NAME, stat.FAIL);
        } else {
            stat.addStatus(TEST_NAME, stat.PASS);
        }
    }

    public void invoke() throws Exception {

        sock = new Socket(host, new Integer(port).intValue());
        OutputStream os = sock.getOutputStream();
        String get = "GET " + contextRoot + " HTTP/1.0\n";
        System.out.println(get);
        os.write(get.getBytes());
        os.write("\n".getBytes());
        
        InputStream is = null;
        BufferedReader bis = null;
        String line = null;
        try {
            is = sock.getInputStream();
            bis = new BufferedReader(new InputStreamReader(is));
            int i = 0;
            while ((line = bis.readLine()) != null) {
                System.out.println(i++ + ": " + line);
                if (line.startsWith("Location:")) {
                    break;
                }
            }
        } finally {
            try {
                if (is != null) {
                    is.close();
                }
            } catch (IOException ioe) {
                // ignore
            }
            try {
                if (bis != null) {
                    bis.close();
                }
            } catch (IOException ioe) {
                // ignore
            }
        }

        if (line == null) {
            System.err.println("Missing Location response header");
            fail = true;
            return;
        }

        int index = line.indexOf("http");
        if (index == -1) {
            System.err.println(
                "Missing http address in Location response header");
            fail = true;
            return;
        }

        String redirectTo = line.substring(index);
        System.out.println("Redirect to: " + redirectTo);
        URL url = new URL(redirectTo);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.connect();
        int responseCode = conn.getResponseCode();
        if (responseCode != 200) { 
            System.err.println("Wrong response code. Expected: 200"
                               + ", received: " + responseCode);
            fail = true;
            return;
        }


        BufferedReader br = null;
        try {
            br = new BufferedReader(new InputStreamReader(
                conn.getInputStream()));
            processResponse(br);
        } finally {
            try {
                if (br != null) {
                    br.close();
                }
            } catch (IOException ioe) {
                // ignore
            }
        }
    }


    private void processResponse(BufferedReader br) throws Exception {

        boolean first = true;
        String line = null;
        int pos = expectedResponse.indexOf("workspace");
        if (pos>=0) {
            expectedResponse = expectedResponse.substring(pos);
        }
        while ((line = br.readLine()) != null) {
            pos = line.indexOf("workspace");
            if (pos>=0) {
                line = line.substring(pos);
            }
            if (first) {
                if (!EXPECTED_RESPONSE.equals(line)) {
                    System.err.println("Wrong response, expected: "
                                       + EXPECTED_RESPONSE
                                       + ". received: " + line);
                    fail = true;
                    return;
                }
                first = false;
            } else if (!expectedResponse.equals(line)) {
                System.err.println("Wrong response, expected: "
                                   + expectedResponse
                                   + ". received: " + line);
                fail = true;
                return;
            }
        }
    }
}
