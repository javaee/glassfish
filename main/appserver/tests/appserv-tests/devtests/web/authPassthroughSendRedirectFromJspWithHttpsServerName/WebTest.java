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
 * Unit test for 6269102 ("SSL termination is not working, Appserver replaces
 * the https to http during redirection").
 *
 * This test
 *
 * - sets the authPassthroughEnabled property of http-listener-1 to TRUE [1],
 * - configures a server-name attribute for http-listener-1 with a value of
 *   https://lbhost:8888 [2],
 * - includes a 'Proxy-keysize' header with a value > 0 in the request [3],
 *
 * and expects that the host name and port# of the server-name attribute be
 * reflected in the Location response header, along with an https scheme
 * (due to [1] and [3] above). The https scheme in the server-name attribute
 * is ignored in this case.
 */
public class WebTest {

    private static SimpleReporterAdapter stat
        = new SimpleReporterAdapter("appserv-tests");

    private static final String EXPECTED_LOCATION = 
        "https://lbhost:8888/jsp/redirect/target.jsp";

    private static final String TEST_NAME =
        "auth-passthrough-send-redirect-from-jsp-with-https-server-name";

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
        stat.addDescription("Unit test for 6269102");
        WebTest webTest = new WebTest(args);
        webTest.doTest();
        stat.printSummary(TEST_NAME);
    }

    public void doTest() {
     
        try { 
            testRemoteAddress();
            stat.addStatus(TEST_NAME, stat.PASS);
        } catch (Exception ex) {
            ex.printStackTrace();
            stat.addStatus(TEST_NAME, stat.FAIL);
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

    private void testRemoteAddress() throws Exception {
         
        sock = new Socket(host, new Integer(port).intValue());
        OutputStream os = sock.getOutputStream();
        String get = "GET " + contextRoot + "/jsp/sendRedirect.jsp "
            + "HTTP/1.0\n";
        System.out.println(get);
        os.write(get.getBytes());
        os.write("Proxy-keysize: 512\n".getBytes());
        os.write("Proxy-ip: 123.456.789\n".getBytes());
        os.write("\n".getBytes());
        
        InputStream is = null;
        BufferedReader bis = null;
        String line = null;
        try {
            is = sock.getInputStream();
            bis = new BufferedReader(new InputStreamReader(is));
            while ((line = bis.readLine()) != null) {
                if (line.startsWith("Location:")) {
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

        if (line == null) {
            throw new Exception("Missing Location response header");
        }

        System.out.println("Location header: " + line);
        String location = line.substring("Location:".length()).trim();
        if (!EXPECTED_LOCATION.equals(location)) {
            throw new Exception("Wrong Location response header, expected: "
                                + EXPECTED_LOCATION
                                + ", received: " + location);
        }
    }
}
