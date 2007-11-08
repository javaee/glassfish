import java.io.*;
import java.net.*;
import com.sun.ejte.ccl.reporter.*;

/** 
 * Unit test for:
 *
 *  https://glassfish.dev.java.net/issues/show_bug.cgi?id=2339
 *  ("i18n: deployed app doesn't launch")
 *
 * Multi-byte context root is specified in sun-web.xml.
 */
public class WebTest {

    private static final String TEST_NAME = "multi-byte-context-root";

    private static final String EXPECTED_RESPONSE = "Hello multibyte world!";

    private static SimpleReporterAdapter stat
        = new SimpleReporterAdapter("appserv-tests");

    private String host;
    private String port;

    public WebTest(String[] args) {
        host = args[0];
        port = args[1];
    }
    
    public static void main(String[] args) {

        stat.addDescription("Unit test for GlassFish Issue 2339");
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
     
        URL url = new URL("http://" + host  + ":" + port + "/"
            + "%E3%81%A8%E7%B2%AEJot%E7%B2%AEEnterpriseApplication2%E7%B2%A4%E3%82%8D-war/test.jsp");
        System.out.println("Connecting to: " + url.toString());

        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.connect();
        int responseCode = conn.getResponseCode();

        if (responseCode != 200) {
            System.err.println("Unexpected return code: " + responseCode);
            stat.addStatus(TEST_NAME, stat.FAIL);
        } else {
            InputStream is = conn.getInputStream();
            BufferedReader br = new BufferedReader(new InputStreamReader(is));
            String line = br.readLine();
            System.out.println("Response=" + line);
            if (EXPECTED_RESPONSE.equals(line)) {
                stat.addStatus(TEST_NAME, stat.PASS);
            } else {
                System.err.println("Wrong response. Expected: " + 
                                   EXPECTED_RESPONSE + ", received: " + line);
                stat.addStatus(TEST_NAME, stat.FAIL);
            }
        }    
    }
}
