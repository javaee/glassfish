import java.io.*;
import java.net.*;
import com.sun.ejte.ccl.reporter.*;

/*
 * Unit test for 6495991 ("Classloader.getResource() for JAR resource
 * returns invalid URL"):
 *
 * Make sure that the URL returned by a call to the webapp classloader's
 * getResource("/../lib/test.jar") is valid, i.e., can be opened
 */
public class WebTest {

    private static final String TEST_NAME = "classloader-get-resource-jar-file";

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

        stat.addDescription("Unit test for 6495991");
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
     
        URL url = new URL("http://" + host  + ":" + port + contextRoot + "/test.jsp");
        System.out.println("Connecting to: " + url.toString());

        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.connect();
        int responseCode = conn.getResponseCode();

        if (responseCode != 200) {
            System.err.println("Unexpected response code: " + responseCode);
            stat.addStatus(TEST_NAME, stat.FAIL);
        } else {
            stat.addStatus(TEST_NAME, stat.PASS);
        }  
    }
}
