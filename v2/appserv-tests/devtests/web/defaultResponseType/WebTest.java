import java.io.*;
import java.net.*;
import com.sun.ejte.ccl.reporter.*;

/*
 * Unit test for 6328909
 */
public class WebTest {

    private static SimpleReporterAdapter stat =
        new SimpleReporterAdapter("appserv-tests");
    private static final String TEST_NAME = "default-response-type";
    private static final String EXPECTED_CONTENT_TYPE = "test/xml; charset=utf-8";

    private String host;
    private String port;
    private String contextRoot;

    public WebTest(String[] args) {
        host = args[0];
        port = args[1];
        contextRoot = args[2];
    }
    
    public static void main(String[] args) {
        stat.addDescription("defaultResponseType");
        WebTest webTest = new WebTest(args);
        webTest.doTest();
        stat.printSummary(TEST_NAME);
    }

    public void doTest() {     
        try { 
            invoke();
            stat.addStatus(TEST_NAME, stat.PASS);
        } catch (Exception ex) {
            System.out.println(TEST_NAME + " test failed.");
            stat.addStatus(TEST_NAME, stat.FAIL);
            ex.printStackTrace();
        }
    }

    private void invoke() throws Exception {
         
        URL url = new URL("http://" + host  + ":" + port + "/test.xyz");
        System.out.println("Connecting to: " + url.toString());
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.connect();
        int responseCode = conn.getResponseCode();
        if (responseCode != 200) {
            throw new Exception("Wrong response code. Expected: 200" +
                                ", received: " + responseCode);
        }

        String contentType = conn.getHeaderField("Content-Type");
        if (contentType == null ||
                !(EXPECTED_CONTENT_TYPE.equals(contentType))) {
            throw new Exception("Missing or wrong response Content-Type. " +
                                "Expected: " + EXPECTED_CONTENT_TYPE +
                                ", received: " + contentType);
        }
    }
}
