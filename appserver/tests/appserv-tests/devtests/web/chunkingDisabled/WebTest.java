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
 * Unit test for 4861933 ("S1AS 8 (PE) removes the Content-Length
 * header (valid HTTP header)").
 *
 * This test ensures that if chunking has been disabled, a response whose
 * length exceeds the response buffer size still contains a Content-Length
 * header.
 *
 * This test is supposed to run on PE only (and will always pass on EE by
 * having the servlet print a response that is guaranteed to fit in the
 * response buffer), because the 'chunkingDisabled' http-listener property is
 * supported on PE only.
 */
public class WebTest {

    private static SimpleReporterAdapter stat
        = new SimpleReporterAdapter("appserv-tests");
    private static final String TEST_NAME = "chunked-encoding";

    private String host;
    private String port;
    private String contextRoot;

    public WebTest(String[] args) {
        host = args[0];
        port = args[1];
        contextRoot = args[2];
    }
    
    public static void main(String[] args) {
        stat.addDescription("Unit test for 4861933");
        WebTest webTest = new WebTest(args);
        webTest.doTest();
        stat.printSummary(TEST_NAME);
    }

    public void doTest() {     
        try { 
            invokeServlet();
        } catch (Exception ex) {
            System.out.println(TEST_NAME + " test failed.");
            stat.addStatus(TEST_NAME, stat.FAIL);
            ex.printStackTrace();
        }
    }

    private void invokeServlet() throws Exception {
        
        Socket sock = new Socket(host, new Integer(port).intValue());
        OutputStream os = sock.getOutputStream();

        String get = "GET " + contextRoot + "/TestServlet" + " HTTP/1.1\n";
        os.write(get.getBytes());

        String hostHeader = "Host: " + host + ":" + port + "\n";
        os.write(hostHeader.getBytes());

        os.write("\n".getBytes());
        
        InputStream is = sock.getInputStream();
        BufferedReader bis = new BufferedReader(new InputStreamReader(is));

        boolean responseOK = false;
        boolean contentLenFound = false;
        String line = null;
        while ((line = bis.readLine()) != null) {
            if (line.indexOf("HTTP/1.1 200 OK") != -1) {
                responseOK = true;
            } else if (line.toLowerCase().indexOf("content-length:") != -1) {
                System.out.println("Response Content-Length: " + line);
                contentLenFound = true;
            }
        }

        if (responseOK && contentLenFound) {
            stat.addStatus(TEST_NAME, stat.PASS);
        } else if (!responseOK) {
            System.out.println("Wrong response code, expected 200 OK");
            stat.addStatus(TEST_NAME, stat.FAIL);
        } else {
            System.out.println("Missing Content-Length response header");
            stat.addStatus(TEST_NAME, stat.FAIL);
        }
    }
}
