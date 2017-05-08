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

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.security.SecureRandom;
import java.security.cert.X509Certificate;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import com.sun.appserv.test.util.results.SimpleReporterAdapter;

public class WebTest {
    private static final String TEST_NAME = "keepAliveTimeoutSSL";
    private static final SimpleReporterAdapter stat = new SimpleReporterAdapter("appserv-tests", TEST_NAME);
    private String host;
    private String port;
    public static final String TLS = "TLS";

    public WebTest(String[] args) {
        host = args[0];
        port = args[1];
    }

    public static void main(String[] args) throws Throwable {
        stat.addDescription(TEST_NAME);
        WebTest webTest = new WebTest(args);
        webTest.doTest();
        stat.printSummary();
    }

    private void doTest() throws Throwable {
        BufferedWriter writer = null;
        BufferedReader reader = null;
        try {
            SSLSocketFactory sslsocketfactory = getSSLSocketFactory();
            SSLSocket sslsocket = (SSLSocket) sslsocketfactory.createSocket(host, Integer.parseInt(port));
            writer = new BufferedWriter(new OutputStreamWriter(sslsocket.getOutputStream()));
            writer.write("GET / HTTP/1.1" + '\n');
            writer.write("Host: localhost" + '\n' + '\n');
            writer.flush();
            reader = new BufferedReader(new InputStreamReader(sslsocket.getInputStream()));
            long start = System.currentTimeMillis();
            try {
                while (reader.readLine() != null) {
                }
            } catch (IOException e) {
            }
            long end = System.currentTimeMillis();
            System.out.println("WebTest.invoke: end - start = " + (end - start));
            stat.addStatus(TEST_NAME, end - start >= 10000 ? SimpleReporterAdapter.PASS : SimpleReporterAdapter.FAIL);
        } finally {
            if (writer != null) {
                writer.close();
            }
            if (reader != null) {
                reader.close();
            }
        }
    }

    public SSLSocketFactory getSSLSocketFactory() throws IOException {
        if (host == null || port == null) {
            throw new IOException("null");
        }
        try {
            //---------------------------------
            // Create a trust manager that does not validate certificate chains
            TrustManager[] trustAllCerts = new TrustManager[]{
                new X509TrustManager() {
                    public X509Certificate[] getAcceptedIssuers() {
                        return null;
                    }

                    public void checkClientTrusted(
                        X509Certificate[] certs, String authType) {
                    }

                    public void checkServerTrusted(
                        X509Certificate[] certs, String authType) {
                    }
                }
            };
            // Install the all-trusting trust manager
            SSLContext sc = SSLContext.getInstance(TLS);
            sc.init(null, trustAllCerts, new SecureRandom());
            //---------------------------------
            return sc.getSocketFactory();
        } catch (Exception e) {
            e.printStackTrace();
            throw new IOException(e.getMessage());
        } finally {
        }
    }
}
