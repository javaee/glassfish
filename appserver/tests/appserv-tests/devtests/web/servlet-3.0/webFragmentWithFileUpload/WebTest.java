/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2017 Oracle and/or its affiliates. All rights reserved.
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

public class WebTest {
    
    private static final SimpleReporterAdapter stat =
        new SimpleReporterAdapter("appserv-tests");

    private static final String TEST_NAME = "servlet-3.0-webFragment-with-file-upload";
    private static final String EXPECTED_RESPONSE = "Uploaded content";

    public static void main(String args[]) {

        stat.addDescription("Unit test for fileupload in web fragment");

        String host = args[0];
        String portS = args[1];
        String contextRoot = args[2];

        String testdir = System.getenv("APS_HOME") +
            "/devtests/web/servlet-3.0/webFragmentWithFileUpload/";

        int port = new Integer(portS).intValue();
        try {
            goPost(host, port, contextRoot, "/single.xhtml", testdir);
        } catch (Throwable t) {
            System.out.println(TEST_NAME + " test failed");
            stat.addStatus(TEST_NAME, stat.FAIL);
            t.printStackTrace();
        }

        stat.printSummary(TEST_NAME);
    }

    private static void goPost(String host, int port, String contextRoot,
             String urlPattern, String dir) throws Exception
    {
        Socket sock =null;
        OutputStream os = null;
        InputStream is = null;
        BufferedReader br = null;
        
        String get = "GET " + contextRoot + urlPattern + " HTTP/1.0\r\n" + "Connection: keep-alive\r\n";
        System.out.println(get);
        
        try {
            sock = new Socket(host, port);
            os = sock.getOutputStream();
            os.write(get.getBytes());
            os.write("\r\n".getBytes());
            
            is = sock.getInputStream();
            br = new BufferedReader(new InputStreamReader(is));
            int i = 0;
            String line = null;
            String viewId = null;
            String sessionId = null;
            
            //get session id and JSF view id
            while ((line = br.readLine()) != null) {
                if(line.startsWith("Set-Cookie")) {
                    sessionId = line.split(";")[0].split("=")[1];
                    continue;
                }
                if(line.contains("javax.faces.ViewState:0")) {
                    String[] results = line.split("javax.faces.ViewState:0");
                    viewId = results[1].trim().split(" ")[1].trim().split("\"")[1];
                    break;
                }
            }
            
            // Write header for the uploaded text file
            ByteArrayOutputStream ba = new ByteArrayOutputStream();
            ba.write("--AaB03x\r\n".getBytes());
            ba.write("Content-Disposition: form-data; name=\"form\"\r\n\r\n".getBytes());
            ba.write("form\r\n".getBytes());
            ba.write("--AaB03x\r\n".getBytes());
            ba.write("Content-Disposition: form-data; name=\"form:file\"; filename=\"test.txt\"\r\n".getBytes());
            ba.write("Content-Type: application/octet-stream\r\n\r\n".getBytes());
            
            // Write content of the uploaded text file
            is = new FileInputStream (dir + "test.txt");
            int c;
            while ((c = is.read()) != -1) {
                ba.write(c);
            }
            
            ba.write("\r\n--AaB03x\r\n".getBytes());
            ba.write("Content-Disposition: form-data; name=\"form:j_idt4\"\r\n\r\n".getBytes());
            ba.write("upload\r\n".getBytes());
            ba.write("--AaB03x\r\n".getBytes());
            ba.write("Content-Disposition: form-data; name=\"form:j_idt5\"\r\n\r\n".getBytes());
            ba.write("\r\n--AaB03x\r\n".getBytes());
            ba.write("Content-Disposition: form-data; name=\"javax.faces.ViewState\"\r\n\r\n".getBytes());
            ba.write((viewId + "\r\n").getBytes());
            // Write boundary end
            ba.write("--AaB03x--\r\n".getBytes());
            byte[] data = ba.toByteArray();
            System.out.println(ba.toString());
            
            // Compose the post request header
            StringBuilder postHeader = new StringBuilder();
            postHeader.append("POST " + contextRoot + urlPattern + " HTTP/1.1\r\n");
            postHeader.append("Host: localhost:8080\r\n");
            postHeader.append("Connection: close\r\n");
            postHeader.append("Content-Type: multipart/form-data; boundary=AaB03x\r\n");
            postHeader.append("Content-Length: " + data.length + "\r\n");
            postHeader.append("Cookie: JSESSIONID=" + sessionId + "\r\n\r\n");
            System.out.println(postHeader);
            
            os.write(postHeader.toString().getBytes());
            os.write(data);
            
            while ((line = br.readLine()) != null) {
                if (line.contains(EXPECTED_RESPONSE)) {
                    stat.addStatus(TEST_NAME, stat.PASS);
                    return;
                }
            }
            
            System.out.println("Wrong response. Expected: " +
                               EXPECTED_RESPONSE + ", received: " + line);
            stat.addStatus(TEST_NAME, stat.FAIL);
        } finally {
            try {
                if (is != null) {
                    is.close();
                }
            } catch(IOException ex) {
                // ignore
            }
            try {
                if (br != null) {
                    br.close();
                }
            } catch(IOException ex) {
                // ignore
            }
            try {
                if (os != null) {
                    os.close();
                }
            } catch(IOException ex) {
                // ignore
            }
            try {
                if (sock != null) {
                    sock.close(); 
                }
            } catch(IOException ex) {
                // ignore
            }
        }
        
    }
}
