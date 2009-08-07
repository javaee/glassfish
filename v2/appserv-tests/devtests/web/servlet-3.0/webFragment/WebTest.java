import java.io.*;
import java.net.*;
import com.sun.ejte.ccl.reporter.*;

/*
 * Unit test for web.xml fragments
 */
public class WebTest {

    private static SimpleReporterAdapter stat
        = new SimpleReporterAdapter("appserv-tests");
    private static final String TEST_NAME = "web-fragment";
    private static final String EXPECTED_RESPONSE = "filterMessage=WFTestFilterMesg, mesg=hello t, mesg2=hello2 f, mesg3=hello3 a";
    private static final String EXPECTED_RESPONSE_2 = "min=2, mid=10, max=20";
    private static final String EXPECTED_RESPONSE_3 = "min=2, max=20";

    private String host;
    private String port;
    private String contextRoot;

    public WebTest(String[] args) {
        host = args[0];
        port = args[1];
        contextRoot = args[2];
    }
    
    public static void main(String[] args) {
        stat.addDescription("Unit test for web fragment");
        WebTest webTest = new WebTest(args);
        webTest.doTest();
        stat.printSummary(TEST_NAME);
    }

    public void doTest() {

        runTest(TEST_NAME + "_urlPatternFromWeb", "/mytest", 200, EXPECTED_RESPONSE);

        runTest(TEST_NAME + "_urlPatternFromWebFragment", "/wftest", 404, null);

        runTest(TEST_NAME + "_envEntryFromWebFragment", "/wftest2", 200, EXPECTED_RESPONSE_2);

        runTest(TEST_NAME + "_envEntryFromWeb", "/mytest2", 200, EXPECTED_RESPONSE_3);
    }

    private void runTest(String testName, String urlPattern, int statusCode, String expectedResponse) {
        try {
            invoke(urlPattern, statusCode, expectedResponse);
            stat.addStatus(testName, stat.PASS);
        } catch (Exception ex) {
            stat.addStatus(testName, stat.FAIL);
            ex.printStackTrace();
        }
    }

    private void invoke(String urlPattern, int statusCode, String expectedResponse) throws Exception {
        
        String url = "http://" + host + ":" + port + contextRoot + urlPattern;
        HttpURLConnection conn = (HttpURLConnection)
            (new URL(url)).openConnection();
        conn.connect();

        int code = conn.getResponseCode();
        if (code != statusCode) {
            throw new Exception("Unexpected return code: " + code);
        }

        if (expectedResponse == null) {
            return;
        }

        InputStream is = null;
        BufferedReader input = null;
        String line = null;
        try {
            is = conn.getInputStream();
            input = new BufferedReader(new InputStreamReader(is));
            line = input.readLine();
        } finally {
            try {
                if (is != null) {
                    is.close();
                }
            } catch(IOException ioe) {
                // ignore
            }
            try {
                if (input != null) {
                    input.close();
                }
            } catch(IOException ioe) {
                // ignore
            }
        }
        if (!expectedResponse.equals(line)) {
            throw new Exception("Wrong response. Expected: " + 
                expectedResponse + ", received: " + line);
        }
    }
}
