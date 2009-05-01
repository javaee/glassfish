import java.io.*;
import java.net.*;
import com.sun.ejte.ccl.reporter.*;

/*
 * Unit test for 6412405 ("ParserUtils picks up parser from web app").
 * See also https://glassfish.dev.java.net/issues/show_bug.cgi?id=7968
 * ("Problem with tlds and --libraries")
 */
public class WebTest {

    private static final String TEST_NAME =
        "jsp-in-webapp-with-bundled-xercesImpl";

    private static final String EXPECTED_RESPONSE = "Hello, world!";

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

        stat.addDescription("Unit test for 6412405");
        WebTest webTest = new WebTest(args);

        try {
            webTest.doTest();
            stat.addStatus(TEST_NAME, stat.PASS);
        } catch (Exception ex) {
            ex.printStackTrace();
            stat.addStatus(TEST_NAME, stat.FAIL);
        }

	stat.printSummary();
    }

    public void doTest() throws Exception {

        BufferedReader bis = null;
        try {
            URL url = new URL("http://" + host  + ":" + port
                              + contextRoot + "/test.jsp");
            System.out.println("Connecting to: " + url.toString());

            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.connect();
            int responseCode = conn.getResponseCode();

            if (responseCode != 200) {
                throw new Exception("Unexpected return code: " + responseCode);
            }

            bis = new BufferedReader(
                new InputStreamReader(conn.getInputStream()));
            String line = null;
            while ((line = bis.readLine()) != null) {
                if (EXPECTED_RESPONSE.equals(line)) {
                    break;
                }
            }

            if (line == null) {
                throw new Exception("Wrong response body. Could not find " +
                                    "expected string: " + EXPECTED_RESPONSE);
            }
        } finally {
            try {
                if (bis != null) bis.close();
            } catch (IOException ex) {
            }
        }
    }
}
