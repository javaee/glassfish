import java.io.*;
import java.net.*;
import com.sun.ejte.ccl.reporter.*;

/*
 * Unit test for CR 6240539 ("NPE while using cache-helper")
 */
public class WebTest {

    private static final String TEST_NAME = "cache-helper-ref";

    private static SimpleReporterAdapter stat
        = new SimpleReporterAdapter("appserv-tests");

    private String host;
    private String port;
    private String contextRoot;

    public WebTest(String[] args) {
        host = args[0];
        port = args[1];
        contextRoot = args[2];
    }
    
    public static void main(String[] args) {
        stat.addDescription("Unit test for CR 6240539");
        WebTest webTest = new WebTest(args);
        webTest.doTest();
	stat.printSummary();
    }

    public void doTest() {
     
        try { 
            URL url = new URL("http://" + host  + ":" + port
                              + contextRoot + "/TestServlet");
            System.out.println("Connecting to: " + url.toString());
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.connect();
            int responseCode = conn.getResponseCode();
            if (responseCode != 200) { 
                System.err.println("Wrong response code. Expected: 200, "
                                   + "received: " + responseCode);
                stat.addStatus(TEST_NAME, stat.FAIL);
            } else {
                stat.addStatus(TEST_NAME, stat.PASS);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            stat.addStatus(TEST_NAME, stat.FAIL);
        }
    }

}
