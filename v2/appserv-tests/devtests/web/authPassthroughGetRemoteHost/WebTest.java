import java.io.*;
import java.net.*;
import com.sun.ejte.ccl.reporter.*;

/*
 * Unit test for 6347215 ("AS81 to support client-ip from loadbalancer as in
 * AS71 using AuthPassThroughEnabled"):
 *
 * This test sets the HTTP listener's authPassthroughEnabled property to TRUE,
 * includes a 'Proxy-ip' header in the request, and expects that the address
 * returned by ServletRequest.getRemoteAddr() match the host name that
 * corresponds to the value of the 'Proxy-ip' header (for this to work, this
 * test also sets http-service.http-protocol.dns-lookup-enabled to TRUE).
 */
public class WebTest {

    private static SimpleReporterAdapter stat
        = new SimpleReporterAdapter("appserv-tests");

    private static final String EXPECTED_RESPONSE = 
        "RemoteHost=localhost";

    private static final String TEST_NAME =
        "auth-passthrough-get-remote-host";

    private String host;
    private String port;
    private String contextRoot;

    public WebTest(String[] args) {
        host = args[0];
        port = args[1];
        contextRoot = args[2];
    }
    
    public static void main(String[] args) {
        stat.addDescription("Unit test for 6347215");
        WebTest webTest = new WebTest(args);
        webTest.doTest();
        stat.printSummary(TEST_NAME);
    }

    public void doTest() {
     
        try { 
            testRemoteAddress();
            stat.addStatus(TEST_NAME, stat.PASS);
        } catch (Exception ex) {
            System.out.println(TEST_NAME + " test failed");
            stat.addStatus(TEST_NAME, stat.FAIL);
            ex.printStackTrace();
        }
    }

    private void testRemoteAddress() throws Exception {
         
        Socket sock = new Socket(host, new Integer(port).intValue());
        OutputStream os = sock.getOutputStream();
        String get = "GET " + contextRoot + "/jsp/remoteHost.jsp "
            + "HTTP/1.0\n";
        System.out.println(get);
        os.write(get.getBytes());
        os.write("Proxy-ip: 127.0.0.1\n".getBytes());
        os.write("\n".getBytes());
        
        InputStream is = null;
        BufferedReader bis = null;
        String line = null;
        String lastLine = null;
        try {
            is = sock.getInputStream();
            bis = new BufferedReader(new InputStreamReader(is));
            while ((line = bis.readLine()) != null) {
                lastLine = line;
            }
        } finally {
            try {
                if (sock != null) {
                    sock.close();
                }
            } catch (IOException ioe) {
                // ignore
            }
            try {
                if (is != null) {
                    is.close();
                }
            } catch (IOException ioe) {
                // ignore
            }
            try {
                if (bis != null) {
                    bis.close();
                }
            } catch (IOException ioe) {
                // ignore
            }
        }

        if (!lastLine.startsWith(EXPECTED_RESPONSE)) {
            throw new Exception("Wrong response. " + "Expected: " +
                EXPECTED_RESPONSE + ", received: " + lastLine);
        }
    }
}
