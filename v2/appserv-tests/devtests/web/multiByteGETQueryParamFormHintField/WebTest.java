import java.lang.*;
import java.io.*;
import java.net.*;

import com.sun.ejte.ccl.reporter.*;

/**
 * Unit test for 6339608 ("Differences in the behaviour of 8.1 PE ur1
 * and 8.1 EE ur2 using IE (HTTP GET Query with I18N)"):
 *
 * Make sure that form-hint-field specified in sun-web.xml is applied
 * to request's query params. This is verified by the fact that the target
 * servlet does not call ServletRequest.setCharacterEncoding().
 */
public class WebTest {

    private static SimpleReporterAdapter stat
        = new SimpleReporterAdapter("appserv-tests");

    private static final String TEST_NAME =
        "multi-byte-get-query-param-form-hint-field";
    private static final String EXPECTED_RESPONSE = "true";

    private String host;
    private String port;
    private String contextRoot;

    public WebTest(String[] args) {
        host = args[0];
        port = args[1];
        contextRoot = args[2];
    }
    
    public static void main(String[] args) {
        stat.addDescription("Unit test for 6339608");
        WebTest webTest = new WebTest(args);
        try {
            webTest.doTest();
            stat.addStatus(TEST_NAME, stat.PASS);
        } catch (Exception ex) {
            stat.addStatus(TEST_NAME, stat.FAIL);
            ex.printStackTrace();
        }
        stat.printSummary(TEST_NAME);
    }

    public void doTest() throws Exception {
     
        Socket sock = new Socket(host, Integer.parseInt(port));
    
        BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(
            sock.getOutputStream(), "Shift_JIS"));
        bw.write("GET " + contextRoot
                 + "/TestServlet?"
                 + "japaneseName=\u3068\u4eba\u6587"
                 + "&requestCharset=Shift_JIS"
                 + " HTTP/1.0\r\n");
        bw.write("\r\n");
        bw.flush();

        // Read response
        InputStream is = null;
        BufferedReader input = null;
        String line = null;
        String lastLine = null;
        try {
            is = sock.getInputStream();
            input = new BufferedReader(new InputStreamReader(is));
            while ((line = input.readLine()) != null) {
                lastLine = line;
            }
        } finally {
            try {
                if (is != null) {
                    is.close();
                }
            } catch (IOException ioe) {
                // ignore
            }
            try {
                if (input != null) {
                    input.close();
                }
            } catch (IOException ioe) {
                // ignore
            }
        }

        if (!EXPECTED_RESPONSE.equals(lastLine)) {
            throw new Exception("Wrong response body. Expected: " +
                EXPECTED_RESPONSE + ", received: " + lastLine);
        }
    }
}
