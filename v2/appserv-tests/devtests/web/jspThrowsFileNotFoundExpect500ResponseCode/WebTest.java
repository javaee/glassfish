import java.io.*;
import java.net.*;
import com.sun.ejte.ccl.reporter.*;

/*
 * Unit test for 4928358 ("JSP errors are wrongly reported as Not Found
 * errors"):
 *
 * Make sure that if a JSP page throws a FileNotFoundException, the
 * resulting response code is 500 instead of 404 (404 would be interpreted
 * as if the JSP itself did not exist).
 */
public class WebTest {

    private static final String TEST_NAME =
        "jsp-throws-file-not-found-expect-500-response-code";

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
        stat.addDescription("Unit test for 4928358");
        WebTest webTest = new WebTest(args);
        try {
            webTest.doTest();
        } catch (Exception ex) {
            stat.addStatus(TEST_NAME, stat.FAIL);
            ex.printStackTrace();
        }
	stat.printSummary();
    }

    public void doTest() throws Exception {
     
        URL url = new URL("http://" + host  + ":" + port
                          + contextRoot + "/jsp/notfound.jsp");
        System.out.println("Connecting to: " + url.toString());
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.connect();
        int responseCode = conn.getResponseCode();
        if (responseCode == 500) { 
            stat.addStatus(TEST_NAME, stat.PASS);
        } else {
            System.err.println("Wrong response code. Expected: 500"
                               + ", received: " + responseCode);
            stat.addStatus(TEST_NAME, stat.FAIL);
        }
    }
}
