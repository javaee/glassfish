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
import java.net.HttpURLConnection;
import com.sun.ejte.ccl.reporter.*;

/*
 * Unit test for Issue 8535: welcome-file ignores servlet mapping
 *
 */
public class WebTest {

    private static final String TEST_NAME = "welcome-page-extenson-match";
    private static final String EXPECTED = "Hello";

    private static SimpleReporterAdapter stat
        = new SimpleReporterAdapter("appserv-tests");

    private String host;
    private String port;
    private String contextRoot;

    public WebTest(String[] args) {
        host = args[0];
        port = args[1];
        contextRoot = args[2];
    }
    
    public static void main(String[] args) {
        stat.addDescription("Unit test for issue 8535");
        WebTest webTest = new WebTest(args);
        webTest.doTest();
	    stat.printSummary();
    }

    public void doTest() {
     
        try {

            URL url = new URL("http://" + host  + ":" + port
                              + contextRoot + "/");
            System.out.println("Connecting to: " + url.toString());
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.connect();
            int responseCode = conn.getResponseCode();

            if (responseCode == HttpURLConnection.HTTP_OK) {
                InputStream is = null;
                BufferedReader reader = null;
                boolean match = false;
                try {
                    is = conn.getInputStream();
                    reader = new BufferedReader(new InputStreamReader(is));
                    String line = null;
                    while ((line = reader.readLine()) != null) {
                        System.out.println(line);
                        if (EXPECTED.equals(line)) {
                            match = true;
                            break;
                        }
                    }
                } finally {
                    if (is != null) {
                        try {
                            is.close();
                        } catch(IOException ex) {
                        }
                    }
                    if (reader != null) {
                        try {
                            reader.close();
                        } catch(IOException ex) {
                        }
                    }
                }
                stat.addStatus(TEST_NAME, ((match)? stat.PASS : stat.FAIL));
            } else {
                System.err.println("Incorrect response code: Expected: "
                                   + HttpURLConnection.HTTP_OK
                                   + ", received: " + responseCode);
                stat.addStatus(TEST_NAME, stat.FAIL);
            }

        } catch( Exception ex) {
            ex.printStackTrace();
            stat.addStatus(TEST_NAME, stat.FAIL);
        }
    }

}
