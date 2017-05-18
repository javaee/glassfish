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
 * Unit test for puggability
 */
public class WebTest {

    private static SimpleReporterAdapter stat
        = new SimpleReporterAdapter("appserv-tests");
    private static final String TEST_NAME = "servlet-3.0-pluggable-sharedlibraries-war";
    private static final String[] EXPECTED_RESPONSE = {"none","CALLED SHAREDLIB-1;CALLED SHAREDLIB-2;CALLED SHAREDLIB-3;CALLED SHAREDLIB-4;CALLED APPLIB-1;null","CALLED SHAREDLIB-1;CALLED SHAREDLIB-2;CALLED SHAREDLIB-3;CALLED SHAREDLIB-4;null;CALLED APPLIB-2"};

    private String host;
    private String port;
    private String testNumber;
    private String contextRoot;

    public WebTest(String[] args) {
        host = args[0];
        port = args[1];
        contextRoot = args[2];
        testNumber = args[3];
    }
    
    public static void main(String[] args) {
        stat.addDescription("Unit test for pluggable shared libraries in WAR");
        WebTest webTest = new WebTest(args);
	if("1".equals(args[3])) {
            webTest.doTest("/mytest1");
            stat.printSummary(TEST_NAME);
	}
	if("2".equals(args[3])) {
            webTest.doTest("/mytest2");
            stat.printSummary(TEST_NAME);
	}
    }

    public void doTest(String root) {
        try { 
            invoke(root);
        } catch (Exception ex) {
            System.out.println(TEST_NAME + " test failed");
            stat.addStatus(TEST_NAME, stat.FAIL);
            ex.printStackTrace();
        }
    }

    private void invoke(String root) throws Exception {
        
        String url = "http://" + host + ":" + port + contextRoot
                     + root;
        System.out.println("Invoking " + url);
        HttpURLConnection conn = (HttpURLConnection)
            (new URL(url)).openConnection();

        int code = conn.getResponseCode();
        if (code != 200) {
            System.out.println("Unexpected return code: " + code);
            stat.addStatus(TEST_NAME, stat.FAIL);
        } else {
            InputStream is = null;
            BufferedReader input = null;
            String line = null;
            try {
                is = conn.getInputStream();
                input = new BufferedReader(new InputStreamReader(is));
                line = input.readLine();
            } finally {
                try {
                    if (is != null) {
                        is.close();
                    }
                } catch(IOException ioe) {
                    // ignore
                }
                try {
                    if (input != null) {
                        input.close();
                    }
                } catch(IOException ioe) {
                    // ignore
                }
            }
            if (EXPECTED_RESPONSE[(new Integer(testNumber)).intValue()].equals(line)) {
                System.out.println("RESPONSE : " + line);
                stat.addStatus(TEST_NAME, stat.PASS);
            } else {
                System.out.println("Wrong response. Expected: " + 
                        EXPECTED_RESPONSE[(new Integer(testNumber)).intValue()] + ", received: " + line);
                stat.addStatus(TEST_NAME, stat.FAIL);
            }
        }    
    }
}
