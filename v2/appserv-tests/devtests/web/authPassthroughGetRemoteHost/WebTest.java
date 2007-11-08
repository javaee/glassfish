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
        
        InputStream is = sock.getInputStream();
        BufferedReader bis = new BufferedReader(new InputStreamReader(is));

        String line = null;
        String lastLine = null;
        while ((line = bis.readLine()) != null) {
            lastLine = line;
        }

        if (lastLine.startsWith(EXPECTED_RESPONSE)) {
            stat.addStatus(TEST_NAME, stat.PASS);
        } else {
            System.err.println("Wrong response. "
                               + "Expected: " + EXPECTED_RESPONSE
                               + ", received: " + lastLine);
            stat.addStatus(TEST_NAME, stat.FAIL);
        }
    }
}
