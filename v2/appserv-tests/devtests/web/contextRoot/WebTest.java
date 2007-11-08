import java.io.*;
import java.net.*;
import com.sun.ejte.ccl.reporter.*;

/*
 * Make sure context root ("mycontextroot") specified in sun-web.xml is picked
 * up.
 *
 * For this to work, this unit test does not specify any contextroot argument
 * for deployment.
 */
public class WebTest {

    private static SimpleReporterAdapter stat
        = new SimpleReporterAdapter("appserv-tests");
    private static final String TEST_NAME = "context-root";
    private static final String MY_CONTEXT_ROOT = "mycontextroot";

    private String host;
    private String port;
    private String contextRoot;

    public WebTest(String[] args) {
        host = args[0];
        port = args[1];
        contextRoot = args[2];
    }
    
    public static void main(String[] args) {
        stat.addDescription("Unit test for context-root setting in "
                            + "sun-web.xml");
        WebTest webTest = new WebTest(args);
        webTest.doTest();
        stat.printSummary(TEST_NAME);
    }

    public void doTest() {     
        try { 
            invokeJsp();
        } catch (Exception ex) {
            System.out.println(TEST_NAME + " test failed.");
            stat.addStatus(TEST_NAME, stat.FAIL);
            ex.printStackTrace();
        }
    }

    private void invokeJsp() throws Exception {

        URL url = new URL("http://" + host + ":" + port + "/"
                          + MY_CONTEXT_ROOT + "/jsp/test.jsp");
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
