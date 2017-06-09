/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2017 Oracle and/or its affiliates. All rights reserved.
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

package devtests.security;

import java.io.*;
import java.util.*;
import java.security.*;
import java.net.*;
import javax.net.ssl.*;
import com.sun.ejte.ccl.reporter.*;

/*
   This is the standalone client java program to access AS web app
   which has <security-constraint> protected by (in its web.xml)
   <login-config>
     <auth-method>CLIENT-CERT</auth-method>
     <realm-name>default</realm-name>
   </login-config>
*/
public class WebSSLClient {

    private static final String TEST_NAME
        = "security-cert-realm-custom-loginmodule";    

    private static final String EXPECTED_RESPONSE
        = "This is CN=SSLTest, OU=Sun Java System Application Server, O=Sun Microsystems, L=Santa Clara, ST=California, C=US from index.jsp";

    private static SimpleReporterAdapter stat
        = new SimpleReporterAdapter("appserv-tests");


    public static void main(String args[]) throws Exception{

        String host = args[0];
        String port = args[1];
        String contextRoot = args[2];
        String keyStorePath = args[3];
        String trustStorePath = args[4];
        String sslPassword = args[5];

        System.out.println("host/port=" + host + "/" + port);
        
        try {
            stat.addDescription(TEST_NAME);
            SSLSocketFactory ssf = getSSLSocketFactory(sslPassword,
                                                       keyStorePath,
                                                       trustStorePath);
            HttpsURLConnection connection = connect("https://" + host  + ":"
                                                    + port + contextRoot
                                                    + "/index.jsp",
                                                    ssf);
            
            parseResponse(connection);
            
        } catch (Throwable t) {
            stat.addStatus(TEST_NAME, stat.FAIL);
            t.printStackTrace();
        }
        stat.printSummary(TEST_NAME);
    }


    private static void parseResponse(HttpsURLConnection connection)
            throws Exception {

        BufferedReader in = null;

        try {
            in = new BufferedReader(new InputStreamReader(
                            connection.getInputStream()));
            
            String line = null;
            while ((line = in.readLine()) != null) {
                if (EXPECTED_RESPONSE.equals(line)) {
                    stat.addStatus(TEST_NAME, stat.PASS);
                    break;
                }
            }

            if (line == null) {
                System.err.println("Wrong response. Expected: "
                                   + EXPECTED_RESPONSE
                                   + ", received: " + line);
                stat.addStatus(TEST_NAME, stat.FAIL);
            }
        } finally {
            if (in != null) {
                in.close();
            }
        }
    }


    private static SSLSocketFactory getSSLSocketFactory(String sslPassword,
                                                        String keyStorePath,
                                                        String trustStorePath)
            throws Exception {

        SSLContext ctx = SSLContext.getInstance("TLS");

        // Keystore 
        KeyStore ks = KeyStore.getInstance("JKS");
        char[] passphrase = sslPassword.toCharArray();
        ks.load(new FileInputStream(keyStorePath), passphrase);
        KeyManagerFactory kmf = KeyManagerFactory.getInstance("SunX509");
        kmf.init(ks, passphrase);

        // Truststore
        KeyStore trustStore = KeyStore.getInstance("JKS");
        trustStore.load(new FileInputStream(trustStorePath), null);
        TrustManagerFactory tmf = TrustManagerFactory.getInstance("SunX509");
        tmf.init(trustStore);

        ctx.init(kmf.getKeyManagers(),tmf.getTrustManagers(), null);
        
        return ctx.getSocketFactory();
    }


    private static HttpsURLConnection connect(String urlAddress,
                                              SSLSocketFactory ssf)
            throws Exception {

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

        connection.setDoOutput(true);

        return connection;
    }
}
