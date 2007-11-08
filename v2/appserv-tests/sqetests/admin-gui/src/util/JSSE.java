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
