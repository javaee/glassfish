import java.io.*;
import java.util.*;
import java.security.*;
import java.net.*;
import javax.net.ssl.*;
import com.sun.ejte.ccl.reporter.*;

/*
 * Make sure that servlet code has access to client cert (provided client
 * has been successfully authenticated using SSL client auth) even if the
 * webapp that the servlet is part of does not protect any of its resources
 * with CLIENT-CERT.
 *
 * SSL client auth is enforced by virtue of the HTTPS listener
 * having client-auth-enabled set to true.
 */
public class WebTest {

    private static final String TEST_NAME
        = "ssl-client-auth-unprotected-resource-get-client-cert";    

    private static final String EXPECTED_RESPONSE
        = "[Ljava.security.cert.X509Certificate;";

    private static SimpleReporterAdapter stat
        = new SimpleReporterAdapter("appserv-tests");


    public static void main(String args[]) throws Exception{

        String host = args[0];
        String port = args[1];
        String contextRoot = args[2];
        String keyStorePath = args[3];
        String trustStorePath = args[4];
        
        try {
            SSLSocketFactory ssf = getSSLSocketFactory(keyStorePath,
                                                       trustStorePath);
            HttpsURLConnection connection = connect("https://" + host  + ":"
                                                    + port + contextRoot
                                                    + "/TestServlet",
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


    private static SSLSocketFactory getSSLSocketFactory(String keyStorePath,
                                                        String trustStorePath)
            throws Exception {

        SSLContext ctx = SSLContext.getInstance("TLS");

        // Keystore 
        KeyStore ks = KeyStore.getInstance("JKS");
        char[] passphrase = "changeit".toCharArray();
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
