import java.io.*;
import java.util.*;
import java.security.*;
import java.net.*;
import javax.net.ssl.*;
import com.sun.ejte.ccl.reporter.*;

/*
 * Unit test related to 
 * https://glassfish.dev.java.net/issues/show_bug.cgi?id=562
 * ("HttpServletRequest does not return any attribute in JAX-WS web service"):
 *
 * Make sure ServletRequest.getAttributeNames() returns all SSL-related
 * request attributes mandated by the Servlet spec when the request is over
 * HTTPS with SSL client auth turned on, namely:
 *
 *   javax.servlet.request.cipher_suite
 *   javax.servlet.request.key_size
 *   javax.servlet.request.X509Certificate
 *
 * even if none of these attributes have been requested explicitly by a call
 * to ServletRequest.getAttribute().
 *
 * (SSL client auth is enforced by virtue of the HTTPS listener
 * having client-auth-enabled set to true.)
 */
public class WebTest {

    private static final String TEST_NAME = "servlet-ssl-request-attributes";

    private static final String SSL_CIPHER_SUITE
        = "javax.servlet.request.cipher_suite";
    private static final String SSL_KEY_SIZE
        = "javax.servlet.request.key_size";
    private static final String SSL_CERTIFICATE
        = "javax.servlet.request.X509Certificate";

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
            String line = in.readLine();
            System.out.println("Response: " + line);
            if (line != null
                    && (line.indexOf(SSL_CIPHER_SUITE) >= 0)
                    && (line.indexOf(SSL_KEY_SIZE) >= 0)
                    && (line.indexOf(SSL_CERTIFICATE) >= 0)) {
                stat.addStatus(TEST_NAME, stat.PASS);
            } else {
                System.err.println("Wrong response");
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
