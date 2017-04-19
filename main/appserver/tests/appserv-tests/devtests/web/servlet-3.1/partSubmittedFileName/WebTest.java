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
import java.nio.file.Files;
import java.nio.file.Paths;

import com.sun.ejte.ccl.reporter.*;

public class WebTest {
    
    private static final SimpleReporterAdapter stat =
        new SimpleReporterAdapter("appserv-tests");

    private static final String TEST_NAME = "servlet-3.1-part-submitted-file-name";

    public static void main(String args[]) {

        stat.addDescription("Unit test for part submitted file name");

        String host = args[0];
        String portS = args[1];
        String contextRoot = args[2];
        String testdir = args[3];

        int port = new Integer(portS).intValue();
        try {
            goPost(host, port, contextRoot + "/ServletTest", testdir);
            stat.addStatus(TEST_NAME, stat.PASS);
        } catch (Throwable t) {
            stat.addStatus(TEST_NAME, stat.FAIL);
            t.printStackTrace();
        }

        stat.printSummary(TEST_NAME);
    }

    private static void goPost(String host, int port, String contextPath,
             String dir) throws Exception
    {
        // First compose the post request data
        ByteArrayOutputStream ba = new ByteArrayOutputStream();
        ba.write("--AaB03x\r\n".getBytes());
        // Write header for the first file
        ba.write("Content-Disposition: form-data; name=\"myFile\"; filename=\"test.txt\"\r\n".getBytes());
        ba.write("Content-Type: text/plain\r\n\r\n".getBytes());

        // Write content of first text file
        byte[] file1Bytes = Files.readAllBytes(Paths.get(dir, "test.txt"));
        ba.write(file1Bytes, 0, file1Bytes.length);
        ba.write("\r\n--AaB03x\r\n".getBytes());

        // Write header for the second file
        ba.write("Content-Disposition: form-data; name=\"myFile2\"; filename=\"test2.txt\"\r\n".getBytes());
        ba.write("Content-Type: text/plain\r\n\r\n".getBytes());

        // Write content of second text file
        byte[] file2Bytes = Files.readAllBytes(Paths.get(dir, "test2.txt"));
        ba.write(file2Bytes, 0, file2Bytes.length);
        ba.write("\r\n--AaB03x\r\n".getBytes());

        // Write header for the third part, this is has no file name
        ba.write("Content-Disposition: form-data; name=\"xyz\"\r\n".getBytes());
        ba.write("Content-Type: text/plain\r\n\r\n".getBytes());
        ba.write("1234567abcdefg".getBytes());

        // Write boundary end
        ba.write("\r\n--AaB03x--\r\n".getBytes());
        byte[] data = ba.toByteArray();

        // Compose the post request header
        StringBuilder header = new StringBuilder();
        header.append("POST " + contextPath + " HTTP/1.1\r\n");
        header.append("Host: localhost\r\n");
        header.append("Connection: close\r\n");
        header.append("Content-Type: multipart/form-data; boundary=AaB03x\r\n");
        header.append("Content-Length: " + data.length + "\r\n\r\n");

        try (
            Socket sock = new Socket(host, port);
            OutputStream os = sock.getOutputStream();
            InputStream is = sock.getInputStream();
            BufferedReader bis = new BufferedReader(new InputStreamReader(is));
        ) {
            System.out.println(header);
            os.write(header.toString().getBytes());
            os.write(data);

            int i = 0;
            int partCount = -1;
            int failCount = 0;
            int expectedCount = 0;

            String line = null;
            while ((line = bis.readLine()) != null) {
                System.out.println(i++ + ": " + line);
                if (line.startsWith("getParameter")) {
                    partCount++;
                    expectedCount++;
                    failCount += check(partCount, 0, line);
                } else if (line.startsWith("Part name:")) {
                    partCount++;
                    expectedCount++;
                    failCount += check(partCount, 0, line);
                } else if (line.startsWith("Submitted File Name:")) {
                    expectedCount++;
                    failCount += check(partCount, 1, line);
                } else if (line.startsWith("Size:")) {
                    expectedCount++;
                    failCount += check(partCount, 2, line);
                } else if (line.startsWith("Content Type:")){
                    expectedCount++;
                    failCount += check(partCount, 3, line);
                } else if (line.startsWith("Header Names:")) {
                    expectedCount++;
                    failCount += check(partCount, 4, line);
                }
            }
            if (expectedCount != 16 || failCount > 0) {
                throw new Exception("Wrong expected count or Contains invalid values: " +
                        expectedCount + ", " + failCount);
            }
        }
   }

   static String[][] expected = {
       {"1234567abcdefg"},
       {"myFile", "test.txt", "36", "text/plain", "content-disposition content-type" },
       {"myFile2", "test2.txt", "37", "text/plain", 
            "content-disposition content-type" },
       {"xyz", "null", "14", "text/plain", "content-disposition content-type"}
   };

   private static int check(int x, int y, String line) {
       if (line.contains(expected[x][y]))
           return 0;
       return 1;
   }
}
