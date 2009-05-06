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
    
    public static void main(String args[]) throws Exception{
        String host = args[0];
        String httpPort = args[1];
        String httpsPort = args[2];
        String contextRoot = args[3];
        String trustStorePath = args[4];

        stat.addDescription("Testing @TransportProtected");

        try {
            SSLSocketFactory ssf = getSSLSocketFactory(trustStorePath);

            testURL("GET", "https://" + host + ":" + httpsPort + "/" + contextRoot + "/myurl", ssf, false,
                    "c:Hello:true", "transport-protected-class");
            testURL("GET", "https://" + host + ":" + httpsPort + "/" + contextRoot + "/myurl2", ssf, false,
                    "m:Hello:true", "transport-protected-method");
            testURL("TRACE", "http://" + host + ":" + httpPort + "/" + contextRoot + "/myurl2", null, true,
                    "mfr:Hello:javaee:false", "transport-protected-false-roleallowed-method");
            testURL("GET", "https://" + host + ":" + httpsPort + "/" + contextRoot + "/myurl3", ssf, true,
                    "g:Hello:javaee:true", "rolesallowed-transport-protected-method");
            
            testURL("TRACE", "http://" + host + ":" + httpPort + "/" + contextRoot + "/myurl3", null, true,
                    "t:Hello:javaee:false", "rolesallowed-transport-protected-false-method");
        } catch (Throwable t) {
            stat.addStatus("@TransportProtected", stat.FAIL);
            t.printStackTrace();
        }
        stat.printSummary();
    }

    private static void testURL(String httpMethod, String url, SSLSocketFactory ssf,
            boolean needAuthenticate, String expected, String testName)
            throws Exception {

        HttpURLConnection connection = doHandshake(httpMethod, url, ssf, needAuthenticate);
        if (checkStatus(connection, testName)) {
            parseResponse(connection, expected, testName);
        } else {
            stat.addStatus(testName + "-responseCode", stat.FAIL);
        }
    }

    private static SSLSocketFactory getSSLSocketFactory(String trustStorePath)
                    throws Exception {
        SSLContext sc = SSLContext.getInstance("SSL");
        sc.init(null, getTrustManagers(trustStorePath), null);
        return sc.getSocketFactory();
    }

    private static HttpURLConnection doHandshake(String httpMethod,
            String urlAddress, SSLSocketFactory ssf,
            boolean needAuthenticate) throws Exception{

        URL url = new URL(urlAddress);
        HttpURLConnection connection = null;

        if (ssf != null) {
            HttpsURLConnection.setDefaultSSLSocketFactory(ssf);
            HttpsURLConnection conn = (HttpsURLConnection) url.openConnection();

            conn.setHostnameVerifier(
                new HostnameVerifier() {
                    public boolean verify(String rserver, SSLSession sses) {
                        return true;
                    }
            });
            
            connection = conn;
        } else {
            connection = (HttpURLConnection) url.openConnection();
        }
        connection.setRequestMethod(httpMethod);
        connection.setDoOutput(true);
        if (needAuthenticate) {
            connection.setRequestProperty("Authorization", "Basic amF2YWVlOmphdmFlZQ==");
        }
        return connection;
    }

    private static boolean checkStatus(HttpURLConnection connection,
            String testName) throws Exception{

        int responseCode =  connection.getResponseCode();
        System.out.println("Response code: " + responseCode + " Expected code: 200"); 
        return (connection.getResponseCode() == 200);
    }

    private static void parseResponse(HttpURLConnection connection,
            String expected, String testName) throws Exception {

        BufferedReader in = null;
        boolean ok = false;
        try {
            in = new BufferedReader(
                new InputStreamReader(connection.getInputStream()));
            
            String line = "";
            while ((line = in.readLine()) != null) {
                System.out.println(line);
                if (line.equals(expected)) {
                    ok = true;
                    break;
                }
            }
        } finally {
            try {
                if (in != null) {
                    in.close();
                } 
            } catch(IOException ioe) {
                // ignore
            }
        }

        if (ok) {
            stat.addStatus(testName, stat.PASS);
        } else {
            stat.addStatus(testName, stat.FAIL);
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
