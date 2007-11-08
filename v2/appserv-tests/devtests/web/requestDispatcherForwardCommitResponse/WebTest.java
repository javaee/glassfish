import java.io.*;
import java.net.*;
import com.sun.ejte.ccl.reporter.*;

/**
 * Unit test for CR 6374990 ("Response is not flushed to browser on
 * RequestDispatcher.forward()")
 */
public class WebTest {

    private static SimpleReporterAdapter stat
        = new SimpleReporterAdapter("appserv-tests");

    private static final String TEST_NAME
        = "request-dispatcher-forward-commit-response";

    private static final String EXPECTED_RESPONSE
        = "Message from target servlet";

    private String host;
    private String port;
    private String contextRoot;

    public WebTest(String[] args) {
        host = args[0];
        port = args[1];
        contextRoot = args[2];
    }
    
    public static void main(String[] args) {
        stat.addDescription("Unit test for 6374990");
        WebTest webTest = new WebTest(args);
        try {
            webTest.doTest();
        } catch (Exception ex) {
            stat.addStatus(TEST_NAME, stat.FAIL);
            ex.printStackTrace();
        }

        stat.printSummary(TEST_NAME);

        // Wait until the request has returned, to avoid undeploying this
        // test application prematurely.
        try {
            Thread.currentThread().sleep(10 * 1000);
        } catch (Exception e) {
            // Ignore
        }
    }

    public void doTest() throws Exception {

        URL url = new URL("http://" + host  + ":" + port
                          + contextRoot + "/From");
        System.out.println("Connecting to: " + url.toString());

        long start = System.currentTimeMillis();
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.connect();
        int responseCode = conn.getResponseCode();

        if (responseCode != 200) {
            System.err.println("Unexpected return code: " + responseCode);
            stat.addStatus(TEST_NAME, stat.FAIL);
        } else {
            InputStream is = conn.getInputStream();
            BufferedReader input = new BufferedReader(new InputStreamReader(is));
            String line = input.readLine();
            long end = System.currentTimeMillis();

            if (EXPECTED_RESPONSE.equals(line)) {
                if ((end-start) < (10*1000)) {
                    stat.addStatus(TEST_NAME, stat.PASS);
		} else {
                    System.err.println("Response was delayed by 10 seconds "
                                       + "or more, which is how long the "
                                       + "origin servlet of the RD.forward() "
                                       + "has been sleeping for.");
                    System.err.println("The response should have been "
                                       + "committed immediately.");
                    stat.addStatus(TEST_NAME, stat.FAIL);
                }
            } else {
                System.err.println("Wrong response. Expected: "
                                   + EXPECTED_RESPONSE
                                   + ", received: " + line);
                stat.addStatus(TEST_NAME, stat.FAIL);
            }
        }    
    }
}
