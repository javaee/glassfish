import java.io.*;
import java.net.*;
import com.sun.ejte.ccl.reporter.*;

/*
 * Unit test for the spec requirement that if there is no AsyncListener
 * registered and an async timeout occurs, the container MUST do an
 * ERROR dispatch to the original URI with a response code of 500.
 */
public class WebTest {

    private static final String TEST_NAME = "async-timeout-no-listener";

    private static SimpleReporterAdapter stat =
        new SimpleReporterAdapter("appserv-tests");

    private static final int EXPECTED_RESPONSE_CODE = 500;
   
    private String host;
    private String port;
    private String contextRoot;

    public WebTest(String[] args) {
        host = args[0];
        port = args[1];
        contextRoot = args[2];
    }
    
    public static void main(String[] args) {

        stat.addDescription("Unit test for async timeout without " +
                            "any registered AsyncListener");
        WebTest webTest = new WebTest(args);
        try {
            webTest.doTest();
            stat.addStatus(TEST_NAME, stat.PASS);
        } catch (Exception ex) {
            ex.printStackTrace();
            stat.addStatus(TEST_NAME, stat.FAIL);
        }

	stat.printSummary();
    }

    public void doTest() throws Exception {
     
        URL url = new URL("http://" + host  + ":" + port
                          + contextRoot + "/TestServlet");
        System.out.println("Connecting to: " + url.toString());

        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setReadTimeout(20 * 1000);
        conn.connect();
        int responseCode = conn.getResponseCode();
        if (responseCode != EXPECTED_RESPONSE_CODE) {
            throw new Exception("Unexpected return code: " + responseCode +
                                ", expected: " + EXPECTED_RESPONSE_CODE);
        }
    }
}
