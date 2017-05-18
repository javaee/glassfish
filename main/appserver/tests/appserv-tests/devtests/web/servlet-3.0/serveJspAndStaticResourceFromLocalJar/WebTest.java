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
 * Unit test for serving JSP and static resources from
 * WEB-INF/lib/[*.jar]/META-INF/resources
 *
 * In this unit test, the client makes a request for
 *   http://localhost:8080/abc.jsp
 * and
 *   http://localhost:8080/abc.txt
 * and the requested resource is supposed to be served from
 *   WEB-INF/lib/nested.jar!META-INF/resources/abc.jsp
 * (by the JspServlet) and
 *   WEB-INF/lib/nested.jar!META-INF/resources/abc.txt
 * (by the DefaultServlet), respectively.
 *
 * Add unit test for IT 11835.
 */
public class WebTest {

    private static SimpleReporterAdapter stat =
        new SimpleReporterAdapter("appserv-tests");
    private static final String TEST_NAME =
        "serve-jsp-and-static-resource-from-local-jar";
    private static final String EXPECTED_RESPONSE = "Hello World!";
    private static final String EXPECTED_RESPONSE_2 = "Hello World folder!";
    private static final String EXPECTED_RESPONSE_3 = "2: /folder/def.txt, /folder/ghi.txt, ";

    private String host;
    private String port;
    private String contextRoot;

    public WebTest(String[] args) {
        host = args[0];
        port = args[1];
        contextRoot = args[2];
    }
    
    public static void main(String[] args) {
        stat.addDescription("Unit test for serving JSP and static " +
                            "resources from JAR inside WEB-INF/lib");
        new WebTest(args).doTest();
        stat.printSummary(TEST_NAME);
    }

    public void doTest() {
        try { 
            invokeJspServlet();
            invokeDefaultServlet("/abc.txt", 200, EXPECTED_RESPONSE);
            invokeDefaultServlet("/folder", new int[] { 301, 302 }, contextRoot + "/folder/");
            invokeDefaultServlet("/folder/", 404, null);
            invokeDefaultServlet("/folder/def.txt", 200, EXPECTED_RESPONSE_2);
            invokeTestServlet();
            stat.addStatus(TEST_NAME, stat.PASS);
        } catch (Exception ex) {
            stat.addStatus(TEST_NAME, stat.FAIL);
            ex.printStackTrace();
        }
    }

    private void invokeJspServlet() throws Exception {
        URL url = new URL("http://" + host  + ":" +
                          port + contextRoot + "/abc.jsp");
        System.out.println("Invoking URL: " + url.toString());
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.connect();
        int responseCode = conn.getResponseCode();
        if (responseCode != HttpURLConnection.HTTP_OK) {
            throw new Exception("Unexpected response code: " + responseCode);
        }
        InputStream is = conn.getInputStream();
        BufferedReader input = new BufferedReader(new InputStreamReader(is));
        String response = input.readLine();
        if (!EXPECTED_RESPONSE.equals(response)) {
            throw new Exception("Wrong response, expected: " +
                EXPECTED_RESPONSE + ", received: " + response);
        }
    }

    private void invokeDefaultServlet(String path, int expectedStatus,
            String expectedResponse) throws Exception {
        invokeDefaultServlet(path, new int[] { expectedStatus }, expectedResponse);
    }

    private void invokeDefaultServlet(String path, int[] expectedStatuses,
            String expectedResponse) throws Exception {
        URL url = new URL("http://" + host  + ":" +
                          port + contextRoot + path);
        System.out.println("Invoking URL: " + url.toString());
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setInstanceFollowRedirects(false);
        conn.connect();
        int responseCode = conn.getResponseCode();
        boolean validStatus = false;
        for (int status : expectedStatuses) {
            if (status == responseCode) {
                validStatus = true;
            }
        }
        if (!validStatus) {
            throw new Exception("Unexpected response code: " + responseCode);
        }

        if (responseCode == HttpURLConnection.HTTP_OK && expectedResponse != null) {
            InputStream is = conn.getInputStream();
            BufferedReader input = new BufferedReader(new InputStreamReader(is));
            String response = input.readLine();
            if (!expectedResponse.equals(response)) {
                throw new Exception("Wrong response, expected: " +
                    expectedResponse + ", received: " + response);
            }
        }

        if (responseCode == HttpURLConnection.HTTP_MOVED_TEMP && expectedResponse != null) {
            String location = conn.getHeaderField("Location");
            System.out.println("Location: " + location);
            if (location == null || !location.endsWith(expectedResponse)) {
                throw new Exception("Wrong location: " + location +
                    " does not end with " + expectedResponse);
            }
        }
    }

    private void invokeTestServlet() throws Exception {
        URL url = new URL("http://" + host  + ":" +
                          port + contextRoot + "/test");
        System.out.println("Invoking URL: " + url.toString());
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.connect();
        int responseCode = conn.getResponseCode();
        if (responseCode != HttpURLConnection.HTTP_OK) {
            throw new Exception("Unexpected response code: " + responseCode);
        }
        InputStream is = conn.getInputStream();
        BufferedReader input = new BufferedReader(new InputStreamReader(is));
        String response = input.readLine();
        if (!EXPECTED_RESPONSE_3.equals(response)) {
            throw new Exception("Wrong response, expected: " +
                EXPECTED_RESPONSE_3 + ", received: " + response);
        }
    }
}
