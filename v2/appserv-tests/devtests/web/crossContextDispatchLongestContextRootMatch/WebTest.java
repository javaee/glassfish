import java.io.*;
import java.net.*;
import com.sun.ejte.ccl.reporter.*;

/*
 * Unit test for:
 *
 *   https://glassfish.dev.java.net/issues/show_bug.cgi?id=3284
 *   ("Contexts and JSTL imports")
 */
public class WebTest {

    private static final String TEST_NAME = "cross-context-dispatch-longest-context-root-match";

    private static final String EXPECTED = "SUCCESS";

    private static SimpleReporterAdapter stat
        = new SimpleReporterAdapter("appserv-tests");

    private String host;
    private String port;
    private String contextRoot;

    public WebTest(String[] args) {
        host = args[0];
        port = args[1];
        contextRoot = args[2];
    }
    
    public static void main(String[] args) {
        stat.addDescription("Unit test for GlassFish Issue 3284");
        WebTest webTest = new WebTest(args);

        try {
            webTest.doTest();
        } catch (Exception ex) {
            ex.printStackTrace();
            stat.addStatus(TEST_NAME, stat.FAIL);
        }

        stat.printSummary();
    }

    public void doTest() throws Exception {
     
        URL url = new URL("http://" + host  + ":" + port + "/123/From");
        System.out.println("Connecting to: " + url.toString());
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.connect();
        int responseCode = conn.getResponseCode();
        if (responseCode != 200) { 
            System.err.println("Wrong response code. Expected: 200"
                               + ", received: " + responseCode);
            stat.addStatus(TEST_NAME, stat.FAIL);
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
                System.err.println("Wrong response body. Could not find "
                                   + "expected string: " + EXPECTED);
                stat.addStatus(TEST_NAME, stat.FAIL);
            } else {
                stat.addStatus(TEST_NAME, stat.PASS);
            }
        }
    }
}
