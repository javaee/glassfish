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

/**
 * Unit test to ensure that the state of a virtual server may be
 * changed without requiring a server restart.
 *
 * This test:
 *
 * 1. Creates a virtual-server myvs and deploys a webapp to it
 *
 * 2. Makes sure that the webapp may be accessed
 *
 * 3. Disables the virtual server and ensures that a 403 response is
 *    returned when the webapp is accessed
 *
 * 4. Turns off the virtual server and ensures that a 404 response is
 *    returned when the webapp is accessed
 *
 * 5. Re-enables the virtual server and makes sure that the webapp may be
 *    accessed again.
 */
public class WebTest {

    private static SimpleReporterAdapter stat
        = new SimpleReporterAdapter("appserv-tests");

    private static String TEST_NAME = null;

    private static final String TEST_ROOT_NAME
        = "virtual-server-state-dynamic-reconfig";

    private static final String ON_RESPONSE = "Success!";

    private static final String DISABLED_RESPONSE
        = "HTTP/1.1 403 Virtual server myvs has been disabled";

    private static final String OFF_RESPONSE
        = "HTTP/1.1 404 Virtual server myvs has been turned off";

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
        stat.addDescription("Unit test for disabling a virtual server");
        WebTest webTest = new WebTest(args);

        try { 
            if ("on".equals(webTest.run)) {
                TEST_NAME = TEST_ROOT_NAME + "-on";
                webTest.onRun();
            } else if ("disabled".equals(webTest.run)) {
                TEST_NAME = TEST_ROOT_NAME + "-disabled";
                webTest.disabledRun();
            } else if ("off".equals(webTest.run)) {
                TEST_NAME = TEST_ROOT_NAME + "-off";
                webTest.offRun();
            }
            stat.addStatus(TEST_NAME, stat.PASS);
        } catch (Exception ex) {
            stat.addStatus(TEST_NAME, stat.FAIL);
            ex.printStackTrace();
        }

        stat.printSummary(TEST_NAME);
    }

    /**
     * Make sure the webapp may be accessed on the newly created virtual
     * server.
     */
    private void onRun() throws Exception {
        
        Socket sock = new Socket(host, new Integer(port).intValue());
        OutputStream os = sock.getOutputStream();
        String get = "GET " + contextRoot + "/test.txt HTTP/1.1\n";
        System.out.println(get);
        os.write(get.getBytes());
        os.write("Host: myvs\n".getBytes());
        os.write("Connnection: Close\n".getBytes());
        os.write("\n".getBytes());

        InputStream is = sock.getInputStream();
        BufferedReader bis = new BufferedReader(new InputStreamReader(is));

        String line = null;
        while ((line = bis.readLine()) != null) {
            System.out.println(line);
            if (line.equals(ON_RESPONSE)) {
                break;
            }
        }

        if (line == null) {
            throw new Exception("Missing expected response: " +
                                ON_RESPONSE);
        }
    }

    /**
     * Make sure that a 403 response is returned when trying to access the
     * webapp after the virtual server on which it is deployed has been
     * disabled.
     */
    private void disabledRun() throws Exception {

        Socket sock = new Socket(host, new Integer(port).intValue());
        OutputStream os = sock.getOutputStream();
        String get = "GET " + contextRoot + "/test.txt HTTP/1.1\n";
        System.out.println(get);
        os.write(get.getBytes());
        os.write("Host: myvs\n".getBytes());
        os.write("Connnection: Close\n".getBytes());
        os.write("\n".getBytes());

        InputStream is = sock.getInputStream();
        BufferedReader bis = new BufferedReader(new InputStreamReader(is));

        String line = null;
        while ((line = bis.readLine()) != null) {
            System.out.println(line);
            if (line.contains(DISABLED_RESPONSE)) {
                break;
            }
        }

        if (line == null) {
            throw new Exception("Missing expected response: " +
                                DISABLED_RESPONSE);
        }
    }

    /**
     * Make sure that a 404 response is returned when trying to access the
     * webapp after the virtual server on which it is deployed has been
     * turned off.
     */
    private void offRun() throws Exception {

        Socket sock = new Socket(host, new Integer(port).intValue());
        OutputStream os = sock.getOutputStream();
        String get = "GET " + contextRoot + "/test.txt HTTP/1.1\n";
        System.out.println(get);
        os.write(get.getBytes());
        os.write("Host: myvs\n".getBytes());
        os.write("Connnection: Close\n".getBytes());
        os.write("\n".getBytes());

        InputStream is = sock.getInputStream();
        BufferedReader bis = new BufferedReader(new InputStreamReader(is));

        String line = null;
        while ((line = bis.readLine()) != null) {
            System.out.println(line);
            if (line.contains(OFF_RESPONSE)) {
                break;
            }
        }

        if (line == null) {
            throw new Exception("Missing expected response: " +
                                OFF_RESPONSE);
        }
    }

}
