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

package test;

import java.io.IOException;
import java.io.StringWriter;
import java.io.PrintWriter;
import java.io.BufferedWriter;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.URL;
import java.net.HttpURLConnection;
import java.util.Enumeration;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class ServletTest2 extends HttpServlet {

    public final static int tests[][] = {
                                         {2, 25},
                                         {50, 50},
                                         {25, 121},
                                         {50, 139},
                                         {100, 94},
                                         {2, 4097},
                                         {50, 5000},
                                         {25, 12100},
                                         {50, 1390},
                                         {100, 100}
                                        };


    public void doGet(HttpServletRequest req,
                      HttpServletResponse resp)
      throws IOException, ServletException {
        boolean passed = true;
        int port = req.getLocalPort();
        URL url = new URL("http://localhost:" + port + "/web-readLineIOException/ServletTest");

        resp.setContentType("text/html");
        PrintWriter writer = resp.getWriter();

        for (int i=0; i < tests.length; i++) {
            int numLines = tests[i][0];
            int lineSize = tests[i][1];
            writer.print(testRequest(url, numLines, lineSize) == 0 ? "readLine::PASSED\n"
                                                                    : "readline::FAILED\n");
        }

        writer.flush();
        writer.close();

    }

    private int testRequest(URL url, int numLines, int lineSize) throws IOException {
        StringWriter sw = new StringWriter();
        BufferedWriter bw = new BufferedWriter(sw);
        for (int i=0; i< numLines; i++) {
            for (int j=0; j<lineSize; j++) {
                bw.write('x');
            }
            bw.newLine();
        }
        bw.flush();
        bw.close();
        String send = sw.toString();
        String response = null;
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setDoOutput(true);
        conn.setDoInput(true);
        conn.setRequestMethod("POST");
        conn.setRequestProperty("content-length", String.valueOf(send.length()));
        conn.setRequestProperty("content-type", "text/plain");
        conn.setRequestProperty("Pragma", "no-cache");
        bw = new BufferedWriter(new OutputStreamWriter(conn.getOutputStream()));
        bw.write(send);
        bw.flush();
        bw.close();

        try {
            InputStream is = conn.getInputStream();
            BufferedReader reader = new BufferedReader(new InputStreamReader(is));
            String result = reader.readLine();
            is.close();
            return 0;
        }
        catch (IOException e) {
            e.printStackTrace();
            return -1;
        }
    }
}
