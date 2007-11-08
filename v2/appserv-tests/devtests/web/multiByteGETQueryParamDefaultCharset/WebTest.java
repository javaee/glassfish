import java.lang.*;
import java.io.*;
import java.net.*;

import com.sun.ejte.ccl.reporter.*;

/**
 * Unit test for 6339608 ("Differences in the behaviour of 8.1 PE ur1
 * and 8.1 EE ur2 using IE (HTTP GET Query with I18N)"):
 *
 * Make sure that default-charset specified in sun-web.xml is applied
 * to request's query params. This is verified by the fact that the target
 * servlet does not call ServletRequest.setCharacterEncoding().
 */
public class WebTest {

    private static SimpleReporterAdapter stat
        = new SimpleReporterAdapter("appserv-tests");

    private static final String TEST_NAME =
        "multi-byte-get-query-param-default-charset";
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
                 + "/TestServlet?japaneseName=\u3068\u4eba\u6587"
                 + " HTTP/1.0\r\n");
        bw.write("\r\n");
        bw.flush();

        // Read response
        InputStream is = sock.getInputStream();
        BufferedReader input = new BufferedReader(new InputStreamReader(is));
        String line = null;
        String lastLine = null;
        while ((line = input.readLine()) != null) {
            lastLine = line;
        }
        if (EXPECTED_RESPONSE.equals(lastLine)) {
            stat.addStatus(TEST_NAME, stat.PASS);
        } else {
            System.err.println("Wrong response body. Expected: "
                               + EXPECTED_RESPONSE + ", received: "
                               + lastLine);
            stat.addStatus(TEST_NAME, stat.FAIL);
        }
    }
}
