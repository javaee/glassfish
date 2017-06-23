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
 * Unit test to make sure that AsyncListener#onTimeout is called after the
 * specified async timeout interval.
 */
public class WebTest {

    private static final String TEST_NAME = "servlet-3.0-async-listener-on-timeout";

    private static SimpleReporterAdapter stat =
        new SimpleReporterAdapter("appserv-tests");

    private static final String EXPECTED_RESPONSE = "Hello world";
    private static final String EXPECTED_SB_RESULT = "A";

    private String host;
    private String port;
    private String contextRoot;

    public WebTest(String[] args) {
        host = args[0];
        port = args[1];
        contextRoot = args[2];
    }
    
    public static void main(String[] args) {

        stat.addDescription("Unit test for AsyncListener#onTimeout");
        WebTest webTest = new WebTest(args);

        try {
            webTest.doTest("wrap=true", EXPECTED_RESPONSE);
            webTest.doTest("result=1", EXPECTED_SB_RESULT);
            webTest.doTest("wrap=false", EXPECTED_RESPONSE);
            webTest.doTest("result=1", EXPECTED_SB_RESULT);
            stat.addStatus(TEST_NAME, stat.PASS);
        } catch (Exception ex) {
            ex.printStackTrace();
            stat.addStatus(TEST_NAME, stat.FAIL);
        }

	stat.printSummary();
    }

    public void doTest(String mode, String expectedResponse) throws Exception {

        InputStream is = null;
        BufferedReader input = null;
       
        try {     
            URL url = new URL("http://" + host  + ":" + port +
                contextRoot + "/AsyncListenerTimeoutServlet?" + mode);
            System.out.println("Connecting to: " + url.toString());

            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            // Default async timeout is 30000
            conn.setReadTimeout(40000);
            conn.connect();

            int responseCode = conn.getResponseCode();
            if (responseCode != 200) {
                throw new Exception("Unexpected return code: " + responseCode);
            }

            is = conn.getInputStream();
            input = new BufferedReader(new InputStreamReader(is));
            int count = 0;
            String response = null;
            while ((response = input.readLine()) != null) {
                System.out.println(response);
                if (expectedResponse.equals(response)) {
                    count++;
                }
            }
            if (count != 1) {
                throw new Exception("Missing or wrong response. Expected: " +
                    expectedResponse + ", received: " + response +
                    ". With count = " + count);
            }
        } finally {
            try {
                if (is != null) is.close();
            } catch (IOException ex) {}
            try {
                if (input != null) input.close();
            } catch (IOException ex) {}
        }
    }
}
