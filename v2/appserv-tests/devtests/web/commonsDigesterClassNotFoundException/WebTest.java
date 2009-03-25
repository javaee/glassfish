import java.io.*;
import java.net.*;
import com.sun.ejte.ccl.reporter.*;

/*
 * Unit test for CR 6314912 ("Parsing Error: Tiles not working in SJSAS 8.1").
 * See also
 * http://blogs.sun.com/roller/page/jluehe?entry=why_is_code_org_apache
 */
public class WebTest {

    private static SimpleReporterAdapter stat
        = new SimpleReporterAdapter("appserv-tests");

    private static final String EXPECTED_RESPONSE = "The Parent";

    private static final String TEST_NAME
        = "commons-digester-class-not-found-exception";

    private String host;
    private String port;
    private String contextRoot;

    public WebTest(String[] args) {
        host = args[0];
        port = args[1];
        contextRoot = args[2];
    }
    
    public static void main(String[] args) {
        stat.addDescription("Unit test for 6314912");

        try {
            new WebTest(args).doTest();
            stat.addStatus(TEST_NAME, stat.PASS);
        } catch (Exception ex) {
            stat.addStatus(TEST_NAME, stat.FAIL);
            ex.printStackTrace();
        }

        stat.printSummary(TEST_NAME);
    }

    private void doTest() throws Exception {
 
        URL url = new URL("http://" + host + ":" + port + contextRoot
                          + "/TestServlet");
        System.out.println("Connecting to: " + url.toString());
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.connect();
        int responseCode = conn.getResponseCode();
        if (responseCode != 200) {
            throw new Exception("Unexpected return code: " + responseCode);
        }

        InputStream is = conn.getInputStream();
        BufferedReader input = new BufferedReader(new InputStreamReader(is));
        String line = input.readLine();
        if (!EXPECTED_RESPONSE.equals(line)) {
            throw new Exception("Wrong response. Expected: " + 
                                EXPECTED_RESPONSE + ", received: " + line);
        }    
    }
}
