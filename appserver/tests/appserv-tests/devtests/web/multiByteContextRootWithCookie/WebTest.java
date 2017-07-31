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
import java.util.List;
import java.util.Map;
import com.sun.ejte.ccl.reporter.*;

/** 
 * Unit test for:
 *
 *  Multi-byte context root with session cookie
 *
 * Multi-byte context root is specified in sun-web.xml.
 */
public class WebTest {

    private static final String TEST_NAME = "multi-byte-context-root-with-cookie";
    private static final String JSESSIONID = "JSESSIONID";

    private static final String EXPECTED_RESPONSE = "abc=def; myid=123@456";

    private static SimpleReporterAdapter stat
        = new SimpleReporterAdapter("appserv-tests");

    private String host;
    private String port;

    public WebTest(String[] args) {
        host = args[0];
        port = args[1];
    }
    
    public static void main(String[] args) {

        stat.addDescription("Unit test for Multi-Byte Context Root with Session Cookie");
        WebTest webTest = new WebTest(args);

        try {
            String sessionId = webTest.doSetJsp();
            boolean expected = false;

            if (sessionId != null) {
                expected = webTest.doGetJsp(sessionId);
            }
            if (expected) {
                stat.addStatus(TEST_NAME, stat.PASS);
            } else {
                stat.addStatus(TEST_NAME, stat.FAIL);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            stat.addStatus(TEST_NAME, stat.FAIL);
        }

	    stat.printSummary();
    }

    public String doSetJsp() throws Exception {
     
        String sessionId = null;

        URL url = new URL("http://" + host  + ":" + port + "/"
            + "good-%E5%A5%BD-good/set.jsp");
        System.out.println("Connecting to: " + url.toString());

        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.connect();
        int responseCode = conn.getResponseCode();

        if (responseCode == 200) {
            Map<String, List<String>> headers = conn.getHeaderFields();
            List<String> cookies = headers.get("Set-Cookie");
            if (cookies == null) {
                cookies = headers.get("Set-cookie");
            }
            System.out.println("Cookies = " + cookies);

            sessionId = getCookieField(cookies, JSESSIONID);
            System.out.println("sessionId = " + sessionId);
        } else {   
            System.err.println("Unexpected return code: " + responseCode);
        }

        return sessionId;
    }

    private boolean doGetJsp(String sessionId) throws Exception {
        boolean expected = false;

        URL url = new URL("http://" + host  + ":" + port + "/"
            + "good-%E5%A5%BD-good/get.jsp");
        System.out.println("Connecting to: " + url.toString());

        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        //String cookie = sessionId + "; myid=123@456";
        String cookie = "$Version=1; " + sessionId + "; myid=\"123@456\"";
        conn.setRequestProperty("Cookie", cookie); 
        conn.connect();
        int responseCode = conn.getResponseCode();

        if (responseCode == 200) {
            InputStream is = conn.getInputStream();
            BufferedReader br = new BufferedReader(new InputStreamReader(is));
            String line = br.readLine();
            System.out.println("Response=" + line);
            if (EXPECTED_RESPONSE.equals(line)) {
                expected = true;
            } else {
                System.err.println("Wrong response. Expected: " + 
                                   EXPECTED_RESPONSE + ", received: " + line);
            }
        } else {   
            System.err.println("Unexpected return code: " + responseCode);
        }

        return expected;
    }

    private String getCookieField(List<String> cookies, String field) {
        String ret = null;

        if (cookies != null) {
            for (String cookie : cookies) {
                if (cookie.startsWith(field)) {
                    int index = 0;
                    int endIndex = cookie.indexOf(';', index);
                    if (endIndex != -1) {
                        ret = cookie.substring(index, endIndex);
                    } else {
                        ret = cookie.substring(index);
                    }
                    ret = ret.trim();
                    break;
                }
            }
        }

        return ret;
    }
}
