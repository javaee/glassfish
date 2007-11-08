import java.io.*;
import java.net.*;
import com.sun.ejte.ccl.reporter.*;

/*
 * Unit test for BOM (in JSP classic syntax) encoding mismatch:
 *
 * This test enforces that a translation error be reported if the page
 * encoding identified by a BOM (in this case: UTF-8) does not match the
 * page encoding specified via 'pageEncoding' page directive attribute
 * (in this case: UTF-16BE).
 */
public class WebTest {

    private static final String TEST_NAME
        = "jsp-bom-in-classic-syntax-encoding-mismatch-pagedir";

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

        stat.addDescription("Unit test for BOM encoding mismatch");
        WebTest webTest = new WebTest(args);

        try {
            webTest.doTest();
        } catch (Exception ex) {
            ex.printStackTrace();
            stat.addStatus(TEST_NAME, stat.FAIL);
        }

	stat.printSummary();
    }

    public void doTest() throws Exception {
     
        URL url = new URL("http://" + host  + ":" + port
                          + contextRoot + "/encodingMismatch.jsp");
        System.out.println("Connecting to: " + url.toString());
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.connect();

        int responseCode = conn.getResponseCode();
        if (responseCode != 500) { 
            System.err.println("Wrong response code. Expected: 500"
                               + ", received: " + responseCode);
            stat.addStatus(TEST_NAME, stat.FAIL);
        } else {
            stat.addStatus(TEST_NAME, stat.PASS);
        }
    }
}
