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

import java.io.*;
import java.net.*;
import com.sun.ejte.ccl.reporter.*;

/*
 * Unit test for IT 10100
 */
public class WebTest {

    private static final String TEST_NAME = "servlet-3.0-servlet-context-create-servlet-with-servlet-security";

    private static SimpleReporterAdapter stat
        = new SimpleReporterAdapter("appserv-tests");

    private String host;
    private int port;
    private String contextRoot;

    public WebTest(String[] args) {
        host = args[0];
        port = Integer.parseInt(args[1]);
        contextRoot = args[2];
    }
    
    public static void main(String[] args) {

        stat.addDescription("Unit test for setServletSecurity");
        WebTest webTest = new WebTest(args);

        try {
            boolean ok = webTest.run();
            stat.addStatus(TEST_NAME, ((ok)? stat.PASS : stat.FAIL));
        } catch( Exception ex) {
            ex.printStackTrace();
            stat.addStatus(TEST_NAME, stat.FAIL);
        }

	    stat.printSummary();
    }

    public boolean run() throws Exception {
        String contextPath = contextRoot + "/newServlet";
        boolean ok = doWebMethod("GET", host, port, contextPath, false, 200, "g:Hello");
        ok = ok && doWebMethod("POST", host, port, contextPath, true, 200, "p:Hello, javaee");
        ok = ok && doWebMethod("OPTIONS", host, port, contextPath, true, 403, null);

        String contextPath2 = contextRoot + "/newServlet2";
        ok = doWebMethod("GET", host, port, contextPath2, false, 200, "g2:Hello");
        ok = ok && doWebMethod("POST", host, port, contextPath2, true, 200, "p2:Hello, javaee");
        ok = ok && doWebMethod("OPTIONS", host, port, contextPath2, true, 403, null);

        String contextPath3 = contextRoot + "/newServlet2_1";
        ok = doWebMethod("GET", host, port, contextPath3, true, 403, null);
        ok = ok && doWebMethod("POST", host, port, contextPath3, true, 200, "p2:Hello, javaee");
        ok = ok && doWebMethod("OPTIONS", host, port, contextPath3, false, 200, "o2:Hello, null");

        return ok;
    }

    private boolean doWebMethod(String webMethod, String host, int port,
            String contextPath, boolean requireAuthenticate, 
            int responseCode, String expected) throws Exception {

        String urlStr = "http://" + host + ":" + port + contextPath;
        System.out.println(webMethod + " " + urlStr);
        URL url = new URL(urlStr);
        HttpURLConnection urlConnection = (HttpURLConnection)url.openConnection();
        urlConnection.setRequestMethod(webMethod);
        if (requireAuthenticate) {
            urlConnection.setRequestProperty("Authorization", "Basic amF2YWVlOmphdmFlZQ==");
        }
        urlConnection.connect();

        int code = urlConnection.getResponseCode();
        boolean ok = (code == responseCode);
        if (expected != null) {
            InputStream is = null;
            BufferedReader bis = null;
            String line = null;

            try{
                is = urlConnection.getInputStream();
                bis = new BufferedReader(new InputStreamReader(is));
                int lineNum = 1;
                while ((line = bis.readLine()) != null) {
                    System.out.println(lineNum + ":  " + line);
                    lineNum++;
                    ok = ok && expected.equals(line);
                }
            } catch( Exception ex){
                ex.printStackTrace();   
                throw new Exception("Test UNPREDICTED-FAILURE");
            } finally {
                try {
                    if (is != null) {
                        is.close();
                    }
                } catch(IOException ioe) {
                    // ignore
                }
                try {
                    if (bis != null) {
                        bis.close();
                    }
                } catch(IOException ioe) {
                    // ignore
                }
            }
        }

        return ok;
    }
}
