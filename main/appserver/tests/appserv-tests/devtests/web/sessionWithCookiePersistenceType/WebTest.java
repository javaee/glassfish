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
import java.util.*;
import com.sun.ejte.ccl.reporter.*;

/*
 * Unit test for cookie-based session persistence.
 * See https://glassfish.dev.java.net/issues/show_bug.cgi?id=11648 for
 * details.
 *
 * This test deploys an application which has cookie-based session
 * persistence configured in its sun-web.xml descriptor, with a cookie
 * name equal to ABC.
 *
 * This test accesses a resource that creates a session and checks to make
 * sure that the session is persisted in a response cookie with name ABC.
 * The test then submits this cookie (along with the JSESSIONID cookie
 * that was also received as part of the response) and checks to make sure
 * that the session may be resumed.
 */
public class WebTest {

    private static SimpleReporterAdapter stat =
        new SimpleReporterAdapter("appserv-tests");
 
   private static final String TEST_NAME = "session-with-cookie-persistence-type";

    private String host;
    private String port;
    private String contextRoot;
    private Socket sock = null;

    public WebTest(String[] args) {
        host = args[0];
        port = args[1];
        contextRoot = args[2];
    }
    
    public static void main(String[] args) {
        stat.addDescription("Unit test for cookie-based session persistence");
        new WebTest(args).doTest();
        stat.printSummary(TEST_NAME);
    }

    public void doTest() {
        try { 
            invoke();
            stat.addStatus(TEST_NAME, stat.PASS);
        } catch (Exception ex) {
            System.out.println(TEST_NAME + " test failed");
            stat.addStatus(TEST_NAME, stat.FAIL);
            ex.printStackTrace();
        } finally {
            try {
                if (sock != null) {
                    sock.close();
                }
            } catch (IOException ioe) {
                // ignore
            }
        }
    }

    private void invoke() throws Exception {

        /*
         * Create session
         */        
        String url = "http://" + host + ":" + port + contextRoot + "/CreateSession";
        System.out.println("Connecting to: " + url);
        HttpURLConnection conn = (HttpURLConnection)
            (new URL(url)).openConnection();

        int code = conn.getResponseCode();
        if (code != 200) {
            throw new Exception("Unexpected return code: " + code);
        }

        Map <String, List<String>> headers = conn.getHeaderFields();
        List<String> cookieHeaders = headers.get("Set-Cookie");
        if (cookieHeaders.size() != 2) {
            throw new Exception("Wrong number of Set-Cookie response " +
                "headers. Expected: 2, received: " + cookieHeaders.size());
        }
        String jsessionIdCookie = null;
        String persistedSessionCookie = null;
        for (String cookieHeader : cookieHeaders) {
            System.out.println("Response cookie: " + cookieHeader);
            if (cookieHeader.indexOf("JSESSIONID=") != -1) {
                jsessionIdCookie = cookieHeader;
            } else if (cookieHeader.indexOf("ABC=") != -1) {
                persistedSessionCookie = cookieHeader;
            }
        }
        if (jsessionIdCookie == null) {
            throw new Exception("Missing JSESSIONID cookie response header");
        }
        if (persistedSessionCookie == null) {
            throw new Exception("Missing persisted session cookie response header");
        }

        /*
         * Resume session
         */        
        sock = new Socket(host, new Integer(port).intValue());
        OutputStream os = sock.getOutputStream();
        String getRequestLine = "GET " + contextRoot + "/CheckSession" +
            " HTTP/1.0\n";
        System.out.print("\nConnecting to: " + getRequestLine); 
        os.write(getRequestLine.getBytes());
        String cookieHeaderLine = "Cookie: " + jsessionIdCookie + "\n";
        System.out.print(cookieHeaderLine);
        os.write(cookieHeaderLine.getBytes());
        cookieHeaderLine = "Cookie: " + persistedSessionCookie + "\n";
        System.out.print(cookieHeaderLine);
        os.write(cookieHeaderLine.getBytes());
        os.write("\n".getBytes());
        
        InputStream is = null;
        BufferedReader bis = null;
        String line = null;
        boolean okStatus = false;
        try {
            is = sock.getInputStream();
            bis = new BufferedReader(new InputStreamReader(is));
            while ((line = bis.readLine()) != null) {
                System.out.println(line);
                if (line.equals("HTTP/1.1 200 OK") || line.equals("HTTP/1.0 200 OK")) {
                    okStatus = true;
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

        if (!okStatus) {
            throw new Exception("Unable to resume session");
        }
    }
}
