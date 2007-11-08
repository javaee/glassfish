import java.io.*;
import java.net.*;
import com.sun.ejte.ccl.reporter.*;

/*
 * Unit test for <servlet-mapping> with multiple <url-pattern> subelements
 * (Servlet 2.5 feature).
 */
public class WebTest {

    private static final String TEST_NAME =
        "servlet-mapping-with-multiple-url-patterns";

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

        stat.addDescription("Unit test for servlet-mapping with "
                            + "multiple url-pattern subelements");
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
     
        boolean fail = false;
        URL url = null;
        HttpURLConnection conn = null;
        int responseCode = -1;

        url = new URL("http://" + host  + ":" + port
                      + contextRoot + "/Servlet_1");
        System.out.println("Connecting to: " + url.toString());
        conn = (HttpURLConnection) url.openConnection();
        conn.connect();
        responseCode = conn.getResponseCode();
        if (responseCode != 200) {
            fail = true;
        }

        url = new URL("http://" + host  + ":" + port
                      + contextRoot + "/Servlet_2");
        System.out.println("Connecting to: " + url.toString());
        conn = (HttpURLConnection) url.openConnection();
        conn.connect();
        responseCode = conn.getResponseCode();
        if (responseCode != 200) {
            fail = true;
        }

        if (fail) {
            System.err.println("Unexpected return code: " + responseCode);
            stat.addStatus(TEST_NAME, stat.FAIL);
        } else {
            stat.addStatus(TEST_NAME, stat.PASS);
        }    
    }
}
