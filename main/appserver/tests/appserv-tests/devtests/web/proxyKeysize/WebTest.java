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
 * Unit test for 6170450 ("request.getScheme() does not return the correct
 * protocol when using passthrough plugin").
 *
 * This test includes a 'Proxy-keysize' header in the request and therefore
 * expects the scheme of the URL returned as the value of the 'Location'
 * response header to be HTTPS (as opposed to regular HTTP).
 */
public class WebTest {

    private static SimpleReporterAdapter stat
        = new SimpleReporterAdapter("appserv-tests");
    private static final String TEST_NAME = "web-proxy-keysize";

    private String host;
    private String port;
    private String contextRoot;

    public WebTest(String[] args) {
        host = args[0];
        port = args[1];
        contextRoot = args[2];
    }
    
    public static void main(String[] args) {
        stat.addDescription("Unit test for 6170450");
        WebTest webTest = new WebTest(args);
        webTest.doTest();
        stat.printSummary(TEST_NAME);
    }

    public void doTest() {
     
        try { 
            invokeJsp();
        } catch (Exception ex) {
            stat.addStatus(TEST_NAME, stat.FAIL);
            ex.printStackTrace();
        }
    }

    private void invokeJsp() throws Exception {
         
        Socket sock = null;
        OutputStream os = null;
        InputStream is = null;
        BufferedReader bis = null;
        String line = null;
        try {
            sock = new Socket(host, new Integer(port).intValue());
            os = sock.getOutputStream();
            String get = "GET " + contextRoot + "/jsp/test.jsp" + " HTTP/1.0\n";
            System.out.println(get);
            os.write(get.getBytes());
            os.write("Proxy-keysize: 512\n".getBytes());
            os.write("\n".getBytes());
        
            is = sock.getInputStream();
            bis = new BufferedReader(new InputStreamReader(is));

            while ((line = bis.readLine()) != null) {
                if (line.startsWith("Location:")) {
                    break;
                }
            }
        } finally {
            try {
                if (sock != null) {
                    sock.close();
                }
            } catch(IOException ioe) {
                // ignore
            }
            try {
                if (os != null) {
                    os.close();
                }
            } catch(IOException ioe) {
                // ignore
            }
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

        if (line != null) {
            System.out.println("Location header: " + line);

            String location = line.substring("Location:".length()).trim();
            if (location.startsWith("https")) {
                stat.addStatus(TEST_NAME, stat.PASS);
            } else {
                System.err.println("Wrong schema in Location response header, "
                                   + "expected: https");
                stat.addStatus(TEST_NAME, stat.FAIL);
            }
        } else {
            System.err.println("Missing Location response header");
            stat.addStatus(TEST_NAME, stat.FAIL);
        }
    }
}
