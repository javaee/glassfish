import java.io.*;
import java.net.*;
import com.sun.ejte.ccl.reporter.*;

/**
 * Unit test for:
 *
 *  https://glassfish.dev.java.net/issues/show_bug.cgi?id=7050
 *  ("Support simple servlet use case")
 */
public class WebTest {

    private static SimpleReporterAdapter stat = new SimpleReporterAdapter("appserv-tests");

    private static final String EXPECTED_RESPONSE = "Embedded servlet invoked";

    private static final String TEST_NAME = "embedded-add-servlet";

    private String host;
    private String port;
    private String contextRoot;

    public WebTest(String[] args) {
        host = args[0];
        port = args[1];
        contextRoot = args[2];
    }
    
    public static void main(String[] args) {
        stat.addDescription("Unit test for IT 7050");
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

        Socket sock = new Socket(host, new Integer(port).intValue());
        OutputStream os = sock.getOutputStream();
        os.write(("GET " + contextRoot + "/addServlet" + " HTTP/1.0\n\n").getBytes());

        InputStream is = sock.getInputStream();
        BufferedReader bis = new BufferedReader(new InputStreamReader(is));
        String line = null;
        while ((line = bis.readLine()) != null) {
            System.out.println(line);
            if (EXPECTED_RESPONSE.equals(line)) {
                break;
            }
        }

        if (line == null) {
            throw new Exception("Unexpected response");
        }
    }
}
