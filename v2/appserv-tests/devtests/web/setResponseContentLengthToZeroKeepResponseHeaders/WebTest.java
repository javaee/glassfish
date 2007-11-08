import java.io.*;
import java.net.*;
import java.net.HttpURLConnection;
import com.sun.ejte.ccl.reporter.*;

/*
 * Test case for Bugzilla 32604 ("Some httpHeaders can be lost in response").
 *
 * Make sure that ServletResponse.setContentLength(0) does not close the
 * response, i.e., allow response headers to be added after this call.
 *
 * See http://issues.apache.org/bugzilla/show_bug.cgi?id=32604 for details.
 */
public class WebTest {

    private static final String TEST_NAME = "set-response-content-length-to-zero-keep-response-headers";

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
        stat.addDescription("Unit test for Bugzilla 32604");
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

            if (responseCode != HttpURLConnection.HTTP_OK) {
                System.err.println("Wrong response code. Expected: "
                                   + HttpURLConnection.HTTP_OK
                                   + ", received: " + responseCode);
                stat.addStatus(TEST_NAME, stat.FAIL);
                return;
            }

            if (conn.getHeaderField("A") == null) {
                System.err.println("Missing response header 'A'");
                stat.addStatus(TEST_NAME, stat.FAIL);    
            } else if (conn.getHeaderField("B") == null) {
                System.err.println("Missing response header 'B'");
                stat.addStatus(TEST_NAME, stat.FAIL);    
            } else {
                stat.addStatus(TEST_NAME, stat.PASS);
            }

        } catch( Exception ex) {
            ex.printStackTrace();
            stat.addStatus(TEST_NAME, stat.FAIL);
        }
    }

}
