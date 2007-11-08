import java.io.*;
import java.net.*;
import java.security.*;
import javax.net.ssl.*;
import com.sun.ejte.ccl.reporter.*;

/*
 * Unit test for CR 6419659 ("Requests not redirected correcly by the LB
 * Plugin when transport-guarantee is CONFIDENTIAL"):
 *
 * Make sure the redirect-port specified in domain.xml is used in the
 * https redirect URL, ignoring the host and port info specified in the Host
 * request header.
 */
public class WebTest {

    private static SimpleReporterAdapter stat
        = new SimpleReporterAdapter("appserv-tests");

    private static final String TEST_NAME = "redirect-port-loadbalancer";

    private static final String EXPECTED_REDIRECT
        = "Location: https://loadbalancer:12345/web-redirect-port-loadbalancer/index.html";

    private String host;
    private String port;
    private String contextRoot;

    public WebTest(String[] args) {
        host = args[0];
        port = args[1];
        contextRoot = args[2];
    }
    
    public static void main(String[] args) {
        stat.addDescription("Unit test for 6419659");
        WebTest webTest = new WebTest(args);
        try {
            webTest.doTest();
        } catch (Exception ex) {
            stat.addStatus(TEST_NAME, stat.FAIL);
            ex.printStackTrace();
        }
        stat.printSummary(TEST_NAME);
    }

    public void doTest() throws Exception {

        Socket sock = new Socket(host, new Integer(port).intValue());
        OutputStream os = sock.getOutputStream();
        String get = "GET " + contextRoot + "/index.html HTTP/1.1\n";
        System.out.println(get);
        os.write(get.getBytes());
        os.write("Host: loadbalancer:9999\n".getBytes());
        os.write("\n".getBytes());
        
        InputStream is = sock.getInputStream();
        BufferedReader bis = new BufferedReader(new InputStreamReader(is));

        String line = null;
        while ((line = bis.readLine()) != null) {
            if (line.startsWith("Location:")) {
                break;
            }
        }

        if (line != null) {
            System.out.println("Location header: " + line);
            if (EXPECTED_REDIRECT.equals(line)) {
                stat.addStatus(TEST_NAME, stat.PASS);
            } else {
                System.err.println("Wrong redirect. Expected: "
                                   + EXPECTED_REDIRECT + ", received: "
                                   + line);
                stat.addStatus(TEST_NAME, stat.FAIL);
            }
        } else {
            System.err.println("Missing Location response header");
            stat.addStatus(TEST_NAME, stat.FAIL);
        }
    }
}
