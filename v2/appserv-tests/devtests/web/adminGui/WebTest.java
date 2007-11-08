import java.lang.*;
import java.io.*;
import java.net.*;
import java.security.KeyStore;
import javax.net.ssl.*;
import com.sun.ejte.ccl.reporter.*;

public class WebTest
{
    
    private static int count = 0;
    private static int EXPECTED_COUNT = 1;
    
    static SimpleReporterAdapter stat=
        new SimpleReporterAdapter("appserv-tests");

    public static void main(String args[])
    {

        // The stat reporter writes out the test info and results
        // into the top-level quicklook directory during a run.
      
        stat.addDescription("Admin Gui sanity check");

        String host = args[0];
        String portS = args[1];
        String trustStorePath = args[2];

        int port = new Integer(portS).intValue();
        String name;
        
        try {
            goGet(host, port , trustStorePath);
            
        } catch (Throwable t) {
            t.printStackTrace();
            count = 0;
        } finally {
            if (count != EXPECTED_COUNT){
                stat.addStatus("web-adminGui", stat.FAIL);
            }           
        }

        stat.printSummary("web/adminGui---> expect 1 PASS");
    }

    private static void goGet(String host, int port, String trustStorePath)
         throws Exception
    {
        try {
            URL url = new URL("http://" + host  + ":" + port + "/");
            System.out.println("Connecting to: " + url.toString());
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.connect();
            int responseCode = conn.getResponseCode();
            System.out.println("Response code: " + responseCode);
            if (responseCode == HttpURLConnection.HTTP_MOVED_TEMP) {
                url = new URL(conn.getHeaderField("Location"));
                System.out.println("Redirected to: " + url.toString());
                conn = (HttpURLConnection) url.openConnection();
                conn.connect();
                responseCode = conn.getResponseCode();
            }

            if (responseCode == HttpURLConnection.HTTP_OK) {
                InputStream is = conn.getInputStream();
                BufferedReader input = new BufferedReader(
                    new InputStreamReader(is));
                String line = null;
                while ((line = input.readLine()) != null) {
                    System.out.println(line);
                    if (line.equalsIgnoreCase("<title>login</title>")) {
                        stat.addStatus("web-adminGui", stat.PASS);
                        count++;
                        break;
                    }
                }
            }
        } catch( Exception ex){
            ex.printStackTrace();
        }

        // If it failed with clear connection, it means we are testing against
        // EE, and we must use SSL.
        if ( count == 0 ){
            System.out.println("Trying with SSL"); 
            SSLSocketFactory ssf = getSSLSocketFactory(trustStorePath);

            System.out.println("Connecting to " + "https://" + host  + ":" + port + "/");


            HttpsURLConnection connection = doSSLHandshake(
                            "https://" + host  + ":" + port + "/", ssf);

            try{
               BufferedReader in = new BufferedReader(
                        new InputStreamReader(connection.getInputStream()));
                String line= "";
                int index = 0;
                while ((line = in.readLine()) != null) {
                    System.out.println(line);
                    if (line.equalsIgnoreCase(
                            "<title>Sun ONE Application Server Login</title>")){
                        stat.addStatus("web-adminGui", stat.PASS);
                        count++;
                        break;
                    }
                }
            } catch (Exception ex){
               ex.printStackTrace(); 
            }
        }

        if (count == 0) {
            stat.addStatus("web-adminGui", stat.FAIL);
        }

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
