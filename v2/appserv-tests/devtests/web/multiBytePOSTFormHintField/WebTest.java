import java.lang.*;
import java.io.*;
import java.net.*;

import com.sun.ejte.ccl.reporter.*;

/**
 * Make sure that form-hint-field specified in sun-web.xml is applied
 * to request's form data. This is verified by the fact that the target
 * servlet does not need to call ServletRequest.setCharacterEncoding()
 * before retrieving the request param via ServletRequest.getParameter().
 */
public class WebTest {

    private static SimpleReporterAdapter stat
        = new SimpleReporterAdapter("appserv-tests");

    private static final String TEST_NAME =
        "multi-byte-post-form-hint-field";
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
        stat.addDescription("Unit test for POST form data with form-hint-field");
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
     
        // POST body
        String body = "japaneseName="
            + URLEncoder.encode("\u3068\u4eba\u6587", "Shift_JIS")
            + "&requestCharset=Shift_JIS";

        // Create a socket to the host
        Socket sock = new Socket(host, Integer.parseInt(port));
        OutputStream os = sock.getOutputStream();
    
        // Send header
        BufferedWriter wr = new BufferedWriter(new OutputStreamWriter(
                                    sock.getOutputStream()));
        wr.write("POST " + contextRoot + "/TestServlet HTTP/1.0\r\n");
        wr.write("Content-Length: " + body.length() + "\r\n");
        wr.write("Content-Type: application/x-www-form-urlencoded\r\n");
        wr.write("\r\n");
    
        // Send body
        wr.write(body);
        wr.flush();

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
