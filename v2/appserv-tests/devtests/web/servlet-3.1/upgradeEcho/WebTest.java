/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2012-2014 Oracle and/or its affiliates. All rights reserved.
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
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;
import com.sun.ejte.ccl.reporter.*;

/*
 * Unit test for upgrade echo 
 */
public class WebTest {

    private static String TEST_NAME = "upgrade-echo";
    private static String EXPECTED_RESPONSE = "HelloWorld";
    private static final String CRLF = "\r\n";

    private static SimpleReporterAdapter stat
        = new SimpleReporterAdapter("appserv-tests");

    public static void main(String[] args) {
        String host = args[0];
        int port = Integer.parseInt(args[1]);
        String contextRoot = args[2];
        stat.addDescription("Unit test for upgrade");

        try {
            Socket s = null;

            InputStream input = null;
            OutputStream output = null;
            boolean expected = false;
            try {
                s = new Socket(host, port);
                output = s.getOutputStream();

                String reqStr = "POST " + contextRoot + "/test HTTP/1.1" + CRLF;
                reqStr += "User-Agent: Java/1.6.0_33" + CRLF;
                reqStr += "Host: " + host + ":" + port + CRLF;
                reqStr += "Accept: text/html, image/gif, image/jpeg, *; q=.2, */*; q=.2" + CRLF;
                reqStr += "Upgrade: echo" + CRLF;
                reqStr += "Connection: Upgrade" + CRLF;
                reqStr += "Content-type: application/x-www-form-urlencoded" + CRLF;
                reqStr += CRLF;
                output.write(reqStr.getBytes());

                input = s.getInputStream();
                int len = -1;
                byte b[] = new byte[1024];
                StringBuilder sb = new StringBuilder();
                //consume headers
                System.out.println("Consuming headers");
                boolean containsUpgrade = false;
                while ((len = input.read(b)) != -1) {
                    String line = new String(b, 0, len);
                    System.out.println(line);
                    sb.append(line);
                    String temp = sb.toString();
                    if (!containsUpgrade &&  temp.toLowerCase().contains("upgrade")) {
                        containsUpgrade = true;
                    }
                    if (temp.contains("\r\n\r\n") || temp.contains("\n\n")) {
                        break;
                    }
                }

                writeChunk(output, "Hello");
                int sleepInSeconds = 1;
                System.out.format("Sleeping %d sec\n", sleepInSeconds);
                Thread.sleep(sleepInSeconds * 1000);
                writeChunk(output, "World");
                
                // read data without using readLine
                long startTime = System.currentTimeMillis();
                System.out.println("Consuming results");
                while ((len = input.read(b)) != -1) {
                    String line = new String(b, 0, len);
                    sb.append(line);
                    boolean hasInfo = sb.toString().replace("/", "").contains(EXPECTED_RESPONSE);
                    boolean hasError = sb.toString().contains("WrongClassLoader");
                    if (hasInfo || hasError || System.currentTimeMillis() - startTime > 20 * 1000) {
                        break;
                    } 
                }

                System.out.println(sb.toString());
                StringTokenizer tokens = new StringTokenizer(sb.toString(), CRLF);
                String line = null;
                while (tokens.hasMoreTokens()) {
                    line = tokens.nextToken();
                }

                expected = containsUpgrade && line.contains("/")
                        && (line.indexOf("/") < line.indexOf("d"))
                        && line.replace("/", "").equals(EXPECTED_RESPONSE);
            } finally {
                try {
                    if (input != null) {
                        System.out.println("# Closing input...");
                        input.close();
                        System.out.println("# Input closed.");
                    }
                } catch(Exception ex) {
                }

                try {
                    if (output != null) {
                        System.out.println("# Closing output...");
                        output.close();
                        System.out.println("# Output closed .");
                    }
                } catch(Exception ex) {
                }
                try {
                    if (s != null) {
                        System.out.println("# Closing socket...");
                        s.close();
                        System.out.println("# Socked closed.");
                    }
                } catch(Exception ex) {
                }
            }

            // server.log should contain "##### OnError" and
            // stacktrace of produced exception (EOF).

            // sleep is here only for verifying that onError was
            // called before this process ended.
            Thread.sleep(10000);

            stat.addStatus(TEST_NAME, ((expected) ? stat.PASS : stat.FAIL));
        } catch(Exception ex) {
            ex.printStackTrace();
            stat.addStatus(TEST_NAME, stat.FAIL);
        }

        stat.printSummary();
    }

    private static void writeChunk(OutputStream out, String data) throws IOException {
        if (data != null) {
            out.write(data.getBytes());
        }
        out.flush();
    }
}
