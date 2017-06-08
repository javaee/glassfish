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
 * Unit test for handling of EL expressions #{...} in tag attributes
 * This includes bugs 6372687, 6377689, 6380354
 *
 * Make sure that is treated as a literal when jspversion of the tld is
 * 2.0 or less.
 */
public class WebTest {

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
        stat.addDescription("Unit test for enum support in EL");
        WebTest webTest = new WebTest(args);
        webTest.doTest("/enum.jsp", "jsp-el-enum-test");
	stat.printSummary();
    }

    private boolean checkValue(BufferedReader input,
                               String expectedValue) 
            throws Exception {
        String line = input.readLine();
        if (line == null)
            return true;
        while ("".equals(line.trim())) {
            line = input.readLine();
            if (line == null)
                return true;
        }
//        System.out.println("###" + line + "$$$");
        if (!line.trim().equals(expectedValue)) {
            System.err.println("Wrong response. Expected value: "
                + expectedValue + ", received: " + line);
            return true;
        }
        return false; 
    }

    public void doTest(String path, String testName) {

        InputStream is = null;
        BufferedReader input = null;
        try {
            URL url = new URL("http://" + host  + ':' + port + '/' + contextRoot + path);
            System.out.println("Connecting to: " + url.toString());
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.connect();
            int responseCode = conn.getResponseCode();
            if (responseCode != 200) {
                System.err.println("Wrong response code. Expected: 200"
                                   + ", received: " + responseCode);
                stat.addStatus(testName, stat.FAIL);
            } else {
                is = conn.getInputStream();
                input = new BufferedReader(new InputStreamReader(
is));
                boolean error = false;
                if (checkValue(input, "PASS"))
                    error = true;
                if (checkValue(input, "PASS"))
                    error = true;
                if (checkValue(input, "diamond"))
                    error = true;
                if (checkValue(input, "diamond"))
                    error = true;
                if (error) {
                    stat.addStatus(testName, stat.FAIL);
                } else {
                    stat.addStatus(testName, stat.PASS);
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            stat.addStatus(testName, stat.FAIL);
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
