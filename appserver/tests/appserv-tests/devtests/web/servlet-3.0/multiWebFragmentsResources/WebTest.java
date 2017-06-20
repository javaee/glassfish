/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2011-2017 Oracle and/or its affiliates. All rights reserved.
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
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import com.sun.ejte.ccl.reporter.*;

/*
 * Unit test for issue GLASSFISH-16058:
 * Deviation from servlet3 spec in ServletContext implementation (getResourcePaths()).
 */
public class WebTest {

    private static SimpleReporterAdapter stat
        = new SimpleReporterAdapter("appserv-tests");
    private static final String TEST_NAME = "servlet-3.0-multi-web-fragments-resources";
    private static final String PREFIX_1 = "getResourcePaths:/=";
    private static final String PREFIX_2 = "getResourcePaths:/catalog/=";
    private static final String PREFIX_3 = "getResourcePaths:/catalog=";
    private static final String EXPECTED_RESULT_1 = "/index.jsp,/catalog/,/catalog2/,/WEB-INF/,/customer/,/META-INF/";
    private static final String EXPECTED_RESULT_2 = "/catalog/offers/,/catalog/moreOffers/,/catalog/products.html,/catalog/index.html,/catalog/moreOffers2/,/catalog/another.html";


    private String host;
    private String port;
    private String contextRoot;

    public WebTest(String[] args) {
        host = args[0];
        port = args[1];
        contextRoot = args[2];
    }
    
    public static void main(String[] args) {
        stat.addDescription("Unit test for multi web fragments resources");
        WebTest webTest = new WebTest(args);
        webTest.doTest();
        stat.printSummary(TEST_NAME);
    }

    public void doTest() {
        try { 
            invoke();
        } catch (Exception ex) {
            System.out.println(TEST_NAME + " test failed");
            stat.addStatus(TEST_NAME, stat.FAIL);
            ex.printStackTrace();
        }
    }

    private void invoke() throws Exception {
        
        String url = "http://" + host + ":" + port + contextRoot + "/index.jsp";
        System.out.println(url);
        HttpURLConnection conn = (HttpURLConnection)
            (new URL(url)).openConnection();

        int code = conn.getResponseCode();
        if (code != 200) {
            System.out.println("Unexpected return code: " + code);
            stat.addStatus(TEST_NAME, stat.FAIL);
        } else {
            InputStream is = null;
            BufferedReader input = null;
            Set<String> result1 = null;
            Set<String> result2 = null;
            Set<String> result3 = null;
            String line = null;
            try {
                is = conn.getInputStream();
                input = new BufferedReader(new InputStreamReader(is));
                while ((line = input.readLine()) != null) {
                    System.out.println(line);
                    if (line.startsWith(PREFIX_1)) {
                        result1 = parseResult(line, PREFIX_1);
                    } else if (line.startsWith(PREFIX_2)) {
                        result2 = parseResult(line, PREFIX_2);
                    } else if (line.startsWith(PREFIX_3)) {
                        result3 = parseResult(line, PREFIX_3);
                    }
                }
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

            Set<String> expectedResult1 = parseResult(EXPECTED_RESULT_1, "");
            Set<String> expectedResult2 = parseResult(EXPECTED_RESULT_2, "");
            boolean status1 = expect(PREFIX_1, result1, expectedResult1);
            boolean status2 = expect(PREFIX_2, result2, expectedResult2);
            boolean status3 = expect(PREFIX_3, result3, expectedResult2);

            stat.addStatus(TEST_NAME,
                    ((status1 && status2 && status3) ? stat.PASS : stat.FAIL));
        }    
    }

    private Set<String> parseResult(String line, String prefix) {
        return new HashSet<String>(Arrays.asList(line.substring(prefix.length()).split(",")));
    }

    private boolean expect(String prefix, Set<String> result, Set<String> expectedResult) {
        boolean status = expectedResult.equals(result);

        if (!status) {
            System.out.println(prefix + ": Wrong response. Expected: " + expectedResult +", received: " + result);
        }

        return status;
    }
}
