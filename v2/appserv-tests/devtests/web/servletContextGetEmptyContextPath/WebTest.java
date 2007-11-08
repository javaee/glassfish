import java.io.*;
import java.net.*;
import com.sun.ejte.ccl.reporter.*;

/*
 * Unit test for new ServletContext.getContextPath() method (Servlet 2.5
 * feature) with an empty context path.
 *
 * The empty context path is specified in the sun-web.xml, as follows:
 *
 *  <sun-web-app>
 *    <context-root></context-root> 
 *  </sun-web-app>
 *
 * and there is no contextroot specified in build.properties.
 */
public class WebTest {

    private static final String TEST_NAME
        = "servlet-context-get-empty-context-path";

    private static SimpleReporterAdapter stat
        = new SimpleReporterAdapter("appserv-tests");

    private static final String EXPECTED_RESPONSE = "0";

    private String host;
    private String port;

    public WebTest(String[] args) {
        host = args[0];
        port = args[1];
    }
    
    public static void main(String[] args) {

        stat.addDescription("Unit test for ServletContext.getContextPath()");
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
     
        URL url = new URL("http://" + host  + ":" + port + "/TestServlet");
        System.out.println("Connecting to: " + url.toString());
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.connect();
        int responseCode = conn.getResponseCode();
        if (responseCode != 200) {
            System.err.println("Unexpected return code: " + responseCode);
            stat.addStatus(TEST_NAME, stat.FAIL);
        } else {
            InputStream is = conn.getInputStream();
            BufferedReader input = new BufferedReader(new InputStreamReader(is));
            String line = input.readLine();
            if (EXPECTED_RESPONSE.equals(line)) {
                stat.addStatus(TEST_NAME, stat.PASS);
            } else {
                System.err.println("Wrong response. Expected: " + 
                                   EXPECTED_RESPONSE + ", received: " + line);
                stat.addStatus(TEST_NAME, stat.FAIL);
            }
        }    
    }
}
