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

import java.lang.*;
import java.io.*;
import java.net.*;

import com.sun.ejte.ccl.reporter.*;

public class WebTest
{
    
    private static final String TEST_NAME = "form-based";
    private static final String JSESSIONID = "JSESSIONID";
    private static final String FILTER_REQUEST = "FILTER-REQUEST:";
    private static final String FILTER_FORWARD = "FILTER-FORWARD:";
    private static final String FILTER_INCLUDE = "FILTER-INCLUDE:";
    private static final String PASS = "PASS";

    private static SimpleReporterAdapter stat=
        new SimpleReporterAdapter("appserv-tests");

    private String host;
    private int port;
    private String contextRoot;
    private String userName;
    private String password;
    private String jsessionId;

    public WebTest(String args[]) {
        host = args[0];
        port = Integer.parseInt(args[1]);
        contextRoot = args[2];
        userName = args[3];
        password = args[4];
    }

    public static void main(String args[]) {

        // The stat reporter writes out the test info and results
        // into the top-level quicklook directory during a run.
      
        stat.addDescription("Standalone jsr115  war test");
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
            sock = new Socket(host, port);
            os = sock.getOutputStream();
            String get = "GET " + contextRoot + "/ServletTest HTTP/1.0\n";
            System.out.print(get);
            os.write(get.getBytes());
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
            sock = new Socket(host, port);
            os = sock.getOutputStream();
            String get = "GET " + contextRoot
                + "/j_security_check?j_username=" + userName
                + "&j_password=" + password
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
            sock = new Socket(host, port);
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
            boolean isFilterRequestOK = false;
            boolean isFilterForwardOK = false;
            boolean isFilterIncludeOK = false;
            while ((line = br.readLine()) != null) {
                System.out.println(line);
                if (line.startsWith(FILTER_REQUEST)) {
                    isFilterRequestOK = PASS.equals(
                        line.substring(FILTER_REQUEST.length()));
                } else if (line.startsWith(FILTER_FORWARD)) {
                    isFilterForwardOK = PASS.equals(
                        line.substring(FILTER_FORWARD.length()));
                } else if (line.startsWith(FILTER_INCLUDE)) {
                    isFilterIncludeOK = PASS.equals(
                        line.substring(FILTER_INCLUDE.length()));
                }
            }

            if (!(isFilterRequestOK && isFilterForwardOK && isFilterIncludeOK)) {
                throw new Exception("Incorrect response: " + isFilterRequestOK + ","
                    + isFilterForwardOK + ", " + isFilterIncludeOK);
            }
        } finally {
            close(sock);
            close(os);
            close(is);
            close(br);
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
