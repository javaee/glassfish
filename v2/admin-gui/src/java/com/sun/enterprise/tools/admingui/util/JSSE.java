/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
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

package com.sun.enterprise.tools.admingui.util;

import java.security.NoSuchAlgorithmException;
import java.security.KeyManagementException;

import javax.net.ssl.HttpsURLConnection;
import java.security.cert.X509Certificate;
import javax.net.ssl.SSLContext;
import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLSession;
import javax.net.ssl.X509TrustManager;

import com.sun.appserv.management.client.TrustAnyTrustManager;


/**
 * This class trusts any server generated certifcate blindly to
 * handle ssl connection. We have commented out the code to generate
 * client certificate, and store server certificate in truststore during 
 * handshake. We use it if necessary later, right now we just trust it.
 */
public class JSSE {

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
    public static void trustAnyServerCertificate() throws NoSuchAlgorithmException, KeyManagementException {
        SSLContext sslc = SSLContext.getInstance("SSLv3");
        final X509TrustManager[] tms = TrustAnyTrustManager.getInstanceArray();
        sslc.init(null, tms, null);
        if (sslc != null) {
            HttpsURLConnection.setDefaultSSLSocketFactory(sslc.getSocketFactory());
        }
	if(HttpsURLConnection.getDefaultHostnameVerifier() instanceof AcceptAnyHostName) {
		return;
	}
        HostnameVerifier hv = new AcceptAnyHostName();
        HttpsURLConnection.setDefaultHostnameVerifier(hv);
    }

    private static class AcceptAnyHostName implements HostnameVerifier{
		public boolean verify(String s, SSLSession ssl) {
			return true;
		}
	}


}
