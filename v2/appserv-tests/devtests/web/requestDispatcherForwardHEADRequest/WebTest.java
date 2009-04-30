import java.io.*;
import java.net.*;
import com.sun.ejte.ccl.reporter.*;

/**
 * Unit test for:
 *
 *   https://glassfish.dev.java.net/issues/show_bug.cgi?id=2210
 *   ("ClassCastException: javax.servlet.http.NoBodyResponse, if target
 *   servlet of a HEAD request performs rd.forward()")
 */
public class WebTest {

    private static SimpleReporterAdapter stat
        = new SimpleReporterAdapter("appserv-tests");

    private static final String TEST_NAME
        = "request-dispatcher-forward-head-request";

    private static final String EXPECTED_CONTENT_LENGTH_HEADER
        = "Content-Length: 11";

    private String host;
    private String port;
    private String contextRoot;
    private Socket sock = null;

    public WebTest(String[] args) {
        host = args[0];
        port = args[1];
        contextRoot = args[2];
    }
    
    public static void main(String[] args) {
        stat.addDescription("Unit test for GlassFish Issue 2210");
        WebTest webTest = new WebTest(args);
        try {
            webTest.doTest();
            stat.addStatus(TEST_NAME, stat.PASS);
        } catch (Exception ex) {
            stat.addStatus(TEST_NAME, stat.FAIL);
            ex.printStackTrace();
        } finally {
            try {
                if (webTest.sock != null) {
                    webTest.sock.close();
                }
            } catch (IOException ioe) {
                // ignore
            }
        }

        stat.printSummary(TEST_NAME);
    }

    public void doTest() throws Exception {

        sock = new Socket(host, new Integer(port).intValue());
        OutputStream os = sock.getOutputStream();
        String request = "HEAD " + contextRoot + "/From " + "HTTP/1.0\n";
        System.out.println(request);
        os.write(request.getBytes());
        os.write("\n".getBytes());

        InputStream is = null;
        BufferedReader bis = null;
        try {
            is = sock.getInputStream();
            bis = new BufferedReader(new InputStreamReader(is));
            String line = null;
            String firstLine = null;
            while ((line = bis.readLine()) != null) {
                if (firstLine == null) {
                    firstLine = line;
                }
                if (EXPECTED_CONTENT_LENGTH_HEADER.equals(line)) {
                    break;
                }
            }
            if (!firstLine.startsWith("HTTP/1.1 200")) {
                throw new Exception("Unexpected return code: " + firstLine);
            }
            if (line == null) {
                throw new Exception(
                    "Wrong or missing Content-Length header. " +
                    "Expected: " + EXPECTED_CONTENT_LENGTH_HEADER);
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
                if (bis != null) {
                    bis.close();
                }
            } catch (IOException ioe) {
                // ignore
            }
        }    
    }
}
