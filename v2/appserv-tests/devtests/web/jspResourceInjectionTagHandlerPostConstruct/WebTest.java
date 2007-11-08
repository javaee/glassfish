import java.io.*;
import java.net.*;
import com.sun.ejte.ccl.reporter.*;

/*
 * Unit test for the @PostConstruct annotation on a JSP tag handler.
 */
public class WebTest {

    private static final String TEST_NAME =
        "jsp-resource-injection-tag-handler-post-construct";

    private static final String EXPECTED_RESPONSE =
        "ds1-login-timeout=0,ds2-login-timeout=0,ds3-login-timeout=0";

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

        stat.addDescription("Unit test for resource injection into JSP tag handler");
        WebTest webTest = new WebTest(args);

        try {
            webTest.doTest();
        } catch (Exception ex) {
            ex.printStackTrace();
            stat.addStatus(TEST_NAME, stat.FAIL);
        }

	stat.printSummary();
    }

    public void doTest() throws Exception {
     
        URL url = new URL("http://" + host  + ":" + port
                          + contextRoot + "/jsp/test.jsp");
        System.out.println("Connecting to: " + url.toString());
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.connect();

        int responseCode = conn.getResponseCode();
        if (responseCode == 200) {
            InputStream is = conn.getInputStream();
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
        } else {
            System.err.println("Wrong response code. Expected: 200"
                               + ", received: " + responseCode);
            stat.addStatus(TEST_NAME, stat.FAIL);
        }
    }
}
