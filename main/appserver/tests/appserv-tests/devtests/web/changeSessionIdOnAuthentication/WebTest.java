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

import java.lang.*;
import java.io.*;
import java.net.*;

import com.sun.ejte.ccl.reporter.*;
/**
 * Fix for CR 6947296
 */
public class WebTest {
    
    private static final String TEST_NAME = "change-session-id-on-authentication";
    private static int EXPECTED_COUNT = 1;
    
    static SimpleReporterAdapter stat=
        new SimpleReporterAdapter("appserv-tests");

    public static void main(String args[]) {

        stat.addDescription("Change session id on authentication");

        String host = args[0];
        String portS = args[1];
        String contextRoot = args[2];

        int port = new Integer(portS).intValue();
        
        try {
            String result = goGet(host, port, contextRoot + "/welcome.jsp",
                   null, false);
            String result2 = null;
            if (result != null && result.length() > 0) {
                result2 = goGet(host, port, contextRoot + "/secure.jsp",
                        result, true);
            } 
                
            stat.addStatus(TEST_NAME,
                    (result != null && result.length() > 0 &&
                    result2 != null && result2.length() > 0 &&
                    !result.equals(result2)) ? 
                    stat.PASS : stat.FAIL);
        } catch (Throwable t) {
            System.out.println(t.getMessage());
            stat.addStatus(TEST_NAME, stat.FAIL);
        }

        stat.printSummary();
    }

    private static String goGet(String host, int port,
             String contextPath, String sessionId, boolean requireAuthentication) throws Exception {
        String result = null;
        Socket s = null;
        OutputStream os = null;
        InputStream is = null;
        BufferedReader bis = null;

        try{
            s = new Socket(host, port);
            os = s.getOutputStream();
            System.out.println(("GET " + contextPath + " HTTP/1.0\n"));
            os.write(("GET " + contextPath + " HTTP/1.0\n").getBytes());
            if (requireAuthentication) {
                os.write("Authorization: Basic amF2YWVlOmphdmFlZQ==\n".getBytes());
            }
            if (sessionId != null) {
                os.write(("Cookie: JSESSIONID=" + sessionId + "\n").getBytes());
            }
            os.write("\n".getBytes());

            is = s.getInputStream();
            bis = new BufferedReader(new InputStreamReader(is));
            String line = null;

            int lineNum=0;
            while ((line = bis.readLine()) != null) {
                int index = line.indexOf("sessionId=");
                System.out.println(lineNum + ":  " + line);
                if (index != -1) {
                     result = line.substring(index + 10);
                }
                lineNum++;
            }
        } catch( Exception ex){
            ex.printStackTrace();   
            throw new Exception("Test UNPREDICTED-FAILURE");
         } finally {
            try {
                if (os != null) os.close();
            } catch (IOException ex) {}
            try {
                if (s != null) s.close();
            } catch (IOException ex) {}
            try {
                if (is != null) is.close();
            } catch (IOException ex) {}
            try {
                if (bis != null) bis.close();
            } catch (IOException ex) {}

            System.out.println("session id = " + result);
            return result;
        }

   }
  
}
