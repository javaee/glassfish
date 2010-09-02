/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 1997-2010 Oracle and/or its affiliates. All rights reserved.
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
 * Unit test for:
 *
 *  https://glassfish.dev.java.net/issues/show_bug.cgi?id=2911
 *  ("sso-enabled and accessLoggingEnabled properties of http-service not
 *   dynamically reconfigurable"):
 *
 * This unit test sets the sso-enabled property of http-service to false and
 * restarts the server. It then removes the sso-enabled property of 
 * virtual server "server", so that the sso-enabled property of http-service
 * (previously set to false) will take effect. It then sets that property to
 * true, and tests whether SSO has been enabled for virtual server "server".
 * If it has, the test succeeds, and otherwise, the test reports failure.
 */
public class WebTest {

    private static final String TEST_NAME = "single-sign-on-http-service-dynamic-reconfig";
    private static final String JSESSIONID = "JSESSIONID";
    private static final String JSESSIONIDSSO = "JSESSIONIDSSO";

    private static SimpleReporterAdapter stat
        = new SimpleReporterAdapter("appserv-tests");

    private String host;
    private String port;
    private String contextRoot;
    private String adminUser;
    private String adminPassword;

    public WebTest(String[] args) {
        host = args[0];
        port = args[1];
        contextRoot = args[2];
        adminUser = args[3];
        adminPassword = args[4];
    }
    
    public static void main(String[] args) {

        stat.addDescription("Unit test for GlassFish Issue 2911");
        WebTest webTest = new WebTest(args);

        try {
            webTest.run();
        } catch( Exception ex) {
            ex.printStackTrace();
            stat.addStatus(TEST_NAME, stat.FAIL);
        }

	stat.printSummary();
    }

    public void run() throws Exception {

        String jsessionId = accessIndexDotJsp();
        String redirect = accessLoginPage(jsessionId);
        String jsessionIdSSO = followRedirect(new URL(redirect).getPath(),
                                              jsessionId);

        if (jsessionIdSSO != null) {
            stat.addStatus(TEST_NAME, stat.PASS);
        } else {
            throw new Exception("Missing JSESSIONIDSSO in response");
        }
    }

    /*
     * Attempt to access index.jsp resource protected by FORM based login.
     */
    private String accessIndexDotJsp() throws Exception {
        Socket sock = new Socket(host, new Integer(port).intValue());
        OutputStream os = sock.getOutputStream();
        String get = "GET " + contextRoot + "/index.jsp" + " HTTP/1.0\n";
        System.out.println(get);
        os.write(get.getBytes());
        os.write("\n".getBytes());
        
        InputStream is = sock.getInputStream();
        BufferedReader br = new BufferedReader(new InputStreamReader(is));

        String line = null;
        while ((line = br.readLine()) != null) {
            System.out.println(line);
            if (line.startsWith("Set-Cookie:")
                    || line.startsWith("Set-cookie:")) {
                break;
            }
        }

        if (line == null) {
            throw new Exception("Missing Set-Cookie response header");
        }

        return getSessionIdFromCookie(line, JSESSIONID);
    }

    /*
     * Access login.jsp.
     */
    private String accessLoginPage(String jsessionId) throws Exception {

        Socket sock = new Socket(host, new Integer(port).intValue());
        OutputStream os = sock.getOutputStream();
        String get = "GET " + contextRoot
            + "/j_security_check?j_username=" + adminUser
            + "&j_password=" + adminPassword
            + " HTTP/1.0\n";
        System.out.println(get);
        os.write(get.getBytes());
        String cookie = "Cookie: " + jsessionId + "\n";
        os.write(cookie.getBytes());
        os.write("\n".getBytes());
        
        InputStream is = sock.getInputStream();
        BufferedReader br = new BufferedReader(new InputStreamReader(is));

        String line = null;
        while ((line = br.readLine()) != null) {
            System.out.println(line);
            if (line.startsWith("Location:")) {
                break;
            }
        }

        if (line == null) {
            throw new Exception("Missing Location response header");
        }

        return line.substring("Location:".length()).trim();
    }

    /*
     * Follow redirect to
     * http://<host>:<port>/web-session-destroyed-during-undeploy/index.jsp
     * and access this resource.
     */
    private String followRedirect(String path, String jsessionId)
            throws Exception {

        Socket sock = new Socket(host, new Integer(port).intValue());
        OutputStream os = sock.getOutputStream();
        String get = "GET " + path + " HTTP/1.0\n";
        System.out.println(get);
        os.write(get.getBytes());
        String cookie = "Cookie: " + jsessionId + "\n";
        os.write(cookie.getBytes());
        os.write("\n".getBytes());
        
        InputStream is = sock.getInputStream();
        BufferedReader br = new BufferedReader(new InputStreamReader(is));

        String line = null;
        String cookieHeader = null;
        boolean accessGranted = false;
        while ((line = br.readLine()) != null) {
            System.out.println(line);
            if (line.startsWith("Set-Cookie:")
                    || line.startsWith("Set-cookie:")) {
                cookieHeader = line;
            } else if (line.contains("SUCCESS!")) {
                accessGranted = true;
            }
        }

        if (cookieHeader == null) {
            throw new Exception("Missing Set-Cookie response header");
        }

        if (!accessGranted) {
            throw new Exception("Failed to access index.jsp");
        }

        return getSessionIdFromCookie(cookieHeader, JSESSIONIDSSO);
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
}
