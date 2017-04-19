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
import java.util.*;
import java.net.*;

import com.sun.ejte.ccl.reporter.*;
/**
 * Test login change session id in Servlet 3.0
 */
public class WebTest {
    
    private static final String TEST_NAME = "servlet-3.0-login-change-session-id";
    private static final String JSESSIONID = "JSESSIONID";
    private static final String EXPECTED_RESPONSE = "one";
    
    static SimpleReporterAdapter stat=
        new SimpleReporterAdapter("appserv-tests");

    public static void main(String args[]) {

        stat.addDescription("Change session id");

        String host = args[0];
        String portS = args[1];
        String contextRoot = args[2];

        int port = new Integer(portS).intValue();
        
        try {
            List<String> result = goGet(host, port, contextRoot + "/test?run=first",
                   null);
            List<String> result2 = null;
            if (result.size() > 0) {
                result2 = goGet(host, port, contextRoot + "/test?run=second",
                        result.get(0));
            } 
                
            stat.addStatus(TEST_NAME,
                    (result.size() > 1 && result2.size() > 1
                    && !result.get(0).equals(result2.get(0))
                    && EXPECTED_RESPONSE.equals(result2.get(1))) ? 
                    stat.PASS : stat.FAIL);
        } catch (Throwable t) {
            System.out.println(t.getMessage());
            stat.addStatus(TEST_NAME, stat.FAIL);
        }

        stat.printSummary();
    }

    private static List<String> goGet(String host, int port,
             String contextPath, String sessionId) throws Exception {
        List<String> result = new ArrayList<String>();
        Socket s = null;
        OutputStream os = null;
        InputStream is = null;
        BufferedReader bis = null;

        try{
            s = new Socket(host, port);
            os = s.getOutputStream();
            System.out.println(("GET " + contextPath + " HTTP/1.0\n"));
            os.write(("GET " + contextPath + " HTTP/1.0\n").getBytes());
            if (sessionId != null) {
                os.write(("Cookie: JSESSIONID=" + sessionId + "\n").getBytes());
            }
            os.write("\n".getBytes());

            is = s.getInputStream();
            bis = new BufferedReader(new InputStreamReader(is));
            String line = null;

            int lineNum=0;
            while ((line = bis.readLine()) != null) {
                System.out.println(lineNum + ":  " + line);
                int index = line.indexOf(JSESSIONID);
                if (index != -1) {
                    result.add(getSessionIdFromCookie(line));
                } else if (line.startsWith("A=")) {
                    result.add(line.substring(2));

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

            System.out.println("result= " + result);
            return result;
        }
    }

    private static String getSessionIdFromCookie(String cookie) {

        String ret = null;

        int index = cookie.indexOf(JSESSIONID + "=");
        if (index != -1) {
            int startIndex = index + JSESSIONID.length() + 1;
            int endIndex = cookie.indexOf(';', index);
            if (endIndex != -1) {
                ret = cookie.substring(startIndex, endIndex);
            } else {
                ret = cookie.substring(startIndex);
            }
            ret = ret.trim();
        }

        return ret;
    }
  
}
