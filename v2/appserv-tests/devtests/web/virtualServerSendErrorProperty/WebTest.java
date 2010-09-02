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

import java.io.*;
import java.net.*;
import com.sun.ejte.ccl.reporter.*;

/**
 * Unit test for 6324911 ("can not migrate all virtual server functions
 * from 7.1 to 8.1. (eg: custom error page)").
 *
 * The supporting build.xml assigns the following property to the virtual
 * server named "server":
 *
 *   send-error="path=default-web.xml reason=MY404 code=404"
 *
 * As a result of this setting, any 404 response must have a reason string of
 * MY404, and must provide the contents of the
 * domains/domain1/config/default-web.xml file in its body.
 *
 * The code below does not check the entire response body. Instead, it only
 * checks for the presence of a line that starts with "<web-app xmlns=",
 * which is contained in default-web.xml, and uses its presence as an
 * indication that the response contains the expected body.
 */
public class WebTest {

    private static SimpleReporterAdapter stat =
        new SimpleReporterAdapter("appserv-tests");

    private static final String TEST_NAME =
        "virtual-server-send-error-property";

    private String host;
    private String port;
    private String contextRoot;

    public WebTest(String[] args) {
        host = args[0];
        port = args[1];
        contextRoot = args[2];
    }
    
    public static void main(String[] args) {
        stat.addDescription("Unit test for 6324911");
        WebTest webTest = new WebTest(args);
        try {
            webTest.doTest("/nonexistent", "HTTP/1.1 404 MY404", "MY404");
            webTest.doTest(webTest.contextRoot + "/test500?sendError=true",
                "HTTP/1.1 500 MY500", "<web-app xmlns=");
            webTest.doTest(webTest.contextRoot + "/test500?sendError=false",
                "HTTP/1.1 500 MY500", "<web-app xmlns=");
            stat.addStatus(TEST_NAME, stat.PASS);
        } catch (Exception ex) {
            stat.addStatus(TEST_NAME, stat.FAIL);
            ex.printStackTrace();
        }

        stat.printSummary(TEST_NAME);
    }

    private void doTest(String target, String status, String body)
            throws Exception {
        Socket sock = new Socket(host, new Integer(port).intValue());
        OutputStream os = sock.getOutputStream();
        String get = "GET " + target + " HTTP/1.0\n";
        System.out.println(get);
        os.write(get.getBytes());
        os.write("\n".getBytes());

        boolean statusHeaderFound = false;
        boolean bodyLineFound = false;        
        BufferedReader br = null;
        try {
            InputStream is = sock.getInputStream();
            br = new BufferedReader(new InputStreamReader(is));
            String line = null;
            while ((line = br.readLine()) != null) {
                System.out.println(line);
                if (line.startsWith(status)) {
                    statusHeaderFound = true;
                }
                if (line.contains(body)) {
                    bodyLineFound = true;
                }
                if (statusHeaderFound && bodyLineFound) {
                    break;
                }
            }
        } finally {
            if (br != null) {
                try {
                    br.close();
                } catch (IOException ioe) {
                    // Ignore
                }
            }
        }

        if (!statusHeaderFound || !bodyLineFound) {
            throw new Exception("Missing response status or body line");
        }
    }
}
