import java.io.*;
import java.net.*;
import com.sun.ejte.ccl.reporter.*;

/*
 * Make sure webapp can be deployed to '/' on virtual server 'server'.
 *
 * For this to work, this unit test does not specify any 'contextroot' argument
 * for deployment. Instead, it specifies a context-root with value '/' in its
 * sun-web.xml, as follows:
 *
 *  <sun-web-app>
 *    <context-root>/</context-root> 
 *  </sun-web-app>
 */
public class WebTest {

    private static SimpleReporterAdapter stat
        = new SimpleReporterAdapter("appserv-tests");

    private static final String EXPECTED_RESPONSE = "This is a test";

    private static final String TEST_NAME = "empty-context-root";

    private String host;
    private String port;

    public WebTest(String[] args) {
        host = args[0];
        port = args[1];
    }
    
    public static void main(String[] args) {
        stat.addDescription("Unit test for context-root setting in "
                            + "sun-web.xml");
        WebTest webTest = new WebTest(args);
        webTest.doTest();
        stat.printSummary(TEST_NAME);
    }

    public void doTest() {     
        try { 
            invokeJsp();
            stat.addStatus(TEST_NAME, stat.PASS);
        } catch (Exception ex) {
            System.out.println(TEST_NAME + " test failed.");
            stat.addStatus(TEST_NAME, stat.FAIL);
            ex.printStackTrace();
        }
    }

    private void invokeJsp() throws Exception {

        URL url = new URL("http://" + host + ":" + port + "/test.jsp");
        System.out.println("Connecting to: " + url.toString());
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.connect();

        int responseCode = conn.getResponseCode();
        if (responseCode != 200) {
            throw new Exception(
                "Wrong response code. Expected: 200, received: " +
                responseCode);
        }

        InputStream is = null;
        BufferedReader input = null;
        try {
            is = conn.getInputStream();
            input = new BufferedReader(new InputStreamReader(is));
            String line = null;
            String lastLine = null;
            while ((line = input.readLine()) != null) {
                lastLine = line;
            }
            if (!EXPECTED_RESPONSE.equals(lastLine)) {
                throw new Exception("Wrong response body. Expected: " +
                    EXPECTED_RESPONSE + ", received: " + lastLine);
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
