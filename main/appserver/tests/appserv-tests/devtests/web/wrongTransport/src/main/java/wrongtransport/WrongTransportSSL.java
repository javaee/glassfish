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

package wrongtransport;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.KeyStore;
import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;

import com.sun.appserv.test.BaseDevTest;
import org.glassfish.grizzly.config.portunif.HttpProtocolFinder;

public class WrongTransportSSL extends BaseDevTest {
    private static final String TEST_NAME = "wrongTransportSSL";
    private String redirectURL;

    public WrongTransportSSL(final String host, final String port, final String path) {
        redirectURL = "http://" + host + ":" + port + "/";
        createPUElements();
        try {
            SSLSocketFactory ssf = getSSLSocketFactory(path);
            final String url = "https://" + host + ":" + port + "/";
            HttpURLConnection connection = doSSLHandshake(url, ssf);
            checkStatus(connection);
            parseResponse(connection);
        } catch (Throwable t) {
            t.printStackTrace();
        } finally {
            deletePUElements();
        }
        stat.printSummary();
    }

    @Override
    protected String getTestName() {
        return TEST_NAME;
    }

    @Override
    protected String getTestDescription() {
        return "Wrong Protocol SSL test";
    }

    public static void main(String args[]) throws Exception {
        new WrongTransportSSL(args[0], args[1], args[2]);
    }

    private void createPUElements() {
        // https-redirect
        report("create-https-redirect-protocol", asadmin("create-protocol",
            "--securityenabled=true",
            "https-redirect"));
        report("create-protocol-filter-redirect", asadmin("create-protocol-filter",
            "--protocol", "https-redirect",
            "--classname", "org.glassfish.grizzly.config.portunif.HttpRedirectFilter",
            "redirect-filter"));
        report("create-https-redirect-ssl", asadmin("create-ssl",
            "--certname", "s1as",
            "--type", "network-listener",
            "--ssl2enabled", "false",
            "--ssl3enabled", "false",
            "--clientauthenabled", "false",
            "https-redirect"));
        //  pu-protocol
        report("create-pu-protocol", asadmin("create-protocol",
            "pu-protocol"));
        report("create-protocol-finder-http-finder", asadmin("create-protocol-finder",
            "--protocol", "pu-protocol",
            "--targetprotocol", "http-listener-1",
            "--classname", HttpProtocolFinder.class.getName(),
            "http-finder"));
        report("create-protocol-finder-https-redirect", asadmin("create-protocol-finder",
            "--protocol", "pu-protocol",
            "--targetprotocol", "https-redirect",
            "--classname", HttpProtocolFinder.class.getName(),
            "https-redirect"));
        // reset listener
        report("set-https-listener-protocol", asadmin("set",
            "configs.config.server-config.network-config.network-listeners.network-listener.http-listener-2.protocol=pu-protocol"));
    }

    private void deletePUElements() {
        // reset listener
        report("reset-https-listener-protocol", asadmin("set",
            "configs.config.server-config.network-config.network-listeners.network-listener.http-listener-2.protocol=http-listener-2"));
        report("delete-pu-protocol", asadmin("delete-protocol",
            "pu-protocol"));
        report("delete-https-redirect", asadmin("delete-protocol",
            "https-redirect"));
    }

    private void checkStatus(HttpURLConnection connection) throws Exception {
        report("response-code", connection.getResponseCode() == 302);
        report("returned-location", redirectURL.equals(connection.getHeaderField("location")));
    }

    private SSLSocketFactory getSSLSocketFactory(String trustStorePath)
        throws Exception {
        SSLContext sc = SSLContext.getInstance("SSL");
        sc.init(null, getTrustManagers(trustStorePath), null);
        return sc.getSocketFactory();
    }

    private HttpsURLConnection doSSLHandshake(String urlAddress, SSLSocketFactory ssf)
        throws Exception {
        URL url = new URL(urlAddress);
        HttpsURLConnection.setDefaultSSLSocketFactory(ssf);
        HttpsURLConnection.setFollowRedirects(true);
        HttpsURLConnection connection = (HttpsURLConnection) url.openConnection();
        connection.setHostnameVerifier(
            new HostnameVerifier() {
                public boolean verify(String server, SSLSession session) {
                    return true;
                }
            });
        connection.setDoOutput(true);
        return connection;
    }

    private TrustManager[] getTrustManagers(String path) throws Exception {
        TrustManager[] tms = null;
        InputStream stream = new FileInputStream(path);
        try {
            KeyStore trustStore = KeyStore.getInstance("JKS");
            trustStore.load(stream, null);
            TrustManagerFactory tmf = TrustManagerFactory.getInstance(
                    TrustManagerFactory.getDefaultAlgorithm());
            tmf.init(trustStore);
            tms = tmf.getTrustManagers();
        } finally {
            stream.close();
        }
        return tms;
    }

    private void parseResponse(HttpURLConnection connection) throws Exception {
        BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
        try {
            while (in.readLine() != null) {
            }
        } finally {
            in.close();
        }
    }
}
