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
 * Unit test for 6188932 ("AS 8.1 does not support auth-passthrough
 * (Web Server 6.1 Add-On)").
 */
public class WebTest {

    private static SimpleReporterAdapter stat
        = new SimpleReporterAdapter("appserv-tests");

    private static final String TEST_NAME = "proxy-auth-cert";

    private static final String EXPECTED_RESPONSE = "true";

    private static final String CLIENT_CERT =
        "MIIClDCCAf0CBEJ2pAowDQYJKoZIhvcNAQEEBQAwgZ"
        + "AxCzAJBgNVBAYTAlVTMRMwEQYDVQQIEwpD% d% aYW"
        + "xpZm9ybmlhMRQwEgYDVQQHEwtTYW50YSBDbGFyYTEZ"
        + "MBcGA1UEChMQU3VuIE1pY3Jvc3lzdGVt% d% aczEr"
        + "MCkGA1UECxMiU3VuIEphdmEgU3lzdGVtIEFwcGxpY2"
        + "F0aW9uIFNlcnZlcjEOMAwGA1UEAxMF% d% acmFnYT"
        + "IwHhcNMDUwNTAyMjIwNDU4WhcNMTUwNDMwMjIwNDU4"
        + "WjCBkDELMAkGA1UEBhMCVVMxEzAR% d% aBgNVBAgT"
        + "CkNhbGlmb3JuaWExFDASBgNVBAcTC1NhbnRhIENsYX"
        + "JhMRkwFwYDVQQKExBTdW4gTWlj% d% acm9zeXN0ZW"
        + "1zMSswKQYDVQQLEyJTdW4gSmF2YSBTeXN0ZW0gQXBw"
        + "bGljYXRpb24gU2VydmVyMQ4w% d% aDAYDVQQDEwVy"
        + "YWdhMjCBnzANBgkqhkiG9w0BAQEFAAOBjQAwgYkCgY"
        + "EApQp/04Zgq623FgQaj+ot% d% a2XIuD1WL2T4kWs"
        + "stpY+zGGnUxV/DdplpV/WGEP8Uqx3gm9Xxp41YYYcF"
        + "vV3JM0xnkmRNcKMnLDyQ% d% aSwBlyrVphjKN9DBa"
        + "vXvDCAKvB/jW7suvgK11OcG6WT3oFBT/r7R9xwXbuv"
        + "NGpzOM7INfNPPuF8UC% d% aAwEAATANBgkqhkiG9w"
        + "0BAQQFAAOBgQCK/IyXSWHRahmB4xriycA2oMkopKu6"
        + "FFC1u+GAztFCef8ARvg4SFWLZWYRPzBMngYII"
        + "Pd6oG42jWfhDaRQ4WGs6/fvGJ/uVyeGrr8N9Z/4lwt"
        + "hpD40e+W8Ny44% d% atakRqCdbVuof6ms/8p0UxQm"
        + "Kt9v+BmJJog8bOm6t8tQ7Bx24Xw==";

    private String host;
    private String port;
    private String contextRoot;
    private Socket socket = null;

    public WebTest(String[] args) {
        host = args[0];
        port = args[1];
        contextRoot = args[2];
    }
    
    public static void main(String[] args) {
        stat.addDescription("Unit test for 6188932");
        WebTest webTest = new WebTest(args);
        webTest.doTest();
        stat.printSummary(TEST_NAME);
    }

    public void doTest() {
     
        try { 
            invoke();
            stat.addStatus(TEST_NAME, stat.PASS);
        } catch (Exception ex) {
            System.out.println(TEST_NAME + " test failed");
            ex.printStackTrace();
            stat.addStatus(TEST_NAME, stat.FAIL);
        } finally {
            try {
                if (socket != null) {
                    socket.close();
                }
            } catch (IOException ioe) {
                // ignore
            }
        }
    }

    private void invoke() throws Exception {

        socket = new Socket(host, new Integer(port).intValue());
        OutputStream os = socket.getOutputStream();

        os.write(("GET " + contextRoot + "/TestServlet HTTP/1.0\n").getBytes());
        os.write(("Proxy-auth-cert: " + CLIENT_CERT + "\n").getBytes());
        os.write("\n".getBytes());

        InputStream is = null;
        BufferedReader bis = null;
        String line = null;
        String lastLine = null;
        try {
            is = socket.getInputStream();
            bis = new BufferedReader(new InputStreamReader(is));
            while ((line = bis.readLine()) != null) {
                lastLine = line;
            }
        } finally {
            try {
                if (is != null) {
                    is.close();
                }
            } catch (IOException ioe) {
                // ignore
            }
            try {
                if (bis != null) {
                    bis.close();
                }
            } catch (IOException ioe) {
                // ignore
            }
        }

        if (!EXPECTED_RESPONSE.equals(lastLine)) {
            throw new Exception("Wrong response. Expected: " +
                                EXPECTED_RESPONSE + ", received: " +
                                lastLine);
        }
    }
}
