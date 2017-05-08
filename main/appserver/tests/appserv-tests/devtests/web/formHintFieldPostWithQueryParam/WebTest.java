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

/**
 * Unit test for 6286450: AS cannot determine request charset using
 * form-hint-field if form action contains query parameters.
 */
public class WebTest {
    
    private static SimpleReporterAdapter stat
        = new SimpleReporterAdapter("appserv-tests");
    private static final String TEST_NAME
        = "form-hint-field-post-with-query-param";
    private static final String EXPECTED_RESPONSE = "Shift_JIS";

    private String host;
    private String port;
    private String contextRoot;

    public WebTest(String[] args) {
        host = args[0];
        port = args[1];
        contextRoot = args[2];
    }

    public static void main(String args[]) {
        stat.addDescription("Unit test for 6286450");
        WebTest webTest = new WebTest(args);
        webTest.doTest();
        stat.printSummary(TEST_NAME);
    }

    public void doTest() {     
        try { 
            run();
        } catch (Exception ex) {
            stat.addStatus(TEST_NAME, stat.FAIL);
            ex.printStackTrace();
        }
    }

    /*
     * Sends a POST request with a query param and body
     */
    private void run() throws Exception {

        // Construct body
        String body = URLEncoder.encode("j_encoding", "UTF-8")
            + "=" + URLEncoder.encode("Shift_JIS", "UTF-8");
    
        // Create a socket to the host
        Socket socket = new Socket(host, new Integer(port).intValue());
    
        // Send header
        BufferedWriter wr = new BufferedWriter(new OutputStreamWriter(
            socket.getOutputStream(), "UTF8"));
        wr.write("POST " + contextRoot + "/a.jsp?query1=value1"
                 + " HTTP/1.0\r\n");
        wr.write("Content-Length: " + body.length() + "\r\n");
        wr.write("Content-Type: application/x-www-form-urlencoded\r\n");
        wr.write("\r\n");
    
        // Send body
        wr.write(body);
        wr.flush();

        // Read response
        BufferedReader bis = new BufferedReader(
            new InputStreamReader(socket.getInputStream()));
        String line = null;
        String lastLine = null;
        while ((line = bis.readLine()) != null) {
            lastLine = line;
        }

        if (!EXPECTED_RESPONSE.equals(lastLine)) {
            System.err.println("Wrong response. Expected: "
                               + EXPECTED_RESPONSE
                               + ", received: " + lastLine);
            stat.addStatus(TEST_NAME, stat.FAIL);
        } else {
            stat.addStatus(TEST_NAME, stat.PASS);
        }
   }
  
}
