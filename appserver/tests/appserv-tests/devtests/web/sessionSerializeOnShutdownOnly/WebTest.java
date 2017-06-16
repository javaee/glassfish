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
import java.net.Socket;
import com.sun.appserv.test.util.results.SimpleReporterAdapter;

/*
* Unit test for
*
*  https://glassfish.dev.java.net/issues/show_bug.cgi?id=6447
*  ("Avoid serializing and saving sessions to file during un- or
*  redeployment (unless requested by user)")
*/
public class WebTest {
    private static final String TEST_ROOT_NAME = "session-serialize-on-shutdown-only";
    private static final String EXPECTED_RESPONSE = "Found map";
    private static final String JSESSIONID = "JSESSIONID";
    private static final SimpleReporterAdapter stat = new SimpleReporterAdapter("appserv-tests",
        "session-serialize-on-shutdown-only");
    private String testName;
    private String host;
    private String port;
    private String contextRoot;
    private String run;
    private boolean shouldFindSession;

    public WebTest(String[] args) {
        host = args[0];
        port = args[1];
        contextRoot = args[2];
        run = args[3];
        shouldFindSession = args.length >= 5 && Boolean.parseBoolean(args[4]);
    }

    public static void main(String[] args) {
        new WebTest(args).run();
    }

    private void run() {
        stat.addDescription("Unit test for GlassFish Issue 6447");
        try {
            testName = TEST_ROOT_NAME + "-" + run;
            if ("first".equals(run)) {
                createSession();
            } else {
                checkForSession();
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            stat.addStatus(testName, SimpleReporterAdapter.FAIL);
        }
        stat.printSummary();
    }

    public void createSession() throws Exception {
        Socket sock = new Socket(host, new Integer(port));
        OutputStream os = sock.getOutputStream();
        String get = "GET " + contextRoot + "/CreateSession" + " HTTP/1.0\n";
        System.out.println(get);
        os.write(get.getBytes());
        os.write("\r\n".getBytes());
        saveSessionID(sock);
        stat.addStatus(testName, SimpleReporterAdapter.PASS);
    }

    public void checkForSession() throws Exception {
        // Read the JSESSIONID from the previous run
        String jsessionId = readSessionID();
        Socket sock = new Socket(host, new Integer(port));
        OutputStream os = sock.getOutputStream();
        String get = "GET " + contextRoot + "/ResumeSession" + " HTTP/1.0\n";
        System.out.println(get);
        os.write(get.getBytes());
        String cookie = "Cookie: " + jsessionId + "\n";
        os.write(cookie.getBytes());
        os.write("\r\n".getBytes());
        InputStream is = sock.getInputStream();
        BufferedReader br = new BufferedReader(new InputStreamReader(is));
        String line = null;
        boolean found = false;
        while ((line = br.readLine()) != null) {
            System.out.println(line);
            found |= line.contains(EXPECTED_RESPONSE);
        }
        stat.addStatus(testName, found == shouldFindSession ? SimpleReporterAdapter.PASS : SimpleReporterAdapter.FAIL);
    }

    private String readSessionID() throws IOException {
        FileInputStream fis = new FileInputStream(JSESSIONID);
        return new BufferedReader(new InputStreamReader(fis)).readLine();
    }

    private void saveSessionID(Socket sock) throws Exception {
        InputStream is = sock.getInputStream();
        BufferedReader br = new BufferedReader(new InputStreamReader(is));
        // Get the JSESSIONID from the response
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
        String jsessionId = getSessionIdFromCookie(line, JSESSIONID);
        // Store the JSESSIONID in a file
        FileOutputStream fos = new FileOutputStream(JSESSIONID);
        OutputStreamWriter osw = new OutputStreamWriter(fos);
        osw.write(jsessionId);
        osw.close();
    }

    private String getSessionIdFromCookie(String cookie, String field) {
        String ret = null;
        int index = cookie.indexOf(field);
        if (index != -1) {
            int endIndex = cookie.indexOf(';', index);
            if (endIndex == -1) {
                ret = cookie.substring(index);
            } else {
                ret = cookie.substring(index, endIndex);
            }
            ret = ret.trim();
        }
        return ret;
    }
}
