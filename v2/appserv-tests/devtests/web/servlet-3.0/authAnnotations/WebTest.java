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
import java.io.*;
import java.net.*;
import com.sun.ejte.ccl.reporter.*;

/*
 * Unit test for @RolesAllowed, @DenyAll, @PermitAll
 */
public class WebTest {

    private static final String TEST_NAME = "auth-annotations";

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

        stat.addDescription("Unit test for @RolesAllowed, @DenyAll, @PermitAll");
        WebTest webTest = new WebTest(args);

        try {
            webTest.run();
        } catch( Exception ex) {
            ex.printStackTrace();
            stat.addStatus(TEST_NAME, stat.FAIL);
        }

	stat.printSummary();
    }

    public void run() throws Exception {
        String contextPath = contextRoot + "/myurl";
        doWebMethod("GET", host, port, contextPath, false, "@PermitAll", 200, "g:Hello");
        doWebMethod("POST", host, port, contextPath, true, "@RolesAllowed", 200, "p:Hello, javaee");
        doWebMethod("TRACE", host, port, contextPath, true, "@DenyAll", 403, null);

        contextPath = contextRoot + "/myurl2";
        doWebMethod("GET", host, port, contextPath, true, "Class-@RolesAllowed", 200, "g:Hello, javaee");
        doWebMethod("POST", host, port, contextPath, true, "Class-@RolesAllowed-Methad-override-@RolesAllowed", 403, null);
        doWebMethod("TRACE", host, port, contextPath, false, "Class-@RolesAllowed-Methad-override-@PermitAll", 200, "t:Hello");
        doWebMethod("PUT", host, port, contextPath, true, "Class-@RolesAllowed-Methad-override-@DenyAll", 403, null);

        contextPath = contextRoot + "/myurl3";
        doWebMethod("GET", host, port, contextPath, false, "Class-@DenyAll", 403, null);
    }

    private static void doWebMethod(String webMethod, String host, int port,
            String contextPath, boolean requireAuthenticate, String testSuffix,
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

        stat.addStatus(TEST_NAME + ":" + testSuffix, ((ok)? stat.PASS : stat.FAIL));
    }
}
