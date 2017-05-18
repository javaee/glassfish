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

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.Socket;
import java.net.URL;
import java.net.URLConnection;

import com.sun.appserv.test.util.results.SimpleReporterAdapter;

public class WebTest {
    private static final SimpleReporterAdapter stat = new SimpleReporterAdapter("appserv-tests", "httpServletDoPost");

    public static void main(String args[]) {
        // The stat reporter writes out the test info and results
        // into the top-level quicklook directory during a run.
        stat.addDescription("Invoking HttpServlet.doPost");
        String host = args[0];
        String portS = args[1];
        String contextRoot = args[2];
        int port = new Integer(portS);
        try {
            goGet(host, port, contextRoot + "/ServletTest");
        } catch (Throwable t) {
            System.out.println(t.getMessage());
        }
        stat.printSummary();
    }

    private static void goGet(String host, int port, String contextPath)
        throws Exception {
        checkResponseCode(host, port, contextPath);
        test(host, port, contextPath, "HTTP/1.1");
        test(host, port, contextPath, "HTTP/1.0");
    }

    private static void checkResponseCode(final String host, final int port, final String contextPath)
        throws IOException {
        final URL url = new URL("http://" + host + ":" + port + contextPath);
        System.out.println("\n Invoking url: " + url.toString());
        final URLConnection conn = url.openConnection();
        DataOutputStream out = null;
        try {
            if (conn instanceof HttpURLConnection) {
                HttpURLConnection urlConnection = (HttpURLConnection) conn;
                urlConnection.setDoOutput(true);
                out = new DataOutputStream(urlConnection.getOutputStream());
                out.writeByte(1);
                int responseCode = urlConnection.getResponseCode();
                stat.addStatus("httpServletDoPost",
                    urlConnection.getResponseCode() == 405 ? SimpleReporterAdapter.PASS : SimpleReporterAdapter.FAIL);

                urlConnection.disconnect();
            }
        } finally {
            if(out != null) {
                out.close();
            }
        }
    }

    private static void test(final String host, final int port, final String contextPath, final String protocol)
        throws IOException {
        boolean mark = false;
        int i = 0;
        final String name = "httpServletDoPost-noCL-" + protocol;
        Socket s = new Socket(host, port);
        try {
            s.setSoTimeout(10000);
            OutputStream os = s.getOutputStream();
            System.out.println("POST " + contextPath + " " + protocol + "\n");
            os.write(("POST " + contextPath + " " + protocol + "\n").getBytes());
            os.write("Host: localhost\r\n".getBytes());
            os.write("content-length: 0\r\n".getBytes());
            os.write("\r\n".getBytes());
            InputStream is = s.getInputStream();
            BufferedReader bis = new BufferedReader(new InputStreamReader(is));
            String line;
            int index;
            while ((line = bis.readLine()) != null) {
                index = line.indexOf("httpServletDoPost");
                System.out.println(i++ + ": " + line);
                if (index != -1) {
                    index = line.indexOf("::");
                    String status = line.substring(index + 1);
                    if ("FAIL".equalsIgnoreCase(status)) {
                        stat.addStatus(name, SimpleReporterAdapter.FAIL);
                        mark = true;
                    }
                }
            }
        } catch (Exception ex) {
        } finally {
            s.close();
            if (!mark && i > 0) {
                stat.addStatus(name, SimpleReporterAdapter.PASS);
            }
        }
    }

}
