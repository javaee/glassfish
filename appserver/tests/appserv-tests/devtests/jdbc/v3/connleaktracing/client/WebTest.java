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

package client;

import java.lang.*;
import java.io.*;
import java.net.*;

import com.sun.ejte.ccl.reporter.*;

public class WebTest {
    private static final String TEST_NAME = "jdbc-connectionleaktracing";
    static SimpleReporterAdapter stat = new SimpleReporterAdapter("appserv-tests");
    static int count;

    public static void main(String args[]) {

        stat.addDescription(TEST_NAME);

        String host = args[0];
        String portS = args[1];
        String contextRoot = args[2];

        int port = Integer.valueOf(portS);

        goGet(host, port, "TEST", contextRoot + "/SimpleServlet");
        stat.printSummary(TEST_NAME);
    }

    private static void goGet(String host, int port,
                              String result, String contextPath) {

        try {
            long time = System.currentTimeMillis();
            Socket s = new Socket(host, port);
            s.setSoTimeout(1000000);
            OutputStream os = s.getOutputStream();

            contextPath += "?url=" + contextPath;
            System.out.println(("GET " + contextPath + " HTTP/1.1\n"));
            os.write(("GET " + contextPath + " HTTP/1.1\n").getBytes());
            os.write("Host: localhost\n".getBytes());
            os.write("\n".getBytes());

            InputStream is = s.getInputStream();
            System.out.println("Time: " + (System.currentTimeMillis() - time));
            BufferedReader bis = new BufferedReader(new InputStreamReader(is));
            String line = null;

            int index;
            while ((line = bis.readLine()) != null) {
                index = line.indexOf(result);
                System.out.println("[Server response]" + line);

                if (index != -1) {
                    index = line.indexOf(":");
                    String status = line.substring(index + 1);

                    if (status.equalsIgnoreCase("PASS")) {
                        stat.addStatus(TEST_NAME, stat.PASS);
                    } else {
                        stat.addStatus(TEST_NAME, stat.FAIL);
                    }
                }

                int pos = line.indexOf("END_OF_TEST");
                if (pos != -1) {
                    bis.close();
                    is.close();
                    break;
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            stat.addStatus(TEST_NAME, stat.FAIL);
        }
    }
}
