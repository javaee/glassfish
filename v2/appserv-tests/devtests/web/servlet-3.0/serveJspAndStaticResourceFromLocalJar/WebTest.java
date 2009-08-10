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
 * Unit test for serving JSP and static resources from
 * WEB-INF/lib/[*.jar]/META-INF/resources
 *
 * In this unit test, the client makes a request for
 *   http://localhost:8080/abc.jsp
 * and
 *   http://localhost:8080/abc.txt
 * and the requested resource is supposed to be served from
 *   WEB-INF/lib/nested.jar!META-INF/resources/abc.jsp
 * (by the JspServlet) and
 *   WEB-INF/lib/nested.jar!META-INF/resources/abc.txt
 * (by the DefaultServlet), respectively.
 */
public class WebTest {

    private static SimpleReporterAdapter stat =
        new SimpleReporterAdapter("appserv-tests");
    private static final String TEST_NAME =
        "serve-jsp-and-static-resource-from-local-jar";
    private static final String EXPECTED_RESPONSE = "Hello World!";

    private String host;
    private String port;
    private String contextRoot;

    public WebTest(String[] args) {
        host = args[0];
        port = args[1];
        contextRoot = args[2];
    }
    
    public static void main(String[] args) {
        stat.addDescription("Unit test for serving JSP and static " +
                            "resources from JAR inside WEB-INF/lib");
        new WebTest(args).doTest();
        stat.printSummary(TEST_NAME);
    }

    public void doTest() {
        try { 
            invokeJspServlet();
            invokeDefaultServlet();
            stat.addStatus(TEST_NAME, stat.PASS);
        } catch (Exception ex) {
            stat.addStatus(TEST_NAME, stat.FAIL);
            ex.printStackTrace();
        }
    }

    private void invokeJspServlet() throws Exception {
        URL url = new URL("http://" + host  + ":" +
                          port + contextRoot + "/abc.jsp");
        System.out.println("Invoking URL: " + url.toString());
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.connect();
        int responseCode = conn.getResponseCode();
        if (responseCode != 200) {
            throw new Exception("Unexpected response code: " + responseCode);
        }
        InputStream is = conn.getInputStream();
        BufferedReader input = new BufferedReader(new InputStreamReader(is));
        String response = input.readLine();
        if (!EXPECTED_RESPONSE.equals(response)) {
            throw new Exception("Wrong response, expected: " +
                EXPECTED_RESPONSE + ", received: " + response);
        }
    }

    private void invokeDefaultServlet() throws Exception {
        URL url = new URL("http://" + host  + ":" +
                          port + contextRoot + "/abc.txt");
        System.out.println("Invoking URL: " + url.toString());
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.connect();
        int responseCode = conn.getResponseCode();
        if (responseCode != 200) {
            throw new Exception("Unexpected response code: " + responseCode);
        }
        InputStream is = conn.getInputStream();
        BufferedReader input = new BufferedReader(new InputStreamReader(is));
        String response = input.readLine();
        if (!EXPECTED_RESPONSE.equals(response)) {
            throw new Exception("Wrong response, expected: " +
                EXPECTED_RESPONSE + ", received: " + response);
        }
    }
}
