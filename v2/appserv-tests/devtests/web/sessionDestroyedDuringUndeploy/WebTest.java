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
 * Unit test for https://glassfish.dev.java.net/issues/show_bug.cgi?id=834
 * ("Sessions not invalidated on Redeploy"):
 *
 * Make sure that if a client authenticates to a webapp using form-based login,
 * it will have to re-login when accessing the same webapp after it has been
 * redeployed.
 */
public class WebTest {

    private static final String TEST_NAME = "session-destroyed-during-undeploy";
    private static final String JSESSIONID = "JSESSIONID";
    private static final String JSESSIONIDSSO = "JSESSIONIDSSO";

    private static final String EXPECTED = "SUCCESS!";

    private static SimpleReporterAdapter stat
        = new SimpleReporterAdapter("appserv-tests");

    private String host;
    private String port;
    private String contextRoot;
    private String adminUser;
    private String adminPassword;
    private String run;

    public WebTest(String[] args) {
        host = args[0];
        port = args[1];
        contextRoot = args[2];
        adminUser = args[3];
        adminPassword = args[4];
        run = args[5];
    }
    
    public static void main(String[] args) {

        stat.addDescription("Unit test for GlassFish Issue 834");
        WebTest webTest = new WebTest(args);

        try {
            if ("firstRun".equals(webTest.run)) {
                webTest.firstRun();
            } else {
                webTest.secondRun();
            }
        } catch( Exception ex) {
            ex.printStackTrace();
            stat.addStatus(TEST_NAME, stat.FAIL);
        }

	stat.printSummary();
    }

    public void firstRun() throws Exception {

        String jsessionId = accessIndexDotJsp();
        String redirect = accessLoginPage(jsessionId);
        String jsessionIdSSO = followRedirect(new URL(redirect).getPath(),
                                              jsessionId);

        // Store the JSESSIONIDSSO in a file
        FileOutputStream fos = null;
        OutputStreamWriter osw = null;
        try {
            fos = new FileOutputStream(JSESSIONIDSSO);
            osw = new OutputStreamWriter(fos);
            osw.write(jsessionIdSSO);
        } finally {
            close(fos);
            close(osw);
        }

        stat.addStatus(TEST_NAME, stat.PASS);
    }

    public void secondRun() throws Exception {
        // Read the JSESSIONIDSSO from the previous run
        FileInputStream fis = null;
        BufferedReader br = null;
        try {
            fis = new FileInputStream(JSESSIONIDSSO);
            br = new BufferedReader(new InputStreamReader(fis));
            accessIndexDotJsp(br.readLine());
            new File(JSESSIONIDSSO).delete();
        } finally {
            close(fis);
            close(br);
        }

        stat.addStatus(TEST_NAME, stat.PASS);
    }

    /*
     * Attempt to access index.jsp resource protected by FORM based login.
     */
    private String accessIndexDotJsp() throws Exception {

        URL url = new URL("http://" + host  + ":" + port + contextRoot
                          + "/index.jsp");
        System.out.println("Connecting to: " + url.toString());
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.connect();

        int responseCode = conn.getResponseCode();
        if (responseCode != HttpURLConnection.HTTP_OK) {
            throw new Exception("Unexpected response code: Got " +
                                responseCode + ", expected: " +
                                HttpURLConnection.HTTP_OK);
        }
                     
        String cookie = conn.getHeaderField("set-cookie");
        if (cookie == null) {
            throw new Exception("Missing Set-Cookie response header");
        }

        return getSessionIdFromCookie(cookie, JSESSIONID);
    }

    /*
     * Access login.jsp.
     */
    private String accessLoginPage(String jsessionId) throws Exception {

        Socket sock = null;
        OutputStream os = null;
        InputStream is = null;
        BufferedReader br = null;
        String line = null;

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

            while ((line = br.readLine()) != null) {
                System.out.println(line);
                if (line.startsWith("Location:")) {
                    break;
                }
            }
        } finally {
            close(sock);
            close(os);
            close(is);
            close(br);
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

        Socket sock = null;
        OutputStream os = null;
        InputStream is = null;
        BufferedReader br = null;
        String cookieHeader = null;
        boolean accessGranted = false;

        try {
            sock = new Socket(host, new Integer(port).intValue());
            os = sock.getOutputStream();
            String get = "GET " + path + " HTTP/1.0\n";
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
                if (line.startsWith("Set-Cookie:")
                        || line.startsWith("Set-cookie:")) {
                    cookieHeader = line;
                } else if (line.contains("SUCCESS!")) {
                    accessGranted = true;
                }
            }
        } finally {
            close(sock);
            close(os);
            close(is);
            close(br);
        }

        if (cookieHeader == null) {
            throw new Exception("Missing Set-Cookie response header");
        }

        if (!accessGranted) {
            throw new Exception("Failed to access index.jsp");
        }

        return getSessionIdFromCookie(cookieHeader, JSESSIONIDSSO);
    }

    /*
     * Attempt to access index.jsp resource protected by FORM based login,
     * supplying JSESSIONIDSSO from previous run.
     */
    private void accessIndexDotJsp(String jsessionIdSSO) throws Exception {
        Socket sock = null;
        OutputStream os = null;
        InputStream is = null;
        BufferedReader br = null;
        boolean loginRequired = false;

        try {
            sock = new Socket(host, new Integer(port).intValue());
            os = sock.getOutputStream();
            String get = "GET " + contextRoot + "/index.jsp" + " HTTP/1.0\n";
            System.out.println(get);
            os.write(get.getBytes());
            String cookie = "Cookie: " + jsessionIdSSO + "\n";
            os.write(cookie.getBytes());
            os.write("\r\n".getBytes());
        
            is = sock.getInputStream();
            br = new BufferedReader(new InputStreamReader(is));

            /*
             * Make sure that a login is required:
             *
             * The response must container either a redirect to the login page,
             * or if the container dispatches the request to the login page
             * (using RD.forward() internally), it must contain the
             * j_security_check action from the login page.
             *
             * See https://glassfish.dev.java.net/issues/show_bug.cgi?id=3374
             * for why a redirect to the login page may occur.
             */
            String line = null;
            String redirectUrl = "Location: http://" + host + ":" + port
                + contextRoot + "/login.jsp"; 
            while ((line = br.readLine()) != null) {
                System.out.println(line);
                if (line.equals(redirectUrl)
                        || line.contains("j_security_check")) {
                    loginRequired = true;
                    break;
                }
            }
        } finally {
            close(sock);
            close(os);
            close(is);
            close(br);
        }

        if (!loginRequired) {
            throw new Exception("No login required");
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

    private void close(Writer writer) {
        try {
            if (writer != null) {
                writer.close();
            }
        } catch(IOException ioe) {
            // ignore
        }
    }
}
