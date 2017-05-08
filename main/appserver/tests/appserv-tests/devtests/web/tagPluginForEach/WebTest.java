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

import java.lang.*;
import java.io.*;
import java.net.*;

import com.sun.ejte.ccl.reporter.*;

/*
 * Unit test for Bugzilla 28840 ("NPE when using an Iterator for items in a
 * JSTL forEach tag")
 */
public class WebTest {

    private static SimpleReporterAdapter stat
        = new SimpleReporterAdapter("appserv-tests");
    private static final String TEST_NAME = "tag-plugin-for-each";

    public static void main(String[] args) {

        stat.addDescription("Unit test for Bugzilla 28840");

        String host = args[0];
        String port = args[1];
        String contextRoot = args[2];
        String hostPortRoot = host  + ":" + port + contextRoot;

        boolean success = false;

        success = doTest("http://" + hostPortRoot + "/jsp/iterator.jsp",
                         "One","Two", "Three");

        if (success) {
            success = doTest("http://" + hostPortRoot + "/jsp/map.jsp",
                             "Three=Three", "One=One", "Two=Two");
        }

        if (success) {
            success = doTest("http://" + hostPortRoot + "/jsp/enum.jsp",
                             "One", "Two", "Three");
        }

        if (success) {
            stat.addStatus(TEST_NAME, stat.PASS);
        } else {
            stat.addStatus(TEST_NAME, stat.FAIL);
        }

        stat.printSummary(TEST_NAME);
    }

    /*
     * Returns true in case of success, false otherwise.
     */
    private static boolean doTest(String urlString, 
                                  String expected1,
                                  String expected2,
                                  String expected3) {
        try { 
            URL url = new URL(urlString);
            System.out.println("Connecting to: " + url.toString());
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.connect();
            int responseCode = conn.getResponseCode();
            if (responseCode != 200) { 
                System.out.println("Wrong response code. Expected: 200"
                                   + ", received: " + responseCode);
                return false;
            }

            InputStream is = conn.getInputStream();
            BufferedReader input = new BufferedReader(new InputStreamReader(is));
            String line = null;
            boolean found = false;
            while ((line = input.readLine()) != null) {
                if (line.contains(expected1) &&
                    line.contains(expected2) &&
                    line.contains(expected3)) {
                    found = true;
                }
            }

            if (!found) {
                System.out.println("Invalid response. Response did not " +
                                   "contain one of the expected strings: " + 
                                   expected1 + "," + 
                                   expected2 + "," +
                                   expected3);
                return false;
            }

        } catch (Exception ex) {
            ex.printStackTrace();
            return false;
        }

        return true;
    }

}
