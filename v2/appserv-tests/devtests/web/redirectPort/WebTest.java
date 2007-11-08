import java.io.*;
import java.net.*;
import java.security.*;
import javax.net.ssl.*;
import com.sun.ejte.ccl.reporter.*;

/*
 * Test case for 4946739 ("Tomcat default SSL port used when redirect-port not
 * specified on http-listener").
 */
public class WebTest {

    private static SimpleReporterAdapter stat
        = new SimpleReporterAdapter("appserv-tests");
    private static final String TEST_NAME = "redirect-port";

    private String host;
    private String port;
    private String httpsPort;
    private String contextRoot;
    private String trustStorePath;

    public WebTest(String[] args) {
        host = args[0];
        port = args[1];
        contextRoot = args[2];
        trustStorePath = args[3];
        httpsPort = args[4];
    }
    
    public static void main(String[] args) {
        stat.addDescription("Unit test for Bugtraq 4946739");
        WebTest webTest = new WebTest(args);
        webTest.doTest();
        stat.printSummary(TEST_NAME);
    }

    public void doTest() {

        URL url = null;
        int responseCode;
        boolean fail = false;

        try {

            System.out.println("http-listener secure port: " + httpsPort);

            url = new URL("http://" + host  + ":" + port + contextRoot
                    + "/jsp/test.jsp");
            System.out.println("Connecting to: " + url.toString());
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.connect();
            responseCode = conn.getResponseCode();
            System.out.println("Response code: " + responseCode);
            if (responseCode != HttpURLConnection.HTTP_MOVED_TEMP) {
                fail = true;
            } else {
                url = new URL(conn.getHeaderField("Location"));
                System.out.println("Redirected to: " + url.toString());
                SSLContext ctx = SSLContext.getInstance("TLS"); 
                ctx.init(null, getTrustManagers(trustStorePath), null);
                HttpsURLConnection httpsConn = (HttpsURLConnection) url.openConnection();
                httpsConn.setSSLSocketFactory(ctx.getSocketFactory());
                httpsConn.setHostnameVerifier(new MyHostnameVerifier());
                httpsConn.connect();
                responseCode = httpsConn.getResponseCode();
                System.out.println("Response code: " + responseCode);
                if (responseCode != HttpURLConnection.HTTP_OK) {
                    fail = true;
                }
            }

            if (fail) {
                stat.addStatus(TEST_NAME, stat.FAIL);
            } else {
                stat.addStatus(TEST_NAME, stat.PASS);
            }

        } catch (Exception ex) {
            System.out.println(TEST_NAME + " test failed");
            stat.addStatus(TEST_NAME, stat.FAIL);
            ex.printStackTrace();
        }
    }

    private TrustManager[] getTrustManagers(String path) throws Exception {

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

    private static class MyHostnameVerifier implements HostnameVerifier {

        public boolean verify(String hostname, SSLSession session) {
            return true;
	}
    }
}
