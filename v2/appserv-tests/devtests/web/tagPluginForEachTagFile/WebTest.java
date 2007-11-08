import java.lang.*;
import java.io.*;
import java.net.*;

import com.sun.ejte.ccl.reporter.*;

/*
 * Unit test for Bugzilla 28361 ("foreach no longer works in tag file") 
 */
public class WebTest {

    private static SimpleReporterAdapter stat
        = new SimpleReporterAdapter("appserv-tests");
    private static final String TEST_NAME = "tag-plugin-for-each-tag-file";
    private static final String EXPECTED = "  Hello World";

    private String host;
    private String port;
    private String contextRoot;

    public WebTest(String[] args) {
        host = args[0];
        port = args[1];
        contextRoot = args[2];
    }
    
    public static void main(String[] args) {
        stat.addDescription("Unit test for Bugzilla 28361");
        WebTest webTest = new WebTest(args);
        webTest.doTest();
        stat.printSummary(TEST_NAME);
    }

    public void doTest() {     
        try { 
            URL url = new URL("http://" + host  + ":" + port + contextRoot
                              + "/test.jsp");
            System.out.println("Connecting to: " + url.toString());
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.connect();
            int responseCode = conn.getResponseCode();
            if (responseCode != 200) { 
                stat.addStatus("Wrong response code. Expected: 200"
                               + ", received: " + responseCode, stat.FAIL);
            } else {
                InputStream is = conn.getInputStream();
                BufferedReader input = new BufferedReader(new InputStreamReader(is));
                String line = null;
                boolean found = false;
                while ((line = input.readLine()) != null) {
                    if (EXPECTED.equals(line)) {
                        found = true;
                    }
                }
                
                if (!found) {
                    stat.addStatus("Invalid response. Response did not " +
                                   "contain expected: " + EXPECTED,
                                   stat.FAIL);
                } else {
                    stat.addStatus(TEST_NAME, stat.PASS);
                }
            }
        } catch (Exception ex) {
            System.out.println(TEST_NAME + " test failed.");
            stat.addStatus(TEST_NAME, stat.FAIL);
            ex.printStackTrace();
        }
    }

}
