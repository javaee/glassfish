import java.io.*;
import java.net.*;
import com.sun.ejte.ccl.reporter.*;

/*
 * Unit test for 4936855 ("i18n: request encoding being set after calling
 * getParameter").
 *
 * Originally, this test expected an IllegalStateException to be thrown if
 * ServletRequest.setCharacterEncoding() was called after
 *
 * - ServletRequest.getReader() had been called, or
 * - request params had been parsed,
 *
 * as originally mandated by the Servlet 2.5 spec.
 * However, the Servlet 2.5 spec was later relaxed to log warning and ignore
 * specified encoding, because throwing IllegalStateException would have broken
 * too many non-compliant applications.
 *
 * See
 *
 *   https://servlet-spec-eg.dev.java.net/issues/show_bug.cgi?id=18
 *
 * for details.
 */
public class WebTest {

    private static final String TEST_NAME =
        "servlet-request-setcharencoding-illegal-state-exception";

    private static final String EXPECTED_RESPONSE = "true";

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

        stat.addDescription("Unit test for 4936855");
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
                          + contextRoot + "/TestServlet");
        System.out.println("Connecting to: " + url.toString());
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.connect();
        int responseCode = conn.getResponseCode();
        if (responseCode != 200) { 
            System.err.println("Wrong response code. Expected: 200"
                               + ", received: " + responseCode);
            stat.addStatus(TEST_NAME, stat.FAIL);
        } else {
            InputStream is = conn.getInputStream();
            BufferedReader input = new BufferedReader(new InputStreamReader(is));
            String line = input.readLine();
            if (EXPECTED_RESPONSE.equals(line)) {
                stat.addStatus(TEST_NAME, stat.PASS);
            } else {
                System.err.println("Wrong response. Expected: "
                                   + EXPECTED_RESPONSE
                                   + ", received: " + line);
                stat.addStatus(TEST_NAME, stat.FAIL);
            }
        }
    }
}
