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
 * Unit test for 6712778 ("Virtual Server's "hosts" parameter cannot be
 * dynamically changed").
 *
 * This unit test creates a virtual server with a "hosts" attribute equal to
 * "myhost", deploys a webapp to it, and accesses the webapp by specifying
 * a Host header equal to "myhost" in the request.
 *
 * The test then updates the virtual server's "hosts" attribute to 
 * "mynewhost", and ensures that the webapp may still be accessed when
 * specifying a Host header equal to "mynewhost" in the request.
 */
public class WebTest {

    private static SimpleReporterAdapter stat
        = new SimpleReporterAdapter("appserv-tests");

    private static String TEST_NAME;
    private static final String TEST_ROOT_NAME
        = "virtual-server-update-hosts-dynamic-reconfig";

    private static final String EXPECTED = "Success!";

    private String host;
    private String port;
    private String contextRoot;
    private String run;

    public WebTest(String[] args) {
        host = args[0];
        port = args[1];
        contextRoot = args[2];
        run = args[3];
    }
    
    public static void main(String[] args) {

        stat.addDescription("Unit test for 6712778");

        WebTest webTest = new WebTest(args);

        try {
            if ("first".equals(webTest.run)) {
                TEST_NAME = TEST_ROOT_NAME + "-first";
                webTest.firstRun();
            } else {
                TEST_NAME = TEST_ROOT_NAME + "-second";
                webTest.secondRun();
            }
        } catch( Exception ex) {
            ex.printStackTrace();
            stat.addStatus(TEST_NAME, stat.FAIL);
        }

        stat.printSummary(TEST_NAME);
    }

    /**
     * Make sure that request with a Host header value equal to "myhost"
     * is mapped to the virtual server.
     */
    private void firstRun() throws Exception {
        
        Socket sock = new Socket(host, new Integer(port).intValue());
        OutputStream os = sock.getOutputStream();
        String get = "GET " + contextRoot + "/test.txt HTTP/1.1\n";
        System.out.println(get);
        os.write(get.getBytes());
        String host = "Host: myhost\n";
        System.out.println(host);
        os.write(host.getBytes());
        os.write("\n".getBytes());

        InputStream is = sock.getInputStream();
        BufferedReader bis = new BufferedReader(new InputStreamReader(is));

        String line = null;
        while ((line = bis.readLine()) != null) {
            System.out.println(line);
            if (line.equals(EXPECTED)) {
                break;
            }
        }

        if (line != null) {
            stat.addStatus(TEST_NAME, stat.PASS);
        } else {
            System.err.println("Missing expected response: " + EXPECTED);
            stat.addStatus(TEST_NAME, stat.FAIL);
        }
    }

    /**
     * Make sure that request with a Host header value equal to "mynewhost"
     * is mapped to the updated virtual server.
     */
    private void secondRun() throws Exception {
        
        Socket sock = new Socket(host, new Integer(port).intValue());
        OutputStream os = sock.getOutputStream();
        String get = "GET " + contextRoot + "/test.txt HTTP/1.1\n";
        System.out.println(get);
        os.write(get.getBytes());
        String host = "Host: mynewhost\n";
        System.out.println(host);
        os.write(host.getBytes());
        os.write("\n".getBytes());

        InputStream is = sock.getInputStream();
        BufferedReader bis = new BufferedReader(new InputStreamReader(is));

        String line = null;
        while ((line = bis.readLine()) != null) {
            System.out.println(line);
            if (line.equals(EXPECTED)) {
                break;
            }
        }

        if (line != null) {
            stat.addStatus(TEST_NAME, stat.PASS);
        } else {
            System.err.println("Missing expected response: " + EXPECTED);
            stat.addStatus(TEST_NAME, stat.FAIL);
        }
    }
}
