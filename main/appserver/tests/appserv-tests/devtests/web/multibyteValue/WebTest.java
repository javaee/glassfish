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

import com.sun.ejte.ccl.reporter.*;

/**
 * Bug 6193728: Multibyte value not processed correctly when request is
 * forwarded to another servlet
 */
public class WebTest {
    
    private static int count = 0;
    private static int EXPECTED_COUNT = 2;
    
    static SimpleReporterAdapter stat=
        new SimpleReporterAdapter("appserv-tests");

    public static void main(String args[]) {
        stat.addDescription("i18 multi byte value test");

        String host = args[0];
        String portS = args[1];
        String contextRoot = args[2];

        int port = new Integer(portS).intValue();
        String name;
        
        try {
            run(host, port, contextRoot + "/ServletTest" );
            
            if (count != EXPECTED_COUNT){
                stat.addStatus("multiByteValue POST", stat.FAIL);
            }           
        } catch (Throwable t) {
            stat.addStatus("multiByteValue", stat.FAIL);
        }

        stat.printSummary("web/multiByteValue---> expect 2 PASS");
    }

    private static void run(String host, int port, String contextPath)
         throws Exception {

        /*
         * Send a GET request
         */
        Socket s = new Socket(host, port);
        OutputStream os = s.getOutputStream();
        System.out.println(("GET " + contextPath + "?j_encoding=Shift_JIS" 
                                                            + " HTTP/1.0\n"));
        os.write(("GET " + contextPath + "?j_encoding=Shift_JIS" 
                                                + " HTTP/1.0\n").getBytes());
        os.write("\n".getBytes());
        
        InputStream is = s.getInputStream();
        BufferedReader bis = new BufferedReader(new InputStreamReader(is));
        String line = null;

        int index, lineNum=0;
        while ((line = bis.readLine()) != null) {
            index = line.indexOf("::");
            System.out.println(lineNum + ":  " + line);
            if (index != -1) {
                String status = line.substring(index+2);
                
                if (status.equalsIgnoreCase("PASS")){
                    stat.addStatus("web-multibyteValue GET: " 
                                        + line.substring(0,index), stat.PASS);
                } else {
                    stat.addStatus("web-multibyteValue GET: " 
                                        + line.substring(0,index), stat.FAIL);                       
                }
                count++;
            } 
            lineNum++;
        }

        /*
         * Send a POST request
         */
        // Construct body
        String body = URLEncoder.encode("j_encoding", "UTF-8")
            + "=" + URLEncoder.encode("Shift_JIS", "UTF-8");
    
        // Create a socket to the host
        s = new Socket(host, port);
        os = s.getOutputStream();
    
        // Send header
        BufferedWriter wr = new BufferedWriter(new OutputStreamWriter(
            s.getOutputStream(), "UTF8"));
        wr.write("POST " + contextPath + " HTTP/1.0\r\n");
        wr.write("Content-Length: " + body.length() + "\r\n");
        wr.write("Content-Type: application/x-www-form-urlencoded\r\n");
        wr.write("\r\n");
    
        // Send body
        wr.write(body);
        wr.flush();

        // Read response
        is = s.getInputStream();
        bis = new BufferedReader(new InputStreamReader(is));
        line = null;

        index=lineNum=0;
        while ((line = bis.readLine()) != null) {
            index = line.indexOf("::");
            System.out.println(lineNum + ":  " + line);
            if (index != -1) {
                String status = line.substring(index+2);
                
                if (status.equalsIgnoreCase("PASS")){
                    stat.addStatus("web-multibyteValue POST: " 
                                        + line.substring(0,index), stat.PASS);
                } else {
                    stat.addStatus("web-multibyteValue POST: " 
                                        + line.substring(0,index), stat.FAIL);                       
                }
                count++;
            } 
            lineNum++;
        }
   }
  
}
