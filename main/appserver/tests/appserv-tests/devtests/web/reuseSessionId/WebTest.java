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
 * Unit test for 4817642 ("RN: get two different session objects in
 * consecutive request dispatching runs") and 4876454 ("get two different
 * session objects in consecutive request dispatching runs").
 *
 * Client specifies JSESSIONID in Cookie request header. The JSP being accessed
 * creates a session. Since the web module's sun-web.xml has reuseSessionID set
 * to TRUE, the container is supposed to assign the client-provided
 * JSESSIONID to the newly generated session, and return it in the response.
 */
public class WebTest {

    private static SimpleReporterAdapter stat
        = new SimpleReporterAdapter("appserv-tests");
    private static final String TEST_NAME = "reuse-sessionid";
    private static final String JSESSION_ID = "1234";

    private String host;
    private String port;
    private String contextRoot;

    public WebTest(String[] args) {
        host = args[0];
        port = args[1];
        contextRoot = args[2];
    }
    
    public static void main(String[] args) {
        stat.addDescription("Unit test for Bugtraq 4817642,4876454");
        WebTest webTest = new WebTest(args);
        webTest.doTest();
        stat.printSummary(TEST_NAME);
    }

    public void doTest() {

        try {
            Socket sock = new Socket(host, new Integer(port).intValue());
            OutputStream os = sock.getOutputStream();
            String get = "GET " + contextRoot + "/jsp/test.jsp" + " HTTP/1.0\n";
            System.out.println(get);
            os.write(get.getBytes());
            os.write(("Cookie: JSESSIONID=" + JSESSION_ID + "\n").getBytes());
            os.write("\n".getBytes());
        
            InputStream is = sock.getInputStream();
            BufferedReader bis = new BufferedReader(new InputStreamReader(is));

            String line = null;
            while ((line = bis.readLine()) != null) {
                if (line.startsWith("Set-Cookie:")
                        || line.startsWith("Set-cookie:") ) {
                    break;
                }
            }

            if (line != null) {
                System.out.println(line);
                // Check jsessionid
                String sessionId = getCookieField(line, "JSESSIONID=");
                if (sessionId != null) {
                    if (!sessionId.equals(JSESSION_ID)) {
                        System.err.println("Wrong JSESSIONID: " + sessionId
                                           + ", expected: \"" + JSESSION_ID
                                           + "\"");
                        stat.addStatus(TEST_NAME, stat.FAIL);
                    } else {
                        stat.addStatus(TEST_NAME, stat.PASS);
                    }
	        } else {
                    System.err.println("Missing JSESSIONID");
                    stat.addStatus(TEST_NAME, stat.FAIL);
                }
	    } else {
                System.err.println("Missing Set-Cookie response header");
                stat.addStatus(TEST_NAME, stat.FAIL);
            }

        } catch (Exception ex) {
            stat.addStatus(TEST_NAME, stat.FAIL);
            ex.printStackTrace();
        }
    }

    private String getCookieField(String cookie, String field) {

        String ret = null;

        int index = cookie.indexOf(field);
        if (index != -1) {
            int endIndex = cookie.indexOf(';', index);
            if (endIndex != -1) {
                ret = cookie.substring(index + field.length(), endIndex);
            } else {
                ret = cookie.substring(index + field.length());
            }
            ret = ret.trim();
        }

        return ret;
    }

}
