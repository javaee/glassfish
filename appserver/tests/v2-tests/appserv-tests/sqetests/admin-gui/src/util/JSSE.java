/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2018 Oracle and/or its affiliates. All rights reserved.
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

package util;

import javax.net.ssl.HttpsURLConnection;
import java.security.cert.Certificate;
import java.security.cert.X509Certificate;
import javax.security.auth.x500.X500Principal;
import javax.net.ssl.SSLContext;
import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLSession;
import javax.net.ssl.X509TrustManager;
import java.io.InputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.security.KeyStore;
import java.util.StringTokenizer;
import java.net.URLConnection;
import java.net.URL;

import com.sun.appserv.management.client.TrustAnyTrustManager;


/**
 *
 *
 *
 *
 */
public class JSSE {

    private URL url = null;

    public JSSE(URL url) {
        this.url = url;
    }

   /* public String getHostFromCertificate() throws Exception {
        HttpsURLConnection https = getHttpsURLConnection();
        https.connect();
        //We don't have to do the following, may be we can getaway with just
        //accepting any server certificate.
        Certificate[] cert = https.getServerCertificates();
        generateTrustStore(cert[0]);
        String dn = getDistinguishedName(cert[0]);
        String hostName = getHostNameFromDN(dn);
        return hostName;
    }
    private void generateTrustStore(Certificate cert) throws Exception {
        File f = new File("certdb.jks");
        FileOutputStream fout = new FileOutputStream(f);
        KeyStore key = KeyStore.getInstance("JKS");//default is JKS
        key.load(null, null); //initialize keystore
        key.setCertificateEntry("s1as", cert);
        key.store(fout, new char[]{'c', 'h', 'a', 'n', 'g', 'e', 'i', 't'});
        System.setProperty("javax.net.ssl.trustStore", "out.jks");
        System.setProperty("javax.net.ssl.trustStorePassword", "changeit");

    }
    private String getDistinguishedName(Certificate cert) {
        String dn = ((X509Certificate)cert).getSubjectX500Principal().getName();
        return dn;
    }
    private String getHostNameFromDN(String dn) {
        StringTokenizer str = new StringTokenizer(dn, ",");
        String s = str.nextToken();
        return s.substring(s.indexOf("=")+1);
    }*/
    public void trustAnyServerCertificate() throws Exception {
        //URL url = new URL("https", "cchidamb-pc.sfbay.sun.com", 4849, "/asadmin/admingui/homePage");
        SSLContext sslc = SSLContext.getInstance("SSLv3");
        final X509TrustManager[] tms = TrustAnyTrustManager.getInstanceArray();
        sslc.init(null, tms, null);
        if (sslc != null) {
            HttpsURLConnection.setDefaultSSLSocketFactory(sslc.getSocketFactory());
        }
        HostnameVerifier hv = new AcceptAnyHostName();
        HttpsURLConnection.setDefaultHostnameVerifier(hv);
        URLConnection conn = url.openConnection();
        HttpsURLConnection https = (HttpsURLConnection)conn;
        https.connect();
        //return https;
    }

    private static class AcceptAnyHostName implements HostnameVerifier{
		public boolean verify(String s, SSLSession ssl) {
			return true;
		}
	}


}
