import java.io.*;
import java.net.*;
import com.sun.ejte.ccl.reporter.*;

/*
 * Unit test for 6346226 ("SessionLockingStandardPipeline.hasFailoverOccurred
 * only supports jroute-id from cookie, not URL").
 *
 * This test requires that security manager be disabled (see build.xml),
 * because the target servlet performs a security-checked operation.
 */
public class WebTest {

    private static SimpleReporterAdapter stat
        = new SimpleReporterAdapter("appserv-tests");

    private static final String TEST_NAME = "jroute-id-in-cookie-or-url";

    private static final String EXPECTED_RESPONSE = "jrouteId=1234";

    private String host;
    private String port;
    private String contextRoot;

    public WebTest(String[] args) {
        host = args[0];
        port = args[1];
        contextRoot = args[2];
    }

    public static void main(String[] args) {

        stat.addDescription("Unit test for 6346226");
        WebTest webTest = new WebTest(args);
        try {
            boolean success = webTest.doTestURL();
            if (success) {
                webTest.doTestCookie();
            }
        } catch (Exception ex) {
            stat.addStatus(TEST_NAME, stat.FAIL);
            ex.printStackTrace();
        }
        stat.printSummary();
    }

    /*
     * @return true on success, false on failure
     */
    public boolean doTestURL() throws Exception {
     
        URL url = new URL("http://" + host  + ":" + port
                          + contextRoot + "/TestServlet"
                          + ";jsessionid=CFE28BD89B33B59CD7249ACBDA5B479D"
                          + ":1234");
        System.out.println("Connecting to: " + url.toString());
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.connect();
        int responseCode = conn.getResponseCode();
        if (responseCode != 200) { 
            System.err.println("Wrong response code. Expected: 200"
                               + ", received: " + responseCode);
            stat.addStatus(TEST_NAME, stat.FAIL);
            return false;
        } else {
            InputStream is = conn.getInputStream();
            BufferedReader input = new BufferedReader(new InputStreamReader(is));
            String line = input.readLine();

            if (EXPECTED_RESPONSE.equals(line)) {
                stat.addStatus(TEST_NAME, stat.PASS);
                return true;
            } else {
                System.err.println("Wrong response. Expected: "
                                   + EXPECTED_RESPONSE
                                   + ", received: " + line);
                stat.addStatus(TEST_NAME, stat.FAIL);
                return false;
            }
        }
    }

    public void doTestCookie() throws Exception {

        Socket socket = new Socket(host, Integer.parseInt(port));
        OutputStream os = socket.getOutputStream();
        os.write(("GET " + contextRoot
                    + "/TestServlet HTTP/1.0\n").getBytes());
        os.write(("Cookie: JROUTE=1234\n").getBytes());
        os.write("\n".getBytes());
        
        InputStream is = socket.getInputStream();
        BufferedReader bis = new BufferedReader(new InputStreamReader(is));
        String line = null;
        String lastLine = null;

        while ((line = bis.readLine()) != null) {
            lastLine = line;
        }

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
