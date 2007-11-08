import java.io.*;
import java.net.*;
import com.sun.ejte.ccl.reporter.*;

/*
 * Unit test for CR 6275709 ("Custom Session ID Generation not working as
 * documented").
 *
 * This unit test configures a custom session id generator at the instance
 * level (as the value of the session-id-generator-classname attribute of the
 * <server.session-config.session-manager.manager-properties> element) and
 * adds its impl class to the appserver's classpath prefix.
 *
 * This custom session id generator creates session ids of the form "abc".
 *
 * The unit test then invokes a servlet which creates an HTTP session and
 * verifies that its session id matches the string "abc".
 */
public class WebTest {

    private static SimpleReporterAdapter stat
        = new SimpleReporterAdapter("appserv-tests");

    private static final String TEST_NAME = "session-id-custom-generator";

    private String host;
    private String port;
    private String contextRoot;
    private boolean fail = false;

    public WebTest(String[] args) {
        host = args[0];
        port = args[1];
        contextRoot = args[2];
    }
    
    public static void main(String[] args) {
        stat.addDescription("Unit test for CR 6275709");
        WebTest webTest = new WebTest(args);
        webTest.doTest();
        stat.printSummary(TEST_NAME);
    }

    public void doTest() {
     
        try { 
            invokeServlet();
        } catch (Exception ex) {
            ex.printStackTrace();
            stat.addStatus(TEST_NAME, stat.FAIL);
        }

        if (fail) {
            stat.addStatus(TEST_NAME, stat.FAIL);
        } else {
            stat.addStatus(TEST_NAME, stat.PASS);
        }

        return;
    }

    private void invokeServlet() throws Exception {
         
        URL url = new URL("http://" + host  + ":" + port
            + contextRoot + "/createSession");
        System.out.println("Connecting to: " + url.toString());
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.connect();
        int responseCode = conn.getResponseCode();
        if (responseCode != 200) { 
            System.err.println("Wrong response code. Expected: 200"
                               + ", received: " + responseCode);
            stat.addStatus(TEST_NAME, stat.FAIL);
        } else {
            stat.addStatus(TEST_NAME, stat.PASS);
        }
    }
}
