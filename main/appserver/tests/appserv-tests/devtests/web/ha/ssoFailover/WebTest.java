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
import java.util.HashMap;
import java.util.Map;
import com.sun.ejte.ccl.reporter.*;
import com.sun.appserv.test.BaseDevTest;

/**
 * Unit test for HA SSO Failover.
 *
 */
public class WebTest extends BaseDevTest {

    static class SessionData {
        private String jsessionId;
        private String jsessionIdVersion;
        private String jreplica;
    }

    private static final String TEST_NAME = "ha-sso-failover";
    private static final String JSESSIONID = "JSESSIONID";
    private static final String JSESSIONIDVERSION = "JSESSIONIDVERSION";
    private static final String JREPLICA = "JREPLICA";
    private static final String JSESSIONIDSSO = "JSESSIONIDSSO";
    private static final String JSESSIONIDSSOVERSION = "JSESSIONIDSSOVERSION";

    private static SimpleReporterAdapter stat
        = new SimpleReporterAdapter("appserv-tests");

    private String host;
    private int port1;
    private int port2;
    private String instancename1;
    private String contextRootPrefix;
    private String user;
    private String password;
    private Map<String, SessionData> app2Sd = new HashMap<String, SessionData>();
    private String ssoId;
    private String ssoIdVersion;
    private long ssoIdVersionNumber = -1L;

    public WebTest(String[] args) {
        host = args[0];
        port1 = Integer.parseInt(args[1]);
        port2 = Integer.parseInt(args[2]);
        instancename1 = args[3];
        contextRootPrefix = "/" + args[4];
        user = args[5];
        password = args[6];
    }
    
    public static void main(String[] args) {

        stat.addDescription("Unit test for GlassFish Issue 1933");
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

    public String getTestName() {
        return TEST_NAME;
    }

    public String getTestDescription() {
        return TEST_NAME;
    }

    public void run() throws Exception {
        /*
         * Access login.jsp
         */
        app2Sd.put("A", new SessionData());
        app2Sd.put("B", new SessionData());
        Socket sock = null;
        OutputStream os = null;
        InputStream is = null;
        BufferedReader br = null;
        try {
            sock = new Socket(host, new Integer(port1).intValue());
            os = sock.getOutputStream();
            String postData = "j_username=" + user
                + "&j_password=" + password;
            String post = "POST " + contextRootPrefix + "-a/j_security_check"
                + " HTTP/1.0\n"
                + "Content-Type: application/x-www-form-urlencoded\n"
                + "Content-length: " + postData.length() + "\n\n"
                + postData;
            System.out.println(post);
            os.write(post.getBytes());
            os.flush();

            is = sock.getInputStream();
            br = new BufferedReader(new InputStreamReader(is));

            String line = null;
            String location = null;
            String cookie = null;
            while ((line = br.readLine()) != null) {
                System.out.println(line);
                if (line.startsWith("Location:")) {
                    location = line;
                } else if (line.startsWith("Set-Cookie:")
                        || line.startsWith("Set-cookie:")) {
                    cookie = line;
                    parseCookies(line, app2Sd.get("A"));
                }
            }

            if (ssoIdVersion != null) {
                long ssoVer = Long.valueOf(ssoIdVersion.substring(
                        JSESSIONIDSSOVERSION.length() + 1));
                if (ssoIdVersionNumber == -1 ||
                        ssoIdVersionNumber + 1 == ssoVer) {
                    ssoIdVersionNumber = ssoVer;
                } else {
                    throw new Exception("Version number does not match: " +
                           ssoIdVersionNumber + ", " + ssoVer);
                }
            }

            if (cookie == null) {
                throw new Exception("Missing Set-Cookie response header");
            } else if (location == null) {
                throw new Exception("Missing Location response header");
            }

            String redirect = location.substring("Location:".length()).trim();
            // follow the redirect
            int cA1 = go(port1, new URL(redirect).getPath(), "A");
            int cB1 = go(port1, contextRootPrefix + "-b/index.jsp", "B");
            
            // stop inst1
            asadmin("stop-local-instance", instancename1);

            int cB2 = go(port2, contextRootPrefix + "-b/index.jsp", "B");
            int cA2 = go(port2, contextRootPrefix + "-a/index.jsp", "A");

            if ((cA2 - cA1 != 1) && (cB2 - cB1 != 1)) {
                throw new Exception("count does not match: " + cA1 + ", " + cB1 + ", " + cA2 + ", " + cB2);
            }
        } finally {
            close(sock);
            close(os);
            close(br);
            close(is);
        }
    }

    /*
     * Access http://<host>:<port>/web-ha-sso-failover-<aName> .
     * @return the associated count value
     */
    private int go(int port, String path, String aName)
            throws Exception {

        int count = -1;
        String countPrefix = aName + ":" + user + ":";
        Socket sock = null;
        OutputStream os = null;
        InputStream is = null;
        BufferedReader br = null;
        try {
            sock = new Socket(host, port);
            os = sock.getOutputStream();
            String get = "GET " + path + " HTTP/1.0\n";
            System.out.println(get);
            os.write(get.getBytes());
            StringBuilder sb = new StringBuilder("Cookie: ");
            sb.append(ssoId);
            if (ssoIdVersion != null) {
                sb.append(";" + ssoIdVersion);
            }
            SessionData data = app2Sd.get(aName);
            if (data.jsessionId != null) {
                sb.append(";" + data.jsessionId);
            }
            if (data.jsessionIdVersion != null) {
                sb.append(";" + data.jsessionIdVersion);
            }
            if (data.jreplica != null) {
                sb.append(";" + data.jreplica);
            }
            os.write(sb.toString().getBytes());
            System.out.println(sb);

            os.write("\n\n".getBytes());
        
            is = sock.getInputStream();
            br = new BufferedReader(new InputStreamReader(is));

            String line = null;
            String cookieHeader = null;
            while ((line = br.readLine()) != null) {
                System.out.println(line);
                if (line.startsWith("Set-Cookie:") ||
                        line.startsWith("Set-cookie:")) {
                    parseCookies(line, app2Sd.get(aName));
                }
                int index = line.indexOf(countPrefix);
                if (index >= 0) {
                    count = Integer.parseInt(line.substring(index + countPrefix.length()));
                    break;
                }
            }
        } finally {
            close(sock);
            close(os);
            close(br);
            close(is);
        } 

        if (count == -1) {
            throw new Exception("Failed to access index.jsp");
        }

        System.out.println("Count: " + countPrefix + count);

        return count;
    }

    private void parseCookies(String cookie, SessionData data) {
        String value = getSessionIdFromCookie(cookie, JSESSIONID);
        if (value != null) {
            data.jsessionId = value;
        }
        value = getSessionIdFromCookie(cookie, JSESSIONIDVERSION);
        if (value != null) {
            data.jsessionIdVersion = value;
        }
        value = getSessionIdFromCookie(cookie, JREPLICA);
        if (value != null) {
            data.jreplica = value;
        }
        value = getSessionIdFromCookie(cookie, JSESSIONIDSSO);
        if (value != null) {
            ssoId = value;
        }
        value = getSessionIdFromCookie(cookie, JSESSIONIDSSOVERSION);
        if (value != null) {
            ssoIdVersion = value;
        }
    }

    private String getSessionIdFromCookie(String cookie, String field) {

        String ret = null;

        int index = cookie.indexOf(field + "=");
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
