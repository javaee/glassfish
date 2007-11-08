import java.io.*;
import java.net.*;
import com.sun.ejte.ccl.reporter.*;

/*
 * Unit test for Bugzilla 29971 ("Commented out page directive is parsed").
 */
public class WebTest {

    private static SimpleReporterAdapter stat
        = new SimpleReporterAdapter("appserv-tests");

    private static final String TEST_NAME = "jsp-commented-out-page-directive";
    private static final String EXPECTED = "text/html;charset=ISO-8859-1";

    private String host;
    private String port;
    private String contextRoot;

    public WebTest(String[] args) {
        host = args[0];
        port = args[1];
        contextRoot = args[2];
    }
    
    public static void main(String[] args) {
        stat.addDescription("Unit test for Bugzilla 29971");
        WebTest webTest = new WebTest(args);
        webTest.doTest();
        stat.printSummary(TEST_NAME);
    }

    public void doTest() {     
        try { 
            invokeJsp();
        } catch (Exception ex) {
            stat.addStatus(TEST_NAME, stat.FAIL);
            ex.printStackTrace();
        }
    }

    public void invokeJsp() throws Exception {
     
        String url = "http://" + host + ":" + port + "/" + contextRoot
            + "/jsp/test.jsp";
        HttpURLConnection conn = (HttpURLConnection)
            (new URL(url)).openConnection();

        int code = conn.getResponseCode();
        if (code != 200) {
            System.err.println("Unexpected return code: " + code);
            stat.addStatus(TEST_NAME, stat.FAIL);
        } else {
            if (!EXPECTED.equals(conn.getContentType())) {
                System.err.println("Unknown or invalid response content type");
                stat.addStatus(TEST_NAME, stat.FAIL);
            } else {
                stat.addStatus(TEST_NAME, stat.PASS);
            }
        }    
    }
}
