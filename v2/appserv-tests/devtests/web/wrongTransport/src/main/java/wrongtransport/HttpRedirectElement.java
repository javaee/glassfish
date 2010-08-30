/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Sun Microsystems, Inc. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License. You can obtain
 * a copy of the License at https://glassfish.dev.java.net/public/CDDL+GPL.html
 * or glassfish/bootstrap/legal/LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 *
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at glassfish/bootstrap/legal/LICENSE.txt.
 * Sun designates this particular file as subject to the "Classpath" exception
 * as provided by Sun in the GPL Version 2 section of the License file that
 * accompanied this code.  If applicable, add the following below the License
 * Header, with the fields enclosed by brackets [] replaced by your own
 * identifying information: "Portions Copyrighted [year]
 * [name of copyright owner]"
 *
 * Contributor(s):
 *
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
import java.util.Arrays;
import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;

import com.sun.appserv.test.BaseDevTest;
import com.sun.grizzly.config.HttpProtocolFinder;

public class HttpRedirectElement extends BaseDevTest {
    private String targetUrl;
    private boolean secureRedirect;
    private SSLSocketFactory ssf;

    public HttpRedirectElement(final String host, final String port, final String securePort, boolean secureRedirect,
        final String path) {
        this.secureRedirect = secureRedirect;
        stat.getSuite().setName(getTestName());
        stat.getSuite().setDescription(getTestDescription());
        createPUElements();
        try {
            String url;
            if (secureRedirect) {
                targetUrl = "https://" + host + ":" + port + "/";
                url = "http://" + host + ":" + port + "/";
            } else {
                targetUrl = "http://" + host + ":" + securePort + "/";
                url = "https://" + host + ":" + securePort + "/";
                ssf = getSSLSocketFactory(path);
            }
            HttpURLConnection connection = getConnection(url);
            connection.setInstanceFollowRedirects(true);
            checkStatus(connection);
            parseResponse(connection);
        } catch (Throwable t) {
            report("exception found: " + t.getMessage(), false);
            t.printStackTrace();
        } finally {
            deletePUElements();
        }
        stat.printSummary();
    }

    private HttpURLConnection getConnection(final String url) throws Exception {
        return secureRedirect
            ? (HttpURLConnection) new URL(url).openConnection()
            : doSSLHandshake(url, ssf);
    }

    private TrustManager[] getTrustManagers(String path) throws Exception {
        TrustManager[] tms = null;
        InputStream stream = new FileInputStream(path);
        try {
            KeyStore trustStore = KeyStore.getInstance("JKS");
            trustStore.load(stream, null);
            TrustManagerFactory tmf = TrustManagerFactory.getInstance("SunX509");
            tmf.init(trustStore);
            tms = tmf.getTrustManagers();
        } finally {
            stream.close();
        }
        return tms;
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

    @Override
    protected String getTestName() {
        System.out.println("HttpRedirectElement.getTestName: secureRedirect = " + secureRedirect);
        final String name = secureRedirect ? "HttpToHttpsRedirectSamePort" : "HttpsToHttpRedirectSamePort";
        System.out.println("HttpRedirectElement.getTestName: name = " + name);
        return name;
    }

    @Override
    protected String getTestDescription() {
        return "Http " + (secureRedirect ? "-->" : "<--")
            + " Https redirection on the same port using new http-redirect elements";
    }

    public static void main(String args[]) throws Exception {
        System.out.println("HttpRedirectElement.main: args = " + Arrays.toString(args));
        new HttpRedirectElement(args[0], args[1], args[2], Boolean.valueOf(args[3]), args[4]);
    }

    private void createPUElements() {
        final String redirectProtocol = "http-redirect";
        // http-redirect
        report("create-http-redirect-protocol", asadmin("create-protocol",
            "--securityenabled", String.valueOf(!secureRedirect),
            redirectProtocol));
        report("create-http-redirect", asadmin("create-http-redirect",
            "--secure-redirect", String.valueOf(secureRedirect),
            redirectProtocol));
        if (!secureRedirect) {
            report("create-ssl", asadmin("create-ssl",
                "--ssl3enabled", "false",
                "--ssl2enabled", "false",
                "--certname", "s1as",
                "--clientauthenabled", "false",
                "--type", "network-listener",
                 redirectProtocol));
        }
        //  pu-protocol
        report("create-pu-protocol", asadmin("create-protocol", "pu-protocol"));
        report("create-protocol-finder-http-finder", asadmin("create-protocol-finder",
            "--protocol", "pu-protocol",
            "--target-protocol", secureRedirect ? "http-listener-2" : "http-listener-1",
            "--classname", HttpProtocolFinder.class.getName(),
            "http-finder"));
        report("create-protocol-finder-http-redirect", asadmin("create-protocol-finder",
            "--protocol", "pu-protocol",
            "--target-protocol", redirectProtocol,
            "--classname", HttpProtocolFinder.class.getName(),
            redirectProtocol));
        // reset listener
        if (secureRedirect) {
            report("set-http-listener-protocol", asadmin("set",
                "configs.config.server-config.network-config.network-listeners.network-listener.http-listener-1.protocol=pu-protocol"));
        } else {
            report("set-http-listener-protocol", asadmin("set",
                "configs.config.server-config.network-config.network-listeners.network-listener.http-listener-2.protocol=pu-protocol"));
            report("enable-http-listener-protocol", asadmin("set",
                "configs.config.server-config.network-config.network-listeners.network-listener.http-listener-2.enabled=true"));
        }
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    private void deletePUElements() {
        // reset listener
        if (secureRedirect) {
            report("reset-http-listener-protocol", asadmin("set",
                "configs.config.server-config.network-config.network-listeners.network-listener.http-listener-1.protocol=http-listener-1"));
        } else {
            report("disable-http-listener-protocol", asadmin("set",
                "configs.config.server-config.network-config.network-listeners.network-listener.http-listener-2.enabled=false"));
            report("reset-http-listener-protocol", asadmin("set",
                "configs.config.server-config.network-config.network-listeners.network-listener.http-listener-2.protocol=http-listener-2"));
        }
        report("delete-pu-protocol", asadmin("delete-protocol",
            "pu-protocol"));
        report("delete-http-redirect", asadmin("delete-protocol",
            "http-redirect"));
    }

    private void checkStatus(HttpURLConnection connection) throws Exception {
        int responseCode = connection.getResponseCode();
        String location = connection.getHeaderField("location");
        report("response-code", responseCode == 302);
        report("returned-location", targetUrl.equals(location));
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
