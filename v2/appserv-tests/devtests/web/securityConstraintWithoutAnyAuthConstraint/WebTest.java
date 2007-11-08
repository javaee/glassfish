import java.io.*;
import java.net.*;
import com.sun.ejte.ccl.reporter.*;

/*
 * Unit test for 6264656 ("appserver fails to handle security constraint
 * without an auth-constraint")
 */
public class WebTest {

    private static SimpleReporterAdapter stat
        = new SimpleReporterAdapter("appserv-tests");

    private static final String TEST_NAME =
        "security-constraint-without-any-auth-constraint";

    private static final String EXPECTED_RESPONSE = "true";

    private String host;
    private String port;
    private String contextRoot;

    public WebTest(String[] args) {
        host = args[0];
        port = args[1];
        contextRoot = args[2];
    }
    
    public static void main(String[] args) {
        stat.addDescription("Unit test for 6264656");
        WebTest webTest = new WebTest(args);
        webTest.doTest();
        stat.printSummary(TEST_NAME);
    }

    public void doTest() {
     
        try { 
            invoke();
        } catch (Exception ex) {
            System.out.println(TEST_NAME + " test failed");
            ex.printStackTrace();
            stat.addStatus(TEST_NAME, stat.FAIL);
        }
    }

    private void invoke() throws Exception {

        URL url = new URL("http://" + host  + ":" + port + contextRoot
                          + "/test.jsp");
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
