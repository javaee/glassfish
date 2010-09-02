/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2010 Oracle and/or its affiliates. All rights reserved.
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
    private static String EXPECTED_RESPONSE = "PASS PASS SCInit-OK";
    static SimpleReporterAdapter stat=
        new SimpleReporterAdapter("appserv-tests");

    public static void main(String args[]) {
      
        stat.addDescription("IT 11802: exploded war support");

        String host = args[0];
        String portS = args[1];
        String contextRoot = args[2];

        int port = new Integer(portS).intValue();
        String name;
        
        try {
            goGet(host, port, contextRoot + "/ServletTest" );
        } catch (Throwable t) {
            t.printStackTrace();
        }

        stat.printSummary("exploded-war");
    }

    private static void goGet(String host, int port,
                              String contextPath) throws Exception {
        Socket s = null;
        OutputStream os = null;
        InputStream is = null;
        BufferedReader bis = null;
        try{
            long time = System.currentTimeMillis();
            s = new Socket(host, port);
            os = s.getOutputStream();

            String getString = "GET " + contextPath + " HTTP/1.0\n";
            System.out.println(getString);
            os.write(getString.getBytes());
            os.write("\n".getBytes());
            
            is = s.getInputStream();
            bis = new BufferedReader(new InputStreamReader(is));
            String line = null;

            boolean expected = false;
            while ((line = bis.readLine()) != null) {
                System.out.println(line);
                if (EXPECTED_RESPONSE.equals(line)) {
                    expected = true;
                }
            }
            stat.addStatus("exploded-war", expected ? stat.PASS : stat.FAIL);
        } catch( Exception ex){
            ex.printStackTrace();
        } finally {
            if (bis != null) {
                bis.close();
            }
            if (is != null) {
                is.close();
            }
            if (os != null) {
                os.close();
            }
            if (s != null) {
                s.close();
            }
        }
   }
  
}
