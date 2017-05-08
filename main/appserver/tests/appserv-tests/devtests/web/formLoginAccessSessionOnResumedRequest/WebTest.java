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
 * Unit test for http://forums.java.net/jive/thread.jspa?messageID=299899
 * 
 * Make sure that session established by FormAuthenticator may be accessed
 * (resumed) by protected resource (in this case, AccessSession servlet) even
 * if the original request that caused a (re)login contains a cookie with an
 * invalid JSESSIONID.
 *
 * This unit test has been reworked in light of the fix for CR 6633257:
 * It no longer expects that the login page be accessed through a redirect,
 * but accepts that it is accessed via a FORWARD dispatch.
 *
 * Also, add test for CR 7014698: Duplicate JSESSIONID cookie in form based login.
 */
public class WebTest {

    private static final String TEST_NAME =
        "form-login-access-session-on-resumed-request";

    private static final String JSESSIONID = "JSESSIONID";

    private static final SimpleReporterAdapter stat =
        new SimpleReporterAdapter("appserv-tests");

    private String host;
    private String port;
    private String contextRoot;
    private String adminUser;
    private String adminPassword;
    private String jsessionId;

    public WebTest(String[] args) {
        host = args[0];
        port = args[1];
        contextRoot = args[2];
        adminUser = args[3];
        adminPassword = args[4];
    }
    
    public static void main(String[] args) {

        stat.addDescription("Unit test that accesses session established by " +
                            "FormAuthenticator");
        WebTest webTest = new WebTest(args);

        try {
            webTest.run();
            stat.addStatus(TEST_NAME, stat.PASS);
        } catch( Exception ex) {
            ex.printStackTrace();
            stat.addStatus(TEST_NAME, stat.FAIL);
        }

	stat.printSummary();
    }

    public void run() throws Exception {

        jsessionId = accessServlet();
        String redirect = accessLoginPage();
        followRedirect(new URL(redirect).getPath());
    }

    /*
     * Attempt to access servlet resource protected by FORM based login.
     */
    private String accessServlet() throws Exception {
        Socket sock = null;
        OutputStream os = null;
        InputStream is = null;
        BufferedReader br = null;
        String location = null;
        String cookie = null;

        try {
            sock = new Socket(host, new Integer(port).intValue());
            os = sock.getOutputStream();
            String get = "GET " + contextRoot + "/AccessSession HTTP/1.0\n";
            System.out.print(get);
            os.write(get.getBytes());
            String sendCookie = "Cookie: JSESSIONID=AABBCCDDEEFFGGHH\n";
            System.out.println(sendCookie);
            os.write(sendCookie.getBytes());
            os.write("\r\n".getBytes());
        
            is = sock.getInputStream();
            br = new BufferedReader(new InputStreamReader(is));

            String line = null;
            while ((line = br.readLine()) != null) {
                System.out.println(line);
                if (line.startsWith("Location:")) {
                    location = line;
                } else if (line.startsWith("Set-Cookie")) {
                    cookie = line;
                }
            }
        } finally {
            close(sock);
            close(os);
            close(is);
            close(br);
        }
 
        if (cookie == null) {
            throw new Exception("Missing Set-Cookie response header");
        }

        return getSessionIdFromCookie(cookie, JSESSIONID);
    }

    /*
     * Access login.jsp.
     */
    private String accessLoginPage() throws Exception {
        Socket sock = null;
        OutputStream os = null;
        InputStream is = null;
        BufferedReader br = null;
        String location = null;

        try {
            sock = new Socket(host, new Integer(port).intValue());
            os = sock.getOutputStream();
            String get = "GET " + contextRoot
                + "/j_security_check?j_username=" + adminUser
                + "&j_password=" + adminPassword
                + " HTTP/1.0\n";
            System.out.println(get);
            os.write(get.getBytes());
            String cookie = "Cookie: " + jsessionId + "\n";
            os.write(cookie.getBytes());
            os.write("\r\n".getBytes());
        
            is = sock.getInputStream();
            br = new BufferedReader(new InputStreamReader(is));

            String line = null;
            while ((line = br.readLine()) != null) {
                System.out.println(line);
                if (line.startsWith("Location:")) {
                    location = line;
                }
            }
        } finally {
            close(sock);
            close(os);
            close(is);
            close(br);
        }

        if (location == null) {
            throw new Exception("Missing Location response header");
        }

        return location.substring("Location:".length()).trim();
    }

    /*
     * Follow redirect to original URL
     */
    private void followRedirect(String path) throws Exception {
        Socket sock = null;
        OutputStream os = null;
        InputStream is = null;
        BufferedReader br = null;
        String response = null;
        String cookie = null;
        int cookieCount = 0;

        try {
            sock = new Socket(host, new Integer(port).intValue());
            os = sock.getOutputStream();
            String get = "GET " + path + " HTTP/1.0\n";
            System.out.print(get);
            os.write(get.getBytes());
            String sendCookie = "Cookie: " + jsessionId + "\n";
            System.out.println(sendCookie);
            os.write(sendCookie.getBytes());
            os.write("\r\n".getBytes());
        
            is = sock.getInputStream();
            br = new BufferedReader(new InputStreamReader(is));

            String line = null;
            while ((line = br.readLine()) != null) {
                System.out.println(line);
                if (line.startsWith("JSESSIONID")) {
                    response = line;
                } else if (line.startsWith("Set-Cookie")) {
                    cookie = line;
                    cookieCount++;
                }
            }
        } finally {
            close(sock);
            close(os);
            close(is);
            close(br);
        }

        //jsessionId is reset in authentication
        //add check for CR 7014698
        if (cookieCount == 1) {
            String newJsessionId = getSessionIdFromCookie(cookie, JSESSIONID);
            if (newJsessionId != null) {
                jsessionId = newJsessionId;
            }
        } else {
            throw new RuntimeException("JSESSIONID cookie count is incorrect: " + cookieCount);
        }

        if (!jsessionId.equals(response)) {
            throw new Exception("Missing response: " + jsessionId);
        }
    }

    private String getSessionIdFromCookie(String cookie, String field) {

        String ret = null;

        int index = cookie.indexOf(field);
        if (index != -1) {
            int endIndex = cookie.indexOf(';', index);
            if (endIndex != -1) {
                ret = cookie.substring(index, endIndex);
            } else {
                ret = cookie.substring(index);
            }
            ret = ret.trim();
        }

        return ret;
    }

    private void close(Socket sock) {
        try {
            if (sock != null) {
                sock.close();
            }
        } catch(IOException ioe) {
            // ignore
        }
    }

    private void close(InputStream is) {
        try {
            if (is != null) {
                is.close();
            }
        } catch(IOException ioe) {
            // ignore
        }
    }

    private void close(OutputStream os) {
        try {
            if (os != null) {
                os.close();
            }
        } catch(IOException ioe) {
            // ignore
        }
    }

    private void close(Reader reader) {
        try {
            if (reader != null) {
                reader.close();
            }
        } catch(IOException ioe) {
            // ignore
        }
    }
}
