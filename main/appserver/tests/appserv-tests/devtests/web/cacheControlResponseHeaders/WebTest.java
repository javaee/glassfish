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
 * Unit test for 6175642 ("Cache-control: Feature regression in 8.1 SE/EE")
 * and 4953220 ("S1AS7 Http Listener needs to be compliant with RFC 2616 and
 * allow multiple cache-control directives").
 *
 * This unit test sets a setCacheControl property with multiple values
 * ("must-revalidate,no-store") on the virtual server with id 'server', stops
 * and restarts the instance, and then verifies that the response corresponding
 * to a request to this unit test's web module (deployed on 'server') contains
 * the expected Cache-Control response headers.
 */
public class WebTest {

    private static SimpleReporterAdapter stat
        = new SimpleReporterAdapter("appserv-tests");
    private static final String TEST_NAME = "cache-control-response-headers";

    private String host;
    private String port;
    private String contextRoot;

    public WebTest(String[] args) {
        host = args[0];
        port = args[1];
        contextRoot = args[2];
    }
    
    public static void main(String[] args) {
        stat.addDescription("Unit test for 6175642");
        WebTest webTest = new WebTest(args);
        webTest.doTest();
        stat.printSummary(TEST_NAME);
    }

    public void doTest() {     
        try { 
            invokeJsp();
        } catch (Exception ex) {
            System.out.println(TEST_NAME + " test failed.");
            stat.addStatus(TEST_NAME, stat.FAIL);
            ex.printStackTrace();
        }
    }

    private void invokeJsp() throws Exception {
         
        Socket sock = new Socket(host, new Integer(port).intValue());
        OutputStream os = sock.getOutputStream();
        String get = "GET " + contextRoot + "/jsp/test.jsp" + " HTTP/1.0\n";
        System.out.println(get);
        os.write(get.getBytes());
        os.write("\n".getBytes());
        
        InputStream is = sock.getInputStream();
        BufferedReader bis = new BufferedReader(new InputStreamReader(is));

        boolean found1 = false;
        boolean found2 = false;
        boolean found12 = false;
        String line = null;
        while ((line = bis.readLine()) != null) {
            System.out.println(line);
            // Take into account the different case in the response header
            // name: Cache-Control vs Cache-control, between PE and SE/EE
            if ("Cache-Control: must-revalidate".equals(line)
                    || "Cache-control: must-revalidate".equals(line)) {
                found1 = true;
            } else if ("Cache-Control: no-store".equals(line)
                    || "Cache-control: no-store".equals(line)) {
                found2 = true;
            } else if ("Cache-Control: must-revalidate,no-store".equals(line) ||
                       "Cache-control: must-revalidate,no-store".equals(line)) {
                found12 = true;
            }
            if ((found1 && found2) || found12) {
                break;
            }
        }

        if ((found1 && found2) || found12) {
            stat.addStatus(TEST_NAME, stat.PASS);
        } else {
            stat.addStatus("Missing Cache-Control response header(s)",
                           stat.FAIL);
        }
    }
}
