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

import java.io.*;
import java.net.*;
import com.sun.ejte.ccl.reporter.*;

/*
 * Unit test for BOM in JSP classic (standard) syntax.
 *
 * Each of the JSP pages accessed by this test is preceded by a BOM from
 * which the JSP container derives the page encoding:
 *
 *  Page           Page Encoding          Bytes         
 *  UTF-16BE.jsp   UTF-16, big-endian     FE FF         
 *  UTF-16LE.jsp   UTF-16, little-endian  FF FE
 *  UTF-8.jsp      UTF-8                  EF BB BF
 *
 * This test enforces that the BOM does not appear in the generated page
 * output, and that the charset component of the Content-Type response header
 * matches the charset identified by the BOM.
 */
public class WebTest {

    private static final String TEST_NAME = "jsp-bom-in-classic-syntax";

    private static final String TEXT_HTML_UTF_16_BE
        = "text/html;charset=UTF-16BE";
    private static final String TEXT_HTML_UTF_16_LE
        = "text/html;charset=UTF-16LE";
    private static final String TEXT_HTML_UTF_8
        = "text/html;charset=UTF-8";

    private static final String EXPECTED_RESPONSE = "this is a test";

    private static SimpleReporterAdapter stat
        = new SimpleReporterAdapter("appserv-tests");

    private String host;
    private String port;
    private String contextRoot;

    public WebTest(String[] args) {
        host = args[0];
        port = args[1];
        contextRoot = args[2];
    }
    
    public static void main(String[] args) {

        stat.addDescription("Unit test for BOM in JSP classic syntax");
        WebTest webTest = new WebTest(args);

        try {
            boolean pass = webTest.doTest("UTF-16BE.jsp");
            if (!pass) {
                stat.addStatus(TEST_NAME, stat.FAIL);
            } else {
                pass = webTest.doTest("UTF-16LE.jsp");
                if (!pass) {
                    stat.addStatus(TEST_NAME, stat.FAIL);
                } else {
                    pass = webTest.doTest("UTF-8.jsp");
                    if (!pass) {
                        stat.addStatus(TEST_NAME, stat.FAIL);
                    } else {
                        stat.addStatus(TEST_NAME, stat.PASS);
                    }
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            stat.addStatus(TEST_NAME, stat.FAIL);
        }

	stat.printSummary();
    }

    /*
     * @return true if passed, false if failed
     */
    public boolean doTest(String jspPage) throws Exception {
     
        InputStream is = null;
        BufferedReader input = null;
        try {
            URL url = new URL("http://" + host  + ":" + port
                              + contextRoot + "/" + jspPage);
            System.out.println("Connecting to: " + url.toString());
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.connect();

            int responseCode = conn.getResponseCode();
            if (responseCode != 200) {
                System.err.println("Wrong response code. Expected: 200"
                                   + ", received: " + responseCode);
                return false;
            }

            is = conn.getInputStream();

            String contentType = conn.getHeaderField("Content-Type");
            if ("UTF-16BE.jsp".equals(jspPage)) {
                if (!TEXT_HTML_UTF_16_BE.equals(contentType)) {
                    System.err.println("Wrong response content-type. "
                                       + "Expected: " + TEXT_HTML_UTF_16_BE
                                       + ", received: " + contentType);
                    return false;
                }
            } else if ("UTF-16LE.jsp".equals(jspPage)) {
                if (!TEXT_HTML_UTF_16_LE.equals(contentType)) {
                    System.err.println("Wrong response content-type. "
                                       + "Expected: " + TEXT_HTML_UTF_16_LE
                                       + ", received: " + contentType);
                    return false;
                }
            } else if ("UTF-8.jsp".equals(jspPage)) {
                if (!TEXT_HTML_UTF_8.equals(contentType)) {
                    System.err.println("Wrong response content-type. "
                                       + "Expected: " + TEXT_HTML_UTF_8
                                       + ", received: " + contentType);
                    return false;
                }
            } else {
                return false;
            }

            String charSet = getCharSet(contentType);
            if (charSet == null) {
                return false;
            }

            input = new BufferedReader(
                    new InputStreamReader(is, charSet));
            String line = input.readLine();
            if (!EXPECTED_RESPONSE.equals(line)) {
                System.err.println("Wrong response. "
                                   + "Expected: " + EXPECTED_RESPONSE
                                   + ", received: " + line);
                return false;
            }

            // Success
            return true;
        } finally {
            try {
                if (is != null) is.close();
            } catch (IOException ex) {}
            try {
                if (input != null) input.close();
            } catch (IOException ex) {}
        }
    }


    private String getCharSet(String contentType) {

        int index = contentType.indexOf('=');
        if (index < 0) {
            System.err.println("Unable to get charset from content-type");
            return null;
        }

        return contentType.substring(index+1);
    }
}
