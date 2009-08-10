/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2009 Sun Microsystems, Inc. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License. You can obtain
 * a copy of the License at https://glassfish.dev.java.net/public/CDDL+GPL.html
 * or glassfish/bootstrap/legal/LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 *
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at glassfish/bootstrap/legal/LICENSE.txt.
 * Sun designates this particular file as subject to the "Classpath" exception
 * as provided by Sun in the GPL Version 2 section of the License file that
 * accompanied this code.  If applicable, add the following below the License
 * Header, with the fields enclosed by brackets [] replaced by your own
 * identifying information: "Portions Copyrighted [year]
 * [name of copyright owner]"
 *
 * Contributor(s):
 *
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

import com.sun.ejte.ccl.reporter.*;

public class WebTest
{
    
    private static URLConnection conn = null;
    private static URL url;
    private static int count = 0;
    
    static SimpleReporterAdapter stat=
        new SimpleReporterAdapter("appserv-tests");

    public static void main(String args[])
    {

        stat.addDescription("Unit test for multipart request");

        String host = args[0];
        String portS = args[1];
        String contextRoot = args[2];

        int port = new Integer(portS).intValue();
        try {
            goPost(host, port, contextRoot + "/ServletTest" );
        } catch (Throwable t) {
            System.out.println("Exception: " + t);
        }

        stat.printSummary("MultipartTest");
    }

    private static void goPost(String host, int port, String contextPath)
         throws Exception
    {
        // First compose the post request data
        ByteArrayOutputStream ba = new ByteArrayOutputStream();
        ba.write("--AaB03x\r\n".getBytes());
        // Write header for the first file
        ba.write("Content-Disposition: form-data; name=\"myFile\"; filename=\"test.txt\"\r\n".getBytes());
        ba.write("Content-Type: text/plain\r\n\r\n".getBytes());

        // Write content of first text file
        InputStream is = new FileInputStream ("test.txt");
        int c;
        while ((c = is.read()) != -1) {
            ba.write(c);
        }
        ba.write("\r\n--AaB03x\r\n".getBytes());

        // Write header for the second file
        ba.write("Content-Disposition: form-data; name=\"myFile2\"; filename=\"Test.war\"\r\n".getBytes());
        ba.write("Content-Type: application/x-java-archive\r\n\r\n".getBytes());

        // Write content of second binary file
        is = new FileInputStream ("Test.war");
        while ((c = is.read()) != -1) {
            ba.write(c);
        }
        // Write boundary end
        ba.write("\r\n--AaB03x--\r\n".getBytes());
        byte[] data = ba.toByteArray();

        // Compose the post request header
        StringBuilder header = new StringBuilder();
        header.append("POST " + contextPath + " HTTP/1.1\r\n");
        header.append("Host: localhost\r\n");
        header.append("Content-Type: multipart/form-data, boundary=AaB03x\r\n");
        header.append("Content-Length: " + data.length + "\r\n\r\n");

        // Now the actual request
        Socket sock = new Socket(host, port);
        OutputStream os = sock.getOutputStream();
        System.out.println(header);
        os.write(header.toString().getBytes());
        os.write(data);

        int i = 0;
        int partCount = -1;
        int failCount = 0;
        int expectedCount = 0;

        is = sock.getInputStream();
        BufferedReader bis = new BufferedReader(new InputStreamReader(is));
        String line = null;
        while ((line = bis.readLine()) != null) {
            System.out.println(i++ + ": " + line);
            if (line.startsWith("Part name:")) {
                partCount++;
                expectedCount++;
                failCount += check(partCount, 0, line);
            } else if (line.startsWith("Size:")) {
                expectedCount++;
                failCount += check(partCount, 1, line);
            } else if (line.startsWith("Content Type:")){
                expectedCount++;
                failCount += check(partCount, 2, line);
            } else if (line.startsWith("Header Names:")) {
                expectedCount++;
                failCount += check(partCount, 3, line);
            }
        }
        if (expectedCount != 8 || failCount > 0) {
            stat.addStatus("multiPartTest", stat.FAIL);
        } else { 
            stat.addStatus("multiPartTest", stat.PASS);
        }

        bis.close();
        is.close();
        os.close();
        sock.close();
   }

   static String[][] expected = {
       {"myFile", "36", "text/plain", "content-disposition content-type" },
       {"myFile2", "4134", "application/x-java-archive", 
            "content-disposition content-type" }};

   private static int check(int x, int y, String line) {
       if (line.contains(expected[x][y]))
           return 0;
       return 1;
   }
  
}
