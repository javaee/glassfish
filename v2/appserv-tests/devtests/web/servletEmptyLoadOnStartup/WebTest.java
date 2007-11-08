import java.io.*;
import java.net.*;
import com.sun.ejte.ccl.reporter.*;

/*
 * Unit test for 6172006 ("Unable to load servlet with empty
 * <load-on-startup> element"):
 *
 * Make sure empty load-on-startup element no longer causes any schema
 * validation errors when using a Servlet 2.5 based web.xml. This used to
 * fail when using a Servlet 2.4 based web.xml.
 */
public class WebTest {

    private static SimpleReporterAdapter stat
        = new SimpleReporterAdapter("appserv-tests");

    private static final String TEST_NAME = "servlet-empty-load-on-startup";

    private static final String EXPECTED_RESPONSE = "PASSED";

    private String host;
    private String port;
    private String contextRoot;

    public WebTest(String[] args) {
        host = args[0];
        port = args[1];
        contextRoot = args[2];
    }
    
    public static void main(String[] args) {
        stat.addDescription("Unit test for 6172006");
        WebTest webTest = new WebTest(args);
        webTest.doTest();
        stat.printSummary(TEST_NAME);
    }

    public void doTest() {     
        try { 
            run();
        } catch (Exception ex) {
            stat.addStatus(TEST_NAME, stat.FAIL);
            ex.printStackTrace();
        }
    }

    public void run() throws Exception {
     
        String url = "http://" + host + ":" + port + contextRoot
                    + "/TestServlet";
        HttpURLConnection conn = (HttpURLConnection)
            (new URL(url)).openConnection();

        int code = conn.getResponseCode();
        if (code != 200) {
            System.err.println("Unexpected return code: " + code);
            stat.addStatus(TEST_NAME, stat.FAIL);
        } else {
            InputStream is = conn.getInputStream();
            BufferedReader input = new BufferedReader(new InputStreamReader(is));
            String line = input.readLine();
            if (!EXPECTED_RESPONSE.equals(line)) {
                System.err.println("Wrong response. "
                                   + "Expected: " + EXPECTED_RESPONSE
                                   + ", received: " + line);
                stat.addStatus(TEST_NAME, stat.FAIL);
            } else {
                stat.addStatus(TEST_NAME, stat.PASS);
            }
        }    
    }
}
