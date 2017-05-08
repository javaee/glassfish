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

import java.io.*;
import java.net.*;
import com.sun.ejte.ccl.reporter.*;

/**
 * Unit test for alternate docroot support and welcome pages.
 * See
 *   https://glassfish.dev.java.net/issues/show_bug.cgi?id=6731
 *   ("how to protect files residing in docroot in Glassfish")
 * for details.
 *
 * This test configures the virtual server "server" with the following
 * alternate docroot properties:
 *
 *   <property
 *     name="alternatedocroot_1"
 *     value="from=/mytest dir=/tmp"/>
 *   <property
 *     name="alternatedocroot_2"
 *     value="from=/mytest/* dir=/tmp"/>
 *
 * and ensures that a request with a URI of the form /mytest will first be
 * redirected to "/mytest/", before the contents of the welcome page located
 * in "/tmp/mytest/index.jsp" in the local filesystem will be served.
 */
public class WebTest {

    private static SimpleReporterAdapter stat
        = new SimpleReporterAdapter("appserv-tests");

    private static final String TEST_NAME = "virtual-server-alternate-docroot-welcomepage-redirect";

    private String host;
    private String port;

    public WebTest(String[] args) {
        host = args[0];
        port = args[1];
    }
    
    public static void main(String[] args) {
        stat.addDescription("Unit test for alternate docroot support");
        WebTest webTest = new WebTest(args);
        webTest.doTest();
        stat.printSummary(TEST_NAME);
    }

    public void doTest() {     
        try { 
            invoke();
            stat.addStatus(TEST_NAME, stat.PASS);
        } catch (Exception ex) {
            System.out.println("Test failed. In order for this test " +
                "to pass, it is required that the web container has been " +
                "started prior to running this test, so that the dummy " +
                "web module of the virtual server can be configured with " +
                "the virtual server's alternate docroots");
            stat.addStatus(TEST_NAME, stat.FAIL);
            ex.printStackTrace();
        }
    }

    private void invoke() throws Exception {
        Socket sock = new Socket(host, new Integer(port).intValue());
        OutputStream os = sock.getOutputStream();
        String get = "GET /mytest HTTP/1.0\n";
        System.out.println(get);
        os.write(get.getBytes());
        os.write("\r\n".getBytes());

        InputStream is = sock.getInputStream();
        BufferedReader br = new BufferedReader(new InputStreamReader(is));
        String line = null;
        String location = null;
        while ((line = br.readLine()) != null) {
            System.out.println(line);
            if (line.startsWith("Location:")) {
                location = line;
            }
        }

        if (location == null) {
            throw new Exception("Missing Location response header");
        }

        String redirect = location.substring("Location:".length()).trim();
        followRedirect(new URL(redirect));
    }

    private void followRedirect(URL url) throws Exception {

        System.out.println("Connecting to: " + url.toString());
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.connect();

        int responseCode = conn.getResponseCode();
        if (responseCode != 200) {
            throw new Exception("Wrong response code. Expected: 200"
                                + ", received: " + responseCode);
        }

        InputStream is = conn.getInputStream();
        BufferedReader input = new BufferedReader(new InputStreamReader(is));
        String line = null;
        while ((line = input.readLine()) != null) {
            System.out.println(line);
            if (line.equals("HELLO WORLD")) {
                break;
            }
        }

        if (line == null) {
            throw new Exception("Missing or unexpected content for " + url);
        }
    }

}
