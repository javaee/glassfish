import java.io.*;
import java.net.*;
import com.sun.ejte.ccl.reporter.*;

/**
 * Unit test for dynamic reconfiguration of default-web-module attribute of
 * <virtual-server>.
 *
 * This test sets the default-web-module of the virtual server "server" to
 * the webapp deployed by this unit test, and ensures that accessing
 * "/index.html" (with the empty context path) returns the contents of the
 * webapp's index.html resource instead of the GlassFish welcome page.
 */
public class WebTest {

    private static SimpleReporterAdapter stat
        = new SimpleReporterAdapter("appserv-tests");

    private static final String TEST_NAME = "virtual-server-default-web-module";

    private String host;
    private String port;
    private String contextRoot;

    public WebTest(String[] args) {
        host = args[0];
        port = args[1];
        contextRoot = args[2];
    }
    
    public static void main(String[] args) {
        stat.addDescription("Unit test for virtual server default-web-module");
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
            if(line.contains(expected)) {
                break;
            }
        }

        if (line == null) {
            throw new Exception("Missing content");
        }
    }
}
