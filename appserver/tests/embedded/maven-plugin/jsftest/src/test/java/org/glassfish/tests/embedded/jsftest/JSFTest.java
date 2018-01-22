/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2010-2018 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.tests.embedded.jsftest;

import org.junit.Assert;
import org.junit.Test;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.SecureRandom;
import java.security.cert.X509Certificate;

public class JSFTest {

    @Test
    public void testWeb() throws Exception {

        disableCertValidation();

        goGet("http://localhost:8080/test/JSFTestServlet", "Created viewRoot");
        
        // test non secure access.
        goGet("http://localhost:8080/test", "BHAVANI", "SHANKAR", "Mr. X");

        // test secure access.
        goGet("https://localhost:8181/test", "BHAVANI", "SHANKAR", "Mr. X");
    }

    private static void goGet(String url, String... match) throws Exception {
        try {

            URL servlet = new URL(url);
            HttpURLConnection uc = (HttpURLConnection) servlet.openConnection();
            System.out.println("\nURLConnection = " + uc + " : ");
            if (uc.getResponseCode() != 200) {
                throw new Exception("Servlet did not return 200 OK response code");
            }

            BufferedReader in = new BufferedReader(new InputStreamReader(
                    uc.getInputStream()));
            String line = null;
            boolean[] found = new boolean[match.length];

            int count = 0;
            while ((line = in.readLine()) != null) {
                System.out.println(line);
                for (String m : match) {
                    int index = line.indexOf(m);
                    if (index != -1 && count < match.length) {
                        found[count++] = true;
                        System.out.println("Found [" + m + "] in the response, index = " + count);
                        break;
                    }
                }
            }

            for (boolean f : found) {
                Assert.assertTrue(f);
            }
            System.out.println("\n***** SUCCESS **** Found all matches in the response.*****\n");
            in.close();
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    public static void disableCertValidation() {
        // Create a trust manager that does not validate certificate chains
        TrustManager[] trustAllCerts = new TrustManager[]{new X509TrustManager() {
            public X509Certificate[] getAcceptedIssuers() {
                return null;
            }

            public void checkClientTrusted(X509Certificate[] certs, String authType) {
                return;
            }

            public void checkServerTrusted(X509Certificate[] certs, String authType) {
                return;
            }
        }};

        try {
            SSLContext sc = SSLContext.getInstance("TLS");
            sc.init(null, trustAllCerts, new SecureRandom());
            HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());
        } catch (Exception e) {
            return;
        }
    }


}
