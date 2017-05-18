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

/**
 * Unit test for locale-encoding-mapping.
 */
public class WebTest {
    private static String TEST_NAME = "locale-encoding-mapping";
    private static final SimpleReporterAdapter stat = new SimpleReporterAdapter("appserv-tests", "locale-encoding-mapping");

    public static void main(String args[]) {
        // The stat reporter writes out the test info and results
        // into the top-level quicklook directory during a run.
        stat.addDescription("Invoking localeEncodingMapping");
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
        throws IOException {
        boolean setEncoding = false;
        int i = 0;
        Socket s = new Socket(host, port);

        OutputStream os = null;
        BufferedReader bis = null;
        try {
            //s.setSoTimeout(10000);
            os = s.getOutputStream();
            System.out.println("GET " + contextPath + " HTTP/1.0\n");
            os.write(("GET " + contextPath + " HTTP/1.0\r\n").getBytes());
            os.write("\r\n".getBytes());

            bis = new BufferedReader(new InputStreamReader(s.getInputStream()));
            String line;
            while ((line = bis.readLine()) != null) {
                System.out.println(i++ + ": " + line);
                if (line.contains("charset=euc-jp")) {
                    setEncoding = true;
                }
            }
        } catch (Exception ex) {
        } finally {
            if (os != null) {
                try {
                    os.close();
                } catch(Exception e) {
                }
            }
            if (bis != null) {
                try {
                    bis.close();
                } catch(Exception e) {
                }
            }
            try {
                s.close();
            } catch(Exception e) {
            }

            stat.addStatus(TEST_NAME, ((setEncoding) ? stat.PASS : stat.FAIL));
        }
    }

}
