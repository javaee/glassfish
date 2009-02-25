import java.io.*;
import java.net.*;
import com.sun.ejte.ccl.reporter.*;

/*
 * Unit test to make sure that AsyncListener#onTimeout is called after the
 * specified async timeout interval.
 */
public class WebTest {

    private static final String TEST_NAME = "async-listener-on-timeout";

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

        stat.addDescription("Unit test for AsyncListener#onTimeout");
        WebTest webTest = new WebTest(args);

        try {
            webTest.doTest("wrap=true");
            webTest.doTest("wrap=false");
            stat.addStatus(TEST_NAME, stat.PASS);
        } catch (Exception ex) {
            ex.printStackTrace();
            stat.addStatus(TEST_NAME, stat.FAIL);
        }

	stat.printSummary();
    }

    public void doTest(String mode) throws Exception {
     
        URL url = new URL("http://" + host  + ":" + port
                          + contextRoot + "/TestServlet?" + mode);
        System.out.println("Connecting to: " + url.toString());

        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setConnectTimeout(15 * 1000);
        conn.connect();
        int responseCode = conn.getResponseCode();

        if (responseCode != 200) {
            throw new Exception("Unexpected return code: " + responseCode);
        }
    }
}
