import java.io.*;
import java.net.*;
import com.sun.ejte.ccl.reporter.*;

/*
 * Unit test for Bugtraq 6021119:
 * Ensure that ServletRequest.getServerName() and
 * ServletRequest.getServerPort() return the server name and port,
 * respectively, specified in the Host request header.
 */
public class WebTest {

    private static SimpleReporterAdapter stat
        = new SimpleReporterAdapter("appserv-tests");

    private static final String TEST_NAME = "get-server-name-and-port";
    private static final String SERVER_NAME = "ultra.india.sun.com";
    private static final String SERVER_PORT = "8888";
    private static final String EXPECTED =
        "ServerName=" + SERVER_NAME + ",ServerPort=" + SERVER_PORT;

    private String host;
    private String port;
    private String contextRoot;
    private Socket sock = null;

    public WebTest(String[] args) {
        host = args[0];
        port = args[1];
        contextRoot = args[2];
    }
    
    public static void main(String[] args) {
        stat.addDescription("Unit test for 6021119");
        WebTest webTest = new WebTest(args);
        webTest.doTest();
        stat.printSummary(TEST_NAME);
    }

    public void doTest() {     
        try { 
            invokeServlet();
        } catch (Exception ex) {
            System.out.println(TEST_NAME + " test failed.");
            stat.addStatus(TEST_NAME, stat.FAIL);
            ex.printStackTrace();
        } finally {
            try {
                if (sock != null) {
                    sock.close();
                }
            } catch (IOException ioe) {
                // ignore
            }
        }
    }

    private void invokeServlet() throws Exception {

        sock = new Socket(host, new Integer(port).intValue());
        OutputStream os = sock.getOutputStream();
        os.write(("GET " + contextRoot + "/TestServlet HTTP/1.1\n").getBytes());
        os.write(("Host: " + SERVER_NAME + ":" + SERVER_PORT + "\n").getBytes());
        os.write("\n".getBytes());
        
        InputStream is = null;
        BufferedReader bis = null;
        try {
            is = sock.getInputStream();
            bis = new BufferedReader(new InputStreamReader(is));
            String line = null;
            int i = 0;;
            while ((line = bis.readLine()) != null) {
                System.out.println(i++ + ": " + line);
                if (EXPECTED.equals(line)) {
                    stat.addStatus(TEST_NAME, stat.PASS);
                    return;
                }
            }
        } finally {
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

        stat.addStatus(TEST_NAME, stat.FAIL);
        System.out.println("Expected: " + EXPECTED);
    }
}
