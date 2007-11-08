import java.io.*;
import java.util.Properties;
import java.net.*;
import java.security.KeyStore;
import javax.net.*;
import javax.net.ssl.*;
import com.sun.ejte.ccl.reporter.*;

/**
 * Bugster 6480567
 */
public class WebTest{


    static SimpleReporterAdapter stat=
           new SimpleReporterAdapter("appserv-tests");
    
    private static int count = 0;
    private static int EXPECTED_COUNT = 3;

    public static void main(String args[]) throws Exception{
        String host = args[0];
        String port = args[1];
        String contextRoot = args[2];
        String trustStorePath = args[3];

        stat.addDescription("serverName SSL");

        try {
            SSLSocketFactory ssf = getSSLSocketFactory(trustStorePath);
            HttpsURLConnection connection = doSSLHandshake(
                            "https://" + host  + ":" + port + "/", ssf);
            checkStatus(connection); 

            connection = doSSLHandshake(
                "https://" + host  + ":" + port + "/" + contextRoot 
                + "/ServletTest", ssf);
        } catch (Throwable t) {
            t.printStackTrace();
        }

        if (count != EXPECTED_COUNT){
            stat.addStatus("sslServerName", stat.FAIL);
        }           

        stat.printSummary("sslServerName");
    }

    private static SSLSocketFactory getSSLSocketFactory(String trustStorePath)
                    throws Exception {
        SSLContext sc = SSLContext.getInstance("SSL");
        sc.init(null, getTrustManagers(trustStorePath), null);
        return sc.getSocketFactory();
    }

    private static HttpsURLConnection doSSLHandshake(String urlAddress,
                                                     SSLSocketFactory ssf)
                    throws Exception{
        URL url = new URL(urlAddress);
        HttpsURLConnection.setDefaultSSLSocketFactory(ssf);
        HttpsURLConnection connection = (HttpsURLConnection) url.openConnection();

        connection.setHostnameVerifier(
            new HostnameVerifier() {
                public boolean verify(String rserver, SSLSession sses) {
                    return true;
                }
        });
        connection.setDoOutput(true);
        return connection;
    }

    private static void checkStatus(HttpsURLConnection connection)
                    throws Exception{

        int responseCode=  connection.getResponseCode();
        System.out.println("Response code: " + responseCode + " Expected code: 200"); 
        if (connection.getResponseCode() != 200){
            stat.addStatus("sslServerName", stat.FAIL);
        } else {
            stat.addStatus("sslServerName", stat.PASS);
        }
    }

    private static TrustManager[] getTrustManagers(String path)
                    throws Exception {

        TrustManager[] tms = null;
        InputStream istream = null;

        try {
            KeyStore trustStore = KeyStore.getInstance("JKS");
            istream = new FileInputStream(path);
            trustStore.load(istream, null);
            istream.close();
            istream = null;
            TrustManagerFactory tmf = TrustManagerFactory.getInstance("SunX509");
            tmf.init(trustStore);
            tms = tmf.getTrustManagers();

        } finally {
            if (istream != null) {
                try {
                    istream.close();
                } catch (IOException ioe) {
                    // Do nothing
                }
            }
        }

        return tms;
    }

}
