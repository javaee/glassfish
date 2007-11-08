import java.io.*;
import java.net.*;
import com.sun.ejte.ccl.reporter.*;

/*
 * Unit test for 6182984 ("<jsp:include page="foo.bar"> does not return error
 * 404 for SJSAS 8.1 PE").
 */
public class WebTest {

    private static SimpleReporterAdapter stat
        = new SimpleReporterAdapter("appserv-tests");

    private static final String TEST_NAME
        = "request-dispatcher-include-target-not-found";

    private String host;
    private String port;
    private String contextRoot;

    public WebTest(String[] args) {
        host = args[0];
        port = args[1];
        contextRoot = args[2];
    }
    
    public static void main(String[] args) {
        stat.addDescription("Unit test for 6182984");
        WebTest webTest = new WebTest(args);
        try {
            webTest.doTest();
        } catch (Exception ex) {
            stat.addStatus(TEST_NAME, stat.FAIL);
            ex.printStackTrace();
        }

        stat.printSummary(TEST_NAME);
    }

    public void doTest() throws Exception {
     
        URL url = new URL("http://" + host  + ":" + port
                          + contextRoot + "/jsp/include.jsp");
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
