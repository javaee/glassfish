import java.io.*;
import java.net.*;
import com.sun.ejte.ccl.reporter.*;

/*
 * Unit test for 6269102 ("SSL termination is not working, Appserver replaces
 * the https to http during redirection").
 *
 * This test
 *
 * - sets the authPassthroughEnabled property of http-listener-1 to TRUE [1],
 * - configures a server-name attribute for http-listener-1 with a value of
 *   https://lbhost:8888 [2],
 * - includes a 'Proxy-keysize' header with a value > 0 in the request [3],
 *
 * and expects that the host name and port# of the server-name attribute be
 * reflected in the Location response header, along with an https scheme
 * (due to [1] and [3] above). The https scheme in the server-name attribute
 * is ignored in this case.
 */
public class WebTest {

    private static SimpleReporterAdapter stat
        = new SimpleReporterAdapter("appserv-tests");

    private static final String EXPECTED_LOCATION = 
        "https://lbhost:8888/jsp/redirect/target.jsp";

    private static final String TEST_NAME =
        "auth-passthrough-send-redirect-from-jsp-with-https-server-name";

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
        stat.addDescription("Unit test for 6269102");
        WebTest webTest = new WebTest(args);
        webTest.doTest();
        stat.printSummary(TEST_NAME);
    }

    public void doTest() {
     
        try { 
            testRemoteAddress();
            stat.addStatus(TEST_NAME, stat.PASS);
        } catch (Exception ex) {
            ex.printStackTrace();
            stat.addStatus(TEST_NAME, stat.FAIL);
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

    private void testRemoteAddress() throws Exception {
         
        sock = new Socket(host, new Integer(port).intValue());
        OutputStream os = sock.getOutputStream();
        String get = "GET " + contextRoot + "/jsp/sendRedirect.jsp "
            + "HTTP/1.0\n";
        System.out.println(get);
        os.write(get.getBytes());
        os.write("Proxy-keysize: 512\n".getBytes());
        os.write("Proxy-ip: 123.456.789\n".getBytes());
        os.write("\n".getBytes());
        
        InputStream is = null;
        BufferedReader bis = null;
        String line = null;
        try {
            is = sock.getInputStream();
            bis = new BufferedReader(new InputStreamReader(is));
            while ((line = bis.readLine()) != null) {
                if (line.startsWith("Location:")) {
                    break;
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

        if (line == null) {
            throw new Exception("Missing Location response header");
        }

        System.out.println("Location header: " + line);
        String location = line.substring("Location:".length()).trim();
        if (!EXPECTED_LOCATION.equals(location)) {
            throw new Exception("Wrong Location response header, expected: "
                                + EXPECTED_LOCATION
                                + ", received: " + location);
        }
    }
}
