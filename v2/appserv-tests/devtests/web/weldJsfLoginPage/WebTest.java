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
import java.security.*;
import javax.net.ssl.*;
import com.sun.ejte.ccl.reporter.*;

/*
 * Unit test for https://glassfish.dev.java.net/issues/show_bug.cgi?id=11504
 * ("Glassfishv3 j_security_check causes No active contexts errors")
 */
public class WebTest {

    private static final String TEST_NAME = "weld-jsf-form-login-page";
    private static final SimpleReporterAdapter stat =
        new SimpleReporterAdapter("appserv-tests");

    private String host;
    private String httpPort;
    private String httpsPort;
    private String contextRoot;
    private String keyStorePath;
    private String trustStorePath;

    public WebTest(String[] args) {
        host = args[0];
        httpPort = args[1];
        httpsPort = args[2];
        contextRoot = args[3];
        keyStorePath = args[4];
        trustStorePath = args[5];
    }
    
    public static void main(String[] args) {
        stat.addDescription("Unit test for IT 11504");
        WebTest webTest = new WebTest(args);
        try {
            webTest.run();
            stat.addStatus(TEST_NAME, stat.PASS);
        } catch( Exception ex) {
            ex.printStackTrace();
            stat.addStatus(TEST_NAME, stat.FAIL);
        }

        stat.printSummary();
    }

    /*
     * Attempts to access resource protected by FORM based login.
     */
    private void run() throws Exception {
        URL url = new URL("http://" + host  + ":" + httpPort + contextRoot +
                "/protected.txt");
        System.out.println("Connecting to " + url.toString());
        URLConnection conn = url.openConnection();
        String redirectLocation = conn.getHeaderField("Location");
        System.out.println("Location: " + redirectLocation);
        
        String expectedRedirectLocation = "https://" + host + ":" + httpsPort +
                contextRoot + "/protected.txt";
        if (!expectedRedirectLocation.equals(redirectLocation)) {
            throw new Exception("Unexpected redirect location");
        }

        SSLSocketFactory ssf = getSSLSocketFactory(keyStorePath, trustStorePath);
        System.out.println("Connecting to " + redirectLocation);
        HttpsURLConnection connection = connect(redirectLocation, ssf);
        verifyResponse(connection);
    }

    private void verifyResponse(HttpsURLConnection connection)
            throws Exception {
        BufferedReader in = null;
        try {
            in = new BufferedReader(new InputStreamReader(
                    connection.getInputStream()));
            String line = null;
            boolean found = false;
            while ((line = in.readLine()) != null) {
                System.out.println(line);
                if (line.indexOf("j_security_check") != -1) {
                    found = true;
                }
            }
            if (!found) {
                throw new Exception("Expected j_security_check ACTION " +
                        "not found in response");
            }
        } finally {
            if (in != null) {
                in.close();
            }
        }
    }

    private SSLSocketFactory getSSLSocketFactory(String keyStorePath,
            String trustStorePath) throws Exception {
        SSLContext ctx = SSLContext.getInstance("TLS");

        // Keystore 
        KeyStore ks = KeyStore.getInstance("JKS");
        char[] passphrase = "changeit".toCharArray();
        ks.load(new FileInputStream(keyStorePath), passphrase);
        KeyManagerFactory kmf = KeyManagerFactory.getInstance(
                KeyManagerFactory.getDefaultAlgorithm());
        kmf.init(ks, passphrase);

        // Truststore
        KeyStore trustStore = KeyStore.getInstance("JKS");
        trustStore.load(new FileInputStream(trustStorePath), null);
        TrustManagerFactory tmf = TrustManagerFactory.getInstance(
                TrustManagerFactory.getDefaultAlgorithm());
        tmf.init(trustStore);

        ctx.init(kmf.getKeyManagers(),tmf.getTrustManagers(), null);
 
        return ctx.getSocketFactory();
    }


    private HttpsURLConnection connect(String urlAddress,
            SSLSocketFactory ssf) throws Exception {
        URL url = new URL(urlAddress);
        HttpsURLConnection.setDefaultSSLSocketFactory(ssf);
        HttpsURLConnection connection = (HttpsURLConnection)
            url.openConnection();
        connection.setHostnameVerifier(
            new HostnameVerifier() {
                public boolean verify(String rserver, SSLSession sses) {
                    return true;
                }
        });

        return connection;
    }
}

