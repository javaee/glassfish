import java.io.*;
import java.net.*;
import com.sun.ejte.ccl.reporter.*;

/**
 * Unit test for:
 *
 *   https://issues.apache.org/bugzilla/show_bug.cgi?id=44391
 *  ("SSI handling of escaped characters broken")
 *
 */
public class WebTest {

    private static SimpleReporterAdapter stat
        = new SimpleReporterAdapter("appserv-tests");

    private static final String TEST_NAME
        = "ssi-escape-character";

    private static final String EXPECTED_CONTENT_TYPE = "Content-Type: text/html";
    private static final String EXPECTED = "Happy&quot;&quot;";

    private String host;
    private String port;
    private String contextRoot;

    public WebTest(String[] args) {
        host = args[0];
        port = args[1];
        contextRoot = args[2];
    }
    
    public static void main(String[] args) {
        stat.addDescription("Unit test for SSI Escape Character");
        WebTest webTest = new WebTest(args);
        webTest.doTest();
        stat.printSummary(TEST_NAME);
    }

    public void doTest() {     
        try { 
            invoke();
        } catch (Exception ex) {
            stat.addStatus(TEST_NAME, stat.FAIL);
            ex.printStackTrace();
        }
    }

    private void invoke() throws Exception {

        System.out.println("Host=" + host + ", port=" + port);        
        Socket sock = new Socket(host, new Integer(port).intValue());
        OutputStream os = sock.getOutputStream();
        String get = "GET " + contextRoot + "/index.shtml HTTP/1.1\n";
        System.out.println(get);
        os.write(get.getBytes());
        os.write("Host: localhost\n".getBytes());
        os.write("\n".getBytes());

        InputStream is = sock.getInputStream();
        BufferedReader bis = new BufferedReader(new InputStreamReader(is));

        String line = null;
        boolean hasExpectedContentType = false;
        boolean hasExpectedResponse = false;
        while ((line = bis.readLine()) != null) {
            System.out.println(line);
            if (!hasExpectedContentType) {
                if (line.startsWith(EXPECTED_CONTENT_TYPE)) {
                    hasExpectedContentType = true;
                }
            } else if (line.equals(EXPECTED)) {
                hasExpectedResponse = true;
                break;
            }
        }

        if (hasExpectedContentType && hasExpectedResponse) {
            stat.addStatus(TEST_NAME, stat.PASS);
        } else {
            if (!hasExpectedContentType) {
                System.err.println("Missing expected content type: " + EXPECTED_CONTENT_TYPE);
            }
            if (!hasExpectedResponse) {
                System.err.println("Missing expected response: " + EXPECTED);
            }
            stat.addStatus(TEST_NAME, stat.FAIL);
        }
    }
}
