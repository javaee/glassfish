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

import java.io.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.*;
import com.sun.ejte.ccl.reporter.*;

/*
 * Unit test for IT GLASSFISH-16768: CSRF Prevention Filter
 *
 */
public class WebTest {

    private static final String TEST_NAME =
        "csrf-filter";

    private static SimpleReporterAdapter stat =
        new SimpleReporterAdapter("appserv-tests");

    private String host;
    private String port;
    private String contextRoot;
    private String sessionId;
    private String csrfParam = null;

    public WebTest(String[] args) {
        host = args[0];
        port = args[1];
        contextRoot = args[2];
    }
    
    public static void main(String[] args) {

        stat.addDescription("Unit test for IT GLASSFISH-16768");
        final WebTest webTest = new WebTest(args);

        try {
            webTest.doTest("/resource.jsp", null, false, 403);
            webTest.doTest("/index.jsp", null, true, 200);
            webTest.doTest("/resource.jsp", webTest.csrfParam, false, 200);
            webTest.doTest("/resource.jsp", webTest.csrfParam + "__XXX", false, 403);
            webTest.doTest("/resource.jsp", null, false, 403);
            webTest.doTest("/resource.jsp", webTest.csrfParam + "__XXX", false, 403);
            stat.addStatus(TEST_NAME, stat.PASS);
        } catch(Exception ex) {
            ex.printStackTrace();
            stat.addStatus(TEST_NAME, stat.FAIL);
        }

        stat.printSummary();
    }

    public int doTest(String page, String param,
            boolean processSessionCookieHeader,
            int expectedCode) throws Exception {

        StringBuilder sb = new StringBuilder("http://");
        sb.append(host).append(":").append(port).append(contextRoot).append(page);
        if (sessionId != null) {
            sb.append(";jsessionid=").append(sessionId);
        }

        if (param != null) {
            sb.append("?").append(param);
        }
        URL url = new URL(sb.toString());
     
        System.out.println("Connecting to: " + url.toString());

        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.connect();
        int responseCode = conn.getResponseCode();
        if (responseCode != expectedCode) {
            throw new Exception("Unexpected response code: " + responseCode +
                    ", expected: " + expectedCode);
        }

        if (responseCode == 200) {
            if (processSessionCookieHeader) {
                List<String> tempList = conn.getHeaderFields().get("Set-Cookie");
                if (tempList != null && tempList.size() > 0) {
                    String temp = tempList.get(0).split(";")[0];
                    int ind = temp.indexOf("=");
                    if (ind > 0) {
                        sessionId = temp.substring(ind + 1);
                    }
                }
            }

            InputStream is = null;
            BufferedReader bis = null;
            String line = null;
            String sid = null;

            try {
                is = conn.getInputStream();
                bis = new BufferedReader(new InputStreamReader(is));
                while ((line = bis.readLine()) != null) {
                    System.out.println(line);
                    if (line.startsWith("url=")) {
                        csrfParam = line.substring(6); // url=/?
                    } else if (line.startsWith("sid=")) {
                        sid = line.substring(4);
                        if (!sid.equals(sessionId)) {
                            throw new Exception("Session id mismatch. Got: "
                                    + sid + ". Expected: " + sessionId);
                        }
                    }
                }
            } finally {
                try {
                    if (is != null) {
                        is.close();
                    }
                } catch(IOException ioe) {
                    // ignore
                }
                try {
                    if (bis != null) {
                        bis.close();
                    }
                } catch(IOException ioe) {
                    // ignore
                }
            }
        }

        return responseCode;
    }
}
