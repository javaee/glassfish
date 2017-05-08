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

import java.io.*;
import java.net.*;
import com.sun.ejte.ccl.reporter.*;

/**
 * Unit test for IT:
 *     13129: Session is null in request with URL containing jsessionid parameter
 */
public class WebTest {

    private static SimpleReporterAdapter stat
        = new SimpleReporterAdapter("appserv-tests");
    private static final String TEST_NAME = "jsessionid-parameter";

    private String host;
    private String port;
    private String contextRoot;

    public WebTest(String[] args) {
        host = args[0];
        port = args[1];
        contextRoot = args[2];
    }
    
    public static void main(String[] args) {
        stat.addDescription("Unit test for IT 13129");
        WebTest webTest = new WebTest(args);
        try {
            String id = webTest.doTest("/test.jsp?a=1", "1");
            webTest.doTest("/test.jsp;jsessionid="+ id, "1");
            stat.addStatus(TEST_NAME, stat.PASS);
        } catch(Exception ex) {
            stat.addStatus(TEST_NAME, stat.FAIL);
        }
        stat.printSummary(TEST_NAME);
    }

    public String doTest(String url, String expected) throws Exception {

        Socket sock = null;
        OutputStream os = null;
        InputStream is = null;
        BufferedReader bis = null;
        try {
            sock = new Socket(host, new Integer(port).intValue());
            os = sock.getOutputStream();
            String get = "GET " + contextRoot + url + " HTTP/1.0\n";
            System.out.print(get);
            os.write(get.getBytes());
            os.write("\n".getBytes());
        
            is = sock.getInputStream();
            bis = new BufferedReader(new InputStreamReader(is));

            String line = null;
            String id = null;
            String a = null;
            while ((line = bis.readLine()) != null) {
                if (line.startsWith("id=")) {
                    id = line.substring(3);
                } else if (line.startsWith("a=")) {
                    a = line.substring(2);
                }
            }

            if (!expected.equals(a)) {
                throw new Exception("Unexpected result: " + a + ", expected = " + expected);
            }

            System.out.println("session id = " + id);
            return id;
	    } finally {
            if (bis != null) {
                try {
                    bis.close();
                } catch(IOException ex) {
                }
            }
            if (is != null) {
                try {
                    is.close();
                } catch(IOException ex) {
                }
            }
            if (os != null) {
                try {
                    os.close();
                } catch(IOException ex) {
                }
            }
            if (sock != null) {
                try {
                    sock.close();
                } catch(IOException ex) {
                }
            }
        }
    }
}
