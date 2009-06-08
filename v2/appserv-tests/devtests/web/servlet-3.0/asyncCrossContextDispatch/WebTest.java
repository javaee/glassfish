import java.io.*;
import java.net.*;
import com.sun.ejte.ccl.reporter.*;

/*
 * Unit test for cross-context ASYNC dispatch.
 *
 * This unit test accesses servlet "dispatchFrom" in context "fromContext",
 * which initiates an ASYNC dispatch to servlet "dispatchTo" in context 
 * "toContext".
 */
public class WebTest {

    private static final String TEST_NAME = "async-cross-context-dispatch";

    private static final String EXPECTED_RESPONSE = "Hello world";

    private static SimpleReporterAdapter stat
        = new SimpleReporterAdapter("appserv-tests");

    private String host;
    private String port;

    public WebTest(String[] args) {
        host = args[0];
        port = args[1];
    }
    
    public static void main(String[] args) throws Exception {

        stat.addDescription("Unit test for cross-context async dispatch");
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
     
        URL url = new URL("http://" + host  + ":" + port +
            "/fromContext/dispatchFrom?myname=myvalue");
        System.out.println("Connecting to: " + url.toString());

        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setReadTimeout(10000);
        conn.connect();
        if (conn.getResponseCode() != 200) {
            throw new Exception("Unexpected return code: " +
                                conn.getResponseCode());
        }

        InputStream is = null;
        BufferedReader input = null;
        String line = null;
        try {
            is = conn.getInputStream();
            input = new BufferedReader(new InputStreamReader(is));
            while ((line = input.readLine()) != null) {
                System.out.println(line);
                if (line.equals(EXPECTED_RESPONSE)) {
                    break;
                }
            }
        } finally {
            try {
                if (is != null) {
                    is.close();
                }
            } catch(IOException ioe) {
                // ignore
            }
            try {
                if (input != null) {
                    input.close();
                }
            } catch(IOException ioe) {
                // ignore
            }
        }

        if (line == null) {
            throw new Exception("Missing or unexpected response body, " +
                                "expected: " + EXPECTED_RESPONSE);
        }
    }
}
