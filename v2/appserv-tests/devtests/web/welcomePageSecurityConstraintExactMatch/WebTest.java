import java.io.*;
import java.net.*;
import java.net.HttpURLConnection;
import com.sun.ejte.ccl.reporter.*;

/*
 * Unit test for CR 6233276 ("form auth does not work for url-pattern
 * /*.jsp"):
 *
 * Make sure security constraint with exact match (/index.jsp) url
 * pattern is applied to index.jsp welcome page.
 */
public class WebTest {

    private static final String TEST_NAME = "welcome-page-security-constraint-exact-match";

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
        stat.addDescription("Unit test for CR 6233276");
        WebTest webTest = new WebTest(args);
        webTest.doTest();
	stat.printSummary();
    }

    public void doTest() {
     
        try {

            URL url = new URL("http://" + host  + ":" + port
                              + contextRoot + "/");
            System.out.println("Connecting to: " + url.toString());
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.connect();
            int responseCode = conn.getResponseCode();

            if (responseCode == HttpURLConnection.HTTP_UNAUTHORIZED) {
                stat.addStatus(TEST_NAME, stat.PASS);
            } else {
                System.err.println("Wrong response code. Expected: "
                                   + HttpURLConnection.HTTP_UNAUTHORIZED
                                   + ", received: " + responseCode);
                stat.addStatus(TEST_NAME, stat.FAIL);
            }

        } catch( Exception ex) {
            ex.printStackTrace();
            stat.addStatus(TEST_NAME, stat.FAIL);
        }
    }

}
