import java.io.*;
import java.util.Properties;
import java.net.*;
import java.security.KeyStore;
import javax.net.*;
import javax.net.ssl.*;
import com.sun.ejte.ccl.reporter.*;

public class WebTest{


    static SimpleReporterAdapter stat=
           new SimpleReporterAdapter("appserv-tests");
    
    private static int count = 0;
    private static int EXPECTED_COUNT = 1;

    static String host = null;
    static String port = null;

    public static void main(String args[]) throws Exception{
        host = args[0];
        port = args[1];
        String contextRoot = args[2];
        String trustStorePath = args[3];

        stat.addDescription("Wrong Protocol SSL test");

        try {
            SSLSocketFactory ssf = getSSLSocketFactory(trustStorePath);
            System.out.println("Connecting to: " + "https://" + host  + ":" + port + "/");
            HttpsURLConnection connection = doSSLHandshake(
                            "https://" + host  + ":" + port + "/", ssf);
            checkStatus(connection); 
            parseResponse(connection);
        } catch (Throwable t) {
            t.printStackTrace();
        }

        if (count != EXPECTED_COUNT){
            stat.addStatus("wrongProtocol", stat.FAIL);
        }           

        stat.printSummary("wrongProtocol");
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

        int responseCode=connection.getResponseCode();
        String location = connection.getHeaderField("location");
        System.out.println("Response code: " + responseCode + " Expected code: 302"); 
        if (location!=null && location.equals("http://" + host + ":" + port + "/")){
            stat.addStatus("wrongProtocol", stat.PASS);
        } else {
            stat.addStatus("wrongProtocol", stat.FAIL);
        }
    }

    private static void parseResponse(HttpsURLConnection connection)
                    throws Exception{

       BufferedReader in = new BufferedReader(
                new InputStreamReader(connection.getInputStream()));
            
        String line = "";
        int index;
        while ((line = in.readLine()) != null) {
            System.out.println(line);
        }

        in.close();
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
