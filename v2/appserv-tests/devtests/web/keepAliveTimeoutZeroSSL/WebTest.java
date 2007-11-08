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
    private static final String TEST_NAME = "keepAliveTimeoutInSecondsZeroSSL";
    static String host =""; 
    static String port ="";
    public static final String SSL = "SSL";

    public static void main(String args[]) throws Exception{
        host = args[0];
        port = args[1];
        String contextRoot = args[2];
        String trustStorePath = args[3];

        stat.addDescription("Testing keep-alive timeout-in-seconds=0");

        try {
            doTest();
        } catch (Throwable t) {
            t.printStackTrace();
        }
        stat.printSummary(TEST_NAME);
    }

    private static void doTest() throws Throwable{
        SSLSocketFactory sslsocketfactory = getSSLSocketFactory();
        SSLSocket sslsocket = 
            (SSLSocket)sslsocketfactory
            .createSocket(host,Integer.parseInt(port));

        OutputStream outputstream = sslsocket.getOutputStream();
        OutputStreamWriter outputstreamwriter 
                                = new OutputStreamWriter(outputstream);
        BufferedWriter bufferedwriter = new BufferedWriter(outputstreamwriter);

        bufferedwriter.write("GET / HTTP/1.1" + '\n');
        bufferedwriter.write("Host: localhost" + '\n'+'\n');
        bufferedwriter.flush();
        
        InputStream sslIn = sslsocket.getInputStream();
        InputStreamReader inputstreamreader1 = new InputStreamReader(sslIn);
        BufferedReader bufferedreader1 = new BufferedReader(inputstreamreader1);
        String string1 = null;
        long t1 = System.currentTimeMillis();
        while ((string1 = bufferedreader1.readLine()) != null) {
            System.out.println("line = " + string1);
        }
        System.out.println('\n');
        long t2 = System.currentTimeMillis();
        String val = String.valueOf((t2-t1)/1000);
        System.out.println("keep alive for "+val+" seconds");
        if (t2-t1 < 5000) {
            stat.addStatus(TEST_NAME, stat.PASS);
        } else {
            stat.addStatus(TEST_NAME, stat.FAIL);
        }
    }

    public static SSLSocketFactory getSSLSocketFactory() throws IOException{
        if(host == null || port==null) {
            throw new IOException("null");
        }
        
        try {
            //---------------------------------
            // Create a trust manager that does not validate certificate chains
            TrustManager[] trustAllCerts = new TrustManager[]{
                new X509TrustManager() {
                    public java.security.cert.X509Certificate[] getAcceptedIssuers() {
                        return null;
                    }
                    public void checkClientTrusted(
                            java.security.cert.X509Certificate[] certs, String authType) {
                    }
                    public void checkServerTrusted(
                            java.security.cert.X509Certificate[] certs, String authType) {
                    }
                }
            };
            
            // Install the all-trusting trust manager
            SSLContext sc = SSLContext.getInstance(SSL);
            sc.init(null, trustAllCerts, new java.security.SecureRandom());
            
            //---------------------------------
            return sc.getSocketFactory();
        } catch(Exception e){
                e.printStackTrace();
            throw new IOException(e.getMessage());
        }finally {
        }
    }        
}
