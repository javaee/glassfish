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
                    TEST_NAME + "_urlPatternfromWeb");
            invoke("http://" + host + ":" + port + contextRoot + "/wftest",
                    TEST_NAME + "_urlPatternfomWebFragment");
        } catch (Exception ex) {
            System.out.println(TEST_NAME + " test failed");
            stat.addStatus(TEST_NAME, stat.FAIL);
            ex.printStackTrace();
        }
    }

    private void invoke(String url, String testName) throws Exception {
        
        HttpURLConnection conn = (HttpURLConnection)
            (new URL(url)).openConnection();

        int code = conn.getResponseCode();
        if (code != 200) {
            System.out.println("Unexpected return code: " + code);
            stat.addStatus(testName, stat.FAIL);
        } else {
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
            if (EXPECTED_RESPONSE.equals(line)) {
                stat.addStatus(testName, stat.PASS);
            } else {
                System.out.println("Wrong response. Expected: " + 
                        EXPECTED_RESPONSE + ", received: " + line);
                stat.addStatus(testName, stat.FAIL);
            }
        }    
    }
}
