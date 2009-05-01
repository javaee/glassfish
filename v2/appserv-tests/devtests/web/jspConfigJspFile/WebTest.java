import java.io.*;
import java.net.*;
import com.sun.ejte.ccl.reporter.*;

/*
 * Verifies that <jsp-config> properties are propagated as init parameters
 * to <jsp-file> servlets (as well as to the JspServlet itself), so that
 * sun-web.xml <jsp-config> properties such as reload-interval, httpMethods,
 * etc. can be applied to JSPs configured as <jsp-file>s also.
 */
public class WebTest {

    private static final String TEST_NAME = "jsp-config-jsp-file";
    private static final String EXPECTED_RESPONSE = "myValue";

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
        stat.addDescription("Unit test for init param propagation to "
                            + "<jsp-file> servlets");
        WebTest webTest = new WebTest(args);
        try {
            webTest.doTest();
        } catch (Exception ex) {
            stat.addStatus(TEST_NAME, stat.FAIL);
            ex.printStackTrace();
        }
	stat.printSummary();
    }

    public void doTest() throws Exception {

        InputStream is = null;
        BufferedReader input = null;
        try {
            URL url = new URL("http://" + host  + ":" + port
                              + contextRoot + "/TestServlet");
            System.out.println("Connecting to: " + url.toString());
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.connect();
            int responseCode = conn.getResponseCode();
            if (responseCode != 200) {
                System.err.println("Wrong response code. Expected: 200"
                                   + ", received: " + responseCode);
                stat.addStatus(TEST_NAME, stat.FAIL);
            } else {
                is = conn.getInputStream();
                input = new BufferedReader(new InputStreamReader(is));
                String line = input.readLine();
                if (!EXPECTED_RESPONSE.equals(line)) {
                    System.err.println("Wrong response. Expected: "
                                       + EXPECTED_RESPONSE
                                       + ", received: " + line);
                    stat.addStatus(TEST_NAME, stat.FAIL);
                } else {
                    stat.addStatus(TEST_NAME, stat.PASS);
                }
            }
        } finally {
            try {
                if (is != null) is.close();
            } catch (IOException ex) {}
            try {
                if (input != null) input.close();
            } catch (IOException ex) {}
        }
    }
}
