/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 1997-2010 Oracle and/or its affiliates. All rights reserved.
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

public class WebTest
{
    
    private static boolean pass = false;
    private static int bTag = 0;
    private static int useBean = 0;
    private static int scriptlet = 0;
    private static int ELvalue1 = 0;
    private static int ELvalue2 = 0;
    
    static SimpleReporterAdapter stat=
        new SimpleReporterAdapter("appserv-tests");

    public static void main(String args[])
    {

        // The stat reporter writes out the test info and results
        // into the top-level quicklook directory during a run.
      
        stat.addDescription("Test tagdependent bodies are handled correctly in XML syntax");

        String host = args[0];
        String portS = args[1];
        String contextRoot = args[2];
        int port = new Integer(portS).intValue();
        
        try {
            goGet(host, port, contextRoot + "/test.jspx" );
            if (bTag == 6 && useBean == 1 && scriptlet == 1 &&
                ELvalue1 == 1 && ELvalue2 == 1) {
                stat.addStatus("XMLtagdependent test", stat.PASS);
            } else {
                stat.addStatus("XMLtagdependent test", stat.FAIL);
            }
        } catch (Throwable t) {
            System.out.println(t.getMessage());
            stat.addStatus("Test UNPREDICTED-FAILURE", stat.FAIL);
        }

        stat.printSummary("Tagdependent body in XML syntax");
    }

    private static void goGet(String host, int port, String contextPath)
         throws Exception
    {
        Socket sock = null;
        OutputStream os = null;
        InputStream is = null;
        BufferedReader bis = null;
        try {
            sock = new Socket(host, port);
            os = sock.getOutputStream();

            System.out.println("GET " + contextPath + " HTTP/1.0");
            os.write(("GET " + contextPath + " HTTP/1.0\n").getBytes());
            os.write("\n".getBytes());

            is = sock.getInputStream();
            bis = new BufferedReader(new InputStreamReader(is));
            String line = null;

            while ((line = bis.readLine()) != null) {
                if (line.trim().length() > 0)
                    System.out.println(line);
                if (line.indexOf("ELExpression") >=0 ) {
                    if (line.indexOf("${") < 0)
                        stat.addStatus("EL expression", stat.FAIL);
                }
                else if (line.indexOf("ELvalue1") >=0 ) {
                    ELvalue1++;
                    if (line.indexOf("15") < 0)
                        stat.addStatus("ELvalue1", stat.FAIL);
                }
                else if (line.indexOf("ELvalue2") >=0 ) {
                    ELvalue2++;
                    if (line.indexOf("18") < 0)
                        stat.addStatus("ELvalue2", stat.FAIL);
                }
                else if (line.indexOf("<el:dependent") >= 0) {
                    bTag++;
                }
                else if (line.indexOf("<jsp:useBean") >= 0) {
                    useBean++;
                }
                else if (line.indexOf("<jsp:scriptlet") >= 0) {
                    scriptlet++;
                }
            }
        } catch( Exception ex){
            ex.printStackTrace();   
            throw new Exception("Test UNPREDICTED-FAILURE");
        } finally {
            try {
                if (os != null) os.close();
            } catch (IOException ex) {}
            try {
                if (is != null) is.close();
            } catch (IOException ex) {}
            try {
                if (sock != null) sock.close();
            } catch (IOException ex) {}
            try {
                if (bis != null) bis.close();
            } catch (IOException ex) {}
        }
    }
}
