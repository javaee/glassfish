/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 1997-2014 Oracle and/or its affiliates. All rights reserved.
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

import com.sun.ejte.ccl.reporter.*;

public class WebTest {
    
    private static int count = 0;
    private static int EXPECTED_COUNT = 1;
    
    static SimpleReporterAdapter stat=
        new SimpleReporterAdapter("appserv-tests");

    public static void main(String args[]) {

        // The stat reporter writes out the test info and results
        // into the top-level quicklook directory during a run.
      
        stat.addDescription("Double content-length header");

        String host = args[0];
        String portS = args[1];
        String contextRoot = args[2];

        int port = new Integer(portS).intValue();
        String name;
        
        try {
            goGet(host, port, "EXPIRED", contextRoot + "/ServletTest" );
            
        } catch (Throwable t) {
        } finally{
            if (count != EXPECTED_COUNT){
                stat.addStatus("web-doubleContentLength", stat.FAIL);
            }           
        }

        stat.printSummary("web/doubleContentLength---> expect 2 PASS");
    }

    private static void goGet(String host, int port,
                              String result, String contextPath)
         throws Exception {
        Socket s = new Socket(host, port);
        OutputStream os = s.getOutputStream();

        System.out.println(("POST " + contextPath + " HTTP/1.1\n"));
        try {
            os.write(("POST " + contextPath + " HTTP/1.1\n").getBytes());
            os.write(("Host: localhost\r\n").getBytes());
            os.write("content-length: 0\r\n".getBytes());
            os.write("content-length: 10\r\n".getBytes());
            os.write("content-type: application/x-www-form-urlencoded\r\n".getBytes());
            os.write("\r\n".getBytes());
            os.write("a\r\n".getBytes());
        } catch (Exception e) {
            // Web server sometimes sends the response with 400 status
            // code right after the second content-length header in which
            // case next os.write will cause an exception. 
        }

        InputStream is = s.getInputStream();
        BufferedReader bis = new BufferedReader(new InputStreamReader(is));
        String line = null;

        try{
            int index, lineNum=0;
            while ((line = bis.readLine()) != null) {
                index = line.indexOf("400");
                System.out.println(lineNum + ":  " + line);
                if (index != -1) {
                    stat.addStatus("web-doubleContentLength", stat.PASS);
                    count++;
                    break;
                } 
                lineNum++;
            }
        } catch( Exception ex){
         }
   }
  
}
