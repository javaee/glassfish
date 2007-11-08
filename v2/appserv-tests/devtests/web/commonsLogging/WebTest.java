import java.io.*;
import java.net.*;
import com.sun.ejte.ccl.reporter.*;

/*
 * Unit test for CR 6220818 ("Bundling commons-logging.jar in WEB-INF/lib
 * causes exception if classloader delegate flag is set to FALSE")
 */
public class WebTest {

    private static SimpleReporterAdapter stat
        = new SimpleReporterAdapter("appserv-tests");

    private static final String TEST_NAME
        = "commons-logging";

    private String host;
    private String port;
    private String contextRoot;

    public WebTest(String[] args) {
        host = args[0];
        port = args[1];
        contextRoot = args[2];
    }
    
    public static void main(String[] args) {
        stat.addDescription("Unit test for 6220818");
        WebTest webTest = new WebTest(args);
        webTest.doTest();
        stat.printSummary(TEST_NAME);
    }

    public void doTest() {     
        try { 
            invokeServlet();
        } catch (Exception ex) {
            System.out.println(TEST_NAME + " test failed.");
            stat.addStatus(TEST_NAME, stat.FAIL);
            ex.printStackTrace();
        }
    }

    private void invokeServlet() throws Exception {
 
        URL url = new URL("http://" + host + ":" + port + contextRoot
                          + "/From");
        System.out.println("Connecting to: " + url.toString());
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.connect();

        int responseCode = conn.getResponseCode();
        if (responseCode == 200) {
            stat.addStatus(TEST_NAME, stat.PASS);
        } else {
            stat.addStatus("Wrong response code. Expected: 200, received: "
                           + responseCode,
                           stat.FAIL);
        }
    }
}
