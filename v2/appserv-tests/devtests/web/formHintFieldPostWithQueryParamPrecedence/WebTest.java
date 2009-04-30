import java.io.*;
import java.net.*;
import com.sun.ejte.ccl.reporter.*;

/*
 * Unit test for 6346738 ("getParameter() fails to return correct paramter
 * when locale-charset used QueryString not considered"):
 *
 * Make sure query param takes precedence (i.e., is returned as the first
 * element by ServletRequest.getParameterValues()) over param with same name in
 * POST body even when form-hint-field has been declared in sun-web.xml (which
 * causes the POST body to be parsed in order to determine request encoding).
 */
public class WebTest {

    private static final String TEST_NAME =
        "form-hint-field-post-with-query-param-precedence";

    private static final String EXPECTED_RESPONSE = "value1,value2";

    private static SimpleReporterAdapter stat
        = new SimpleReporterAdapter("appserv-tests");

    private String host;
    private String port;
    private String contextRoot;
    private Socket socket = null;

    public WebTest(String[] args) {
        host = args[0];
        port = args[1];
        contextRoot = args[2];
    }
    
    public static void main(String[] args) {
        stat.addDescription("Unit test for 6346738");

        try {
            new WebTest(args).doTest();
            stat.addStatus(TEST_NAME, stat.PASS);
        } catch (Exception ex) {
            ex.printStackTrace();
            stat.addStatus(TEST_NAME, stat.FAIL);
        } finally {
            try {
                if (socket != null) {
                    socket.close();
                }
            } catch (IOException ioe) {
                // ignore
            }
        }

	stat.printSummary();
    }

    public void doTest() throws Exception {

        String body = "param1=value2";

        // Create a socket to the host
        socket = new Socket(host, new Integer(port).intValue());
    
        // Send header
        BufferedWriter wr = new BufferedWriter(new OutputStreamWriter(
                                            socket.getOutputStream()));
        wr.write("POST " + contextRoot + "/TestServlet?param1=value1"
                 + " HTTP/1.0\r\n");
        wr.write("Content-Length: " + body.length() + "\r\n");
        wr.write("Content-Type: application/x-www-form-urlencoded\r\n");
        wr.write("\r\n");
    
        // Send body
        wr.write(body);
        wr.flush();

        // Read response
        BufferedReader bis = null;
        String lastLine = null;
        try {
            bis = new BufferedReader(new InputStreamReader(
                socket.getInputStream()));
            String line = null;
            while ((line = bis.readLine()) != null) {
                lastLine = line;
            }
        } finally {
            try {
                if (bis != null) {
                    bis.close();
                }
            } catch (IOException ioe) {
                // ignore
            }
        }

        if (!EXPECTED_RESPONSE.equals(lastLine)) {
            throw new Exception("Wrong response. Expected: " +
                                EXPECTED_RESPONSE + ", received: " +
                                lastLine);
        }
    }
}
