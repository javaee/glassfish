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
    private static final String EXPECTED_RESPONSE_2 = "min=2";

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
        try { 
            invoke("http://" + host + ":" + port + contextRoot + "/mytest",
                   EXPECTED_RESPONSE);
            invoke("http://" + host + ":" + port + contextRoot + "/wftest",
                   EXPECTED_RESPONSE);
            invoke("http://" + host + ":" + port + contextRoot + "/wftest2",
                   EXPECTED_RESPONSE_2);
            stat.addStatus(TEST_NAME, stat.PASS);
        } catch (Exception ex) {
            stat.addStatus(TEST_NAME, stat.FAIL);
            ex.printStackTrace();
        }
    }

    private void invoke(String url, String expectedResponse) throws Exception {
        
        HttpURLConnection conn = (HttpURLConnection)
            (new URL(url)).openConnection();
        conn.connect();

        int code = conn.getResponseCode();
        if (code != 200) {
            throw new Exception("Unexpected return code: " + code);
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
