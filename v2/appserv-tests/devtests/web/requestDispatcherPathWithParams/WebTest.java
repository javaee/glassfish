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
import com.sun.ejte.ccl.reporter.*;

/*
 * Unit test for 4873423 ("getRequestURI not returning expected value").
 */
public class WebTest {

    private static SimpleReporterAdapter stat
        = new SimpleReporterAdapter("appserv-tests");
    private static final String TEST_NAME = "request-dispatcher-path-with-params";
    private static final String REQUEST_URI = "/web-" + TEST_NAME + "/jsp/forwardTo.jsp;abcd=xyz";
    private static final String QUERY_STRING = "test=john";

    private String host;
    private String port;
    private String contextRoot;
    private Socket sock = null;

    public WebTest(String[] args) {
        host = args[0];
        port = args[1];
        contextRoot = args[2];
    }
    
    public static void main(String[] args) {
        stat.addDescription("Unit test for 4873423");
        WebTest webTest = new WebTest(args);
        webTest.doTest();
        stat.printSummary(TEST_NAME);
    }

    public void doTest() {     
        try { 
            invokeJsp();
            stat.addStatus(TEST_NAME, stat.PASS);
        } catch (Exception ex) {
            System.out.println(TEST_NAME + " test failed.");
            stat.addStatus(TEST_NAME, stat.FAIL);
            ex.printStackTrace();
        } finally {
            try {
                if (sock != null) {
                    sock.close();
                }
            } catch (IOException ioe) {
                // ignore
            }
        }
    }

    private void invokeJsp() throws Exception {
         
        sock = new Socket(host, new Integer(port).intValue());
        OutputStream os = sock.getOutputStream();
        String get = "GET " + contextRoot + "/jsp/forwardFrom.jsp HTTP/1.0\n";
        System.out.println(get);
        os.write(get.getBytes());
        os.write("\n".getBytes());

        InputStream is = null;
        BufferedReader bis = null;
        try {
            is = sock.getInputStream();
            bis = new BufferedReader(new InputStreamReader(is));
            String line = null;
            String secondToLastLine = null;
            String lastLine = null;
            int i=0;
            while ((line = bis.readLine()) != null) {
                System.out.println(i++ + ": " + line);
                secondToLastLine = lastLine;
                lastLine = line;
            }
            System.out.println("requestURI: " + secondToLastLine);
            System.out.println("queryString: " + lastLine);
            if (!REQUEST_URI.equals(secondToLastLine)) {
                throw new Exception("Wrong requestURI. Received: " +
                    secondToLastLine  + ", Expected: " + REQUEST_URI);
            }
            if (!QUERY_STRING.equals(lastLine)) {
                throw new Exception("Wrong query string. Received: " +
                    lastLine  + ", Expected: " + QUERY_STRING);
            }
        } finally {
            try {
                if (is != null) {
                    is.close();
                }
            } catch (IOException ioe) {
                // ignore
            }
            try {
                if (bis != null) {
                    bis.close();
                }
            } catch (IOException ioe) {
                // ignore
            }
        }
    }
}
