import java.io.*;
import java.net.*;
import com.sun.ejte.ccl.reporter.*;

/*
 * Unit test for @WebFilter
 */
public class WebTest {

    private static SimpleReporterAdapter stat
        = new SimpleReporterAdapter("appserv-tests");
    private static final String TEST_NAME = "servlet-filter-annotation";
    private static final String EXPECTED_RESPONSE = "filterMessage=my filter";

    private String host;
    private String port;
    private String contextRoot;

    public WebTest(String[] args) {
        host = args[0];
        port = args[1];
        contextRoot = args[2];
    }
    
    public static void main(String[] args) {
        stat.addDescription("Unit test for @WebFilter");
        WebTest webTest = new WebTest(args);
        webTest.doTest();
        stat.printSummary(TEST_NAME);
    }

    public void doTest() {
        try { 
            invoke();
        } catch (Exception ex) {
            System.out.println(TEST_NAME + " test failed");
            stat.addStatus(TEST_NAME, stat.FAIL);
            ex.printStackTrace();
        }
    }

    private void invoke() throws Exception {
        
        String url = "http://" + host + ":" + port + contextRoot
                     + "/mytest";
        HttpURLConnection conn = (HttpURLConnection)
            (new URL(url)).openConnection();

        int code = conn.getResponseCode();
        if (code != 200) {
            System.out.println("Unexpected return code: " + code);
            stat.addStatus(TEST_NAME, stat.FAIL);
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
                stat.addStatus(TEST_NAME, stat.PASS);
            } else {
                System.out.println("Wrong response. Expected: " + 
                        EXPECTED_RESPONSE + ", received: " + line);
                stat.addStatus(TEST_NAME, stat.FAIL);
            }
        }    
    }
}
