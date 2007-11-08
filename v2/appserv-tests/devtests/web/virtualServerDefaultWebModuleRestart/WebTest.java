import java.io.*;
import java.net.*;
import com.sun.ejte.ccl.reporter.*;

/**
 * Unit test for 6526113 ("virtual server throws bad request for static
 * docs when default webmodule is set"):
 *
 * This test:
 *
 * - deploys a webapp;
 * - creates a new virtual server and has its default-web-module attribute
 *   point to the deployed webapp;
 * - creates a new http-listener, and has its default-virtual-server attribute
 *   point to the newly created virtual server;
 * - restarts the domain;
 * - accesses the new virtual server, and ensures that its default-web-module
 *   is honored
 *
 * This test requires a restart, because when the webapp designated as the
 * default-web-module of the new virtual server is first deployed, it
 * is only deployed to the virtual servers available at the time (i.e., to the
 * virtual server with id "server"). It will be deployed to the newly
 * created virtual server only during a restart.
 */
public class WebTest {

    private static SimpleReporterAdapter stat
        = new SimpleReporterAdapter("appserv-tests");

    private static final String TEST_NAME = "virtual-server-default-web-module-restart";

    private String host;
    private String port;

    public WebTest(String[] args) {
        host = args[0];
        port = args[1];
    }
    
    public static void main(String[] args) {
        stat.addDescription("Unit test for 6526113");
        WebTest webTest = new WebTest(args);
        webTest.doTest();
        stat.printSummary(TEST_NAME);
    }

    public void doTest() {     
        try { 
            invoke();
            stat.addStatus(TEST_NAME, stat.PASS);
        } catch (Exception ex) {
            stat.addStatus(TEST_NAME, stat.FAIL);
            ex.printStackTrace();
        }
    }

    private void invoke() throws Exception {
        
        URL url = new URL("http://" + host  + ":" + port + "/index.html");
        System.out.println("Connecting to: " + url.toString());
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.connect();

        int responseCode = conn.getResponseCode();
        if (responseCode != 200) {
            throw new Exception("Wrong response code. Expected: 200"
                                + ", received: " + responseCode);
        }

        InputStream is = conn.getInputStream();
        BufferedReader input = new BufferedReader(new InputStreamReader(is));
        String line = null;
        String expected = "This is my personal welcome page!";
        while ((line = input.readLine()) != null) {
            if (line.contains(expected)) {
                break;
            }
        }

        if (line == null) {
            throw new Exception("Missing content");
        }
    }
}
