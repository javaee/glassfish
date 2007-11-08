import java.io.*;
import java.net.*;
import com.sun.ejte.ccl.reporter.*;

/**
 * Unit test for:
 *
 *   https://glassfish.dev.java.net/issues/show_bug.cgi?id=3398
 *   ("Access logging may not be turned on or off dynamically, requires
 *   server restart to take effect")
 */
public class WebTest {

    private static SimpleReporterAdapter stat
        = new SimpleReporterAdapter("appserv-tests");

    private static final String TEST_NAME = "access-logging-dynamic-reconfig";

    private static final String EXPECTED = "SUCCESS!";

    private String host;
    private String port;
    private String contextRoot;
    private String location;

    public WebTest(String[] args) {
        host = args[0];
        port = args[1];
        contextRoot = args[2];
        location = args[3];
    }
    
    public static void main(String[] args) {
        stat.addDescription("Unit test for GlassFish Issue 3398");
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
        
        URL url = new URL("http://" + host  + ":" + port + contextRoot
                          + "/CheckAccessLog?location="
                          + URLEncoder.encode(location));
        System.out.println("Connecting to: " + url.toString());
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.connect();
        int responseCode = conn.getResponseCode();
        if (responseCode != 200) { 
            throw new Exception("Wrong response code. Expected: 200"
                                + ", received: " + responseCode);
        } else {
            BufferedReader bis = new BufferedReader(
                new InputStreamReader(conn.getInputStream()));
            String line = null;
            while ((line = bis.readLine()) != null) {
                if (EXPECTED.equals(line)) {
                    break;
                }
            }
            if (line == null) {
                throw new Exception("Wrong response body. Could not find "
                                    + "expected string: " + EXPECTED);
            }
        }
    }
}
