import java.io.*;
import java.net.*;
import com.sun.ejte.ccl.reporter.*;

/*
 * Unit test to ensure that GlassFish-style valves compiled against
 * the "old" org.apache.catalina.Valve interface (from GlassFish releases
 * prior to V3), which has been renamed to
 * org.glassfish.web.valve.GlassFishValve in V3, continue to work in V3
 * (in a binary-compatible way).
 * 
 * This test deploys a webapp that specifies a GlassFish-style valve
 * (compiled against the "old" Valve interface) in its sun-web.xml.
 * The valve adds an attribute to the request.
 * 
 * When the test accesses the webapp's servlet, the servlet checks for the
 * presence of this attribute in the request, and will cause the test to fail
 * if the attribute is missing.
 */
public class WebTest {

    private static final String TEST_NAME = "valve-glassFish-style-backward-compatible";

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

        stat.addDescription("Unit test for ensuring binary compatibility " +
            "of GlassFish-style valves compiled against the old Valve " +
            "interface");
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
     
        URL url = new URL("http://" + host  + ":" + port
                          + contextRoot + "/test");
        System.out.println("Connecting to: " + url.toString());

        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.connect();
        int responseCode = conn.getResponseCode();

        if (responseCode != 200) {
            throw new Exception("Unexpected return code: " + responseCode);
        }

        InputStream is = null;
        BufferedReader input = null;
        try {
            is = conn.getInputStream();
            input = new BufferedReader(new InputStreamReader(is));
            String line = input.readLine();
            if (!EXPECTED_RESPONSE.equals(line)) {
                throw new Exception("Wrong response. Expected: " + 
                                    EXPECTED_RESPONSE + ", received: " +
                                    line);
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
    }
}
