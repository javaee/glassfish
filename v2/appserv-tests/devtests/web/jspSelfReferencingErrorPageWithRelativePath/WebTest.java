import java.io.*;
import java.net.*;
import com.sun.ejte.ccl.reporter.*;

/*
 * Unit test for detecting self-referencing error page and throwing compilation
 * error.
 */
public class WebTest {

    private static SimpleReporterAdapter stat
        = new SimpleReporterAdapter("appserv-tests");

    private static final String TEST_NAME
        = "jsp-self-referencing-error-page-with-relative-path";

    private String host;
    private String port;
    private String contextRoot;

    public WebTest(String[] args) {
        host = args[0];
        port = args[1];
        contextRoot = args[2];
    }
    
    public static void main(String[] args) {
        stat.addDescription(
            "Unit test for detecting self-referencing error page");
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
     
        String url = "http://" + host + ":" + port + contextRoot
            + "/jsp/test.jsp";
        HttpURLConnection conn = (HttpURLConnection)
            (new URL(url)).openConnection();

        int code = conn.getResponseCode();
        if (code != 500) {
            System.err.println("Unexpected return code: " + code);
            stat.addStatus(TEST_NAME, stat.FAIL);
        } else {
            stat.addStatus(TEST_NAME, stat.PASS);
        }    
    }
}
