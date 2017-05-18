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
import com.sun.ejte.ccl.reporter.*;

/*
 * Test case for 4944160 ("InvokerServlet must be disabled in
 * default-web.xml"). This test case makes sure that Tomcat's InvokerServlet,
 * which is declared in the appserver domain's default-web.xml, has been
 * disabled.
 *
 * This client attempts to connect to this URL:
 * 
 *   http://<host>:<port>/web-invoker-servlet-disabled/servlet/TestServlet
 *
 * which must result in a 404, because the test servlet is mapped to this 
 * url-pattern in web.xml: /TestServlet, instead of /servlet/TestServlet.
 *
 * The client will be able to connect successfully to the above URL only if the
 * InvokerServlet has been enabled.
 */
public class WebTest {

    private static SimpleReporterAdapter stat
        = new SimpleReporterAdapter("appserv-tests");

    private String host;
    private String port;
    private String contextRoot;
    private boolean fail;

    public WebTest(String[] args) {
        host = args[0];
        port = args[1];
        contextRoot = args[2];
    }
    
    public static void main(String[] args) {
        stat.addDescription("Unit test for Bugtraq 4944160");
        WebTest webTest = new WebTest(args);
        webTest.doTest();
        stat.printSummary("invoker-servlet-disabled");
    }

    public void doTest() {
     
        URL url = null;
        HttpURLConnection conn = null;
        int responseCode;
        boolean fail = false;

        try { 
            /*
             * Connect to the wrong mapping.
             *
             * This will work only if the InvokerServlet in default-web.xml
             * has been enabled, and therefore must fail (with a 404 response
             * code) since the InvokerServlet should not have been enabled.
             */ 
            url = new URL("http://" + host  + ":" + port + contextRoot
                    + "/servlet/TestServlet");
            System.out.println("Connecting to: " + url.toString());
            conn = (HttpURLConnection) url.openConnection();
            conn.connect();
            responseCode = conn.getResponseCode();
            System.out.println("Response code: " + responseCode);
            if (responseCode != 404){
                fail = true;
            }

            /*
             * Connect to the correct mapping, as specified in the deployment
             * descriptor. This must work.
             */
            url = new URL("http://" + host  + ":" + port + contextRoot
                    + "/TestServlet");
            System.out.println("Connecting to: " + url.toString());
            conn = (HttpURLConnection) url.openConnection();
            conn.connect();
            responseCode = conn.getResponseCode();
            System.out.println("Response code: " + responseCode);
            if (responseCode == 404){
                fail = true;
            }

            if (fail) {
                stat.addStatus("invoker-servlet-disabled", stat.FAIL);
            } else {
                stat.addStatus("invoker-servlet-disabled", stat.PASS);
            }

        } catch (Exception ex) {
            System.out.println("invoker-servlet-disabled test failed.");
            stat.addStatus("invoker-servlet-disabled", stat.FAIL);
            ex.printStackTrace();
        }
    }
}
