import java.io.*;
import java.net.*;
import com.sun.ejte.ccl.reporter.*;

/*
 * Unit test for https://glassfish.dev.java.net/issues/show_bug.cgi?id=1607
 * ("MIME extensions not matched in case-insensitive manner")
 */
public class WebTest {

    private static final String TEST_NAME = "mime-mapping-case-insensitive-match";

    private static final String EXPECTED_CONTENT_TYPE = "text/html";

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
        stat.addDescription("Unit test for GlassFish Issue 1607");
        WebTest webTest = new WebTest(args);
        webTest.doTest();
	stat.printSummary();
    }

    public void doTest() {
     
        try { 
            URL url = new URL("http://" + host  + ":" + port
                              + contextRoot + "/foo.HTML");
            System.out.println("Connecting to: " + url.toString());
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.connect();
            int responseCode = conn.getResponseCode();
            if (responseCode != 200) { 
                System.err.println("Wrong response code. Expected: 200"
                                   + ", received: " + responseCode);
                stat.addStatus(TEST_NAME, stat.FAIL);
            } else {
                String contentType = conn.getHeaderField("Content-Type");
                if (EXPECTED_CONTENT_TYPE.equals(contentType)) {
                    stat.addStatus(TEST_NAME, stat.PASS);
                } else {
                    System.err.println("Wrong content type. Expected: "
                                       + EXPECTED_CONTENT_TYPE
                                       + ", received: " + contentType);
                    stat.addStatus(TEST_NAME, stat.FAIL);
                } 
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            stat.addStatus(TEST_NAME, stat.FAIL);
        }
    }

}
