import java.io.*;
import java.net.*;
import java.net.HttpURLConnection;
import com.sun.ejte.ccl.reporter.*;

/*
 * Make sure that if web.xml contains servlet mapping with wildcards, such as:
 *
 *   /application/* map to SomeServlet
 *
 * and a security constraint of the form:
 *
 *   <url-pattern>/application/redirect/*</url-pattern>
 *
 * resources such as
 *
 *   /application/something/*
 *
 * will indeed be protected, i.e., trigger authentication.
 */
public class WebTest {

    private static final String TEST_NAME = "security-constraint-wildcard-mapping";

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
        stat.addDescription("Unit test for security constraints mapping");
        WebTest webTest = new WebTest(args);

        try {
            webTest.doTest();
            stat.addStatus(TEST_NAME, stat.PASS);
        } catch (Exception e) {
            e.printStackTrace();
            stat.addStatus(TEST_NAME, stat.FAIL);
        }

	stat.printSummary();
    }

    public void doTest() throws Exception {
     
        URL url = new URL("http://" + host  + ":" + port
                          + contextRoot + "/application/redirect/xxx");
        System.out.println("Connecting to: " + url.toString());
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.connect();
        int responseCode = conn.getResponseCode();

        if (responseCode != HttpURLConnection.HTTP_UNAUTHORIZED) {
            throw new Exception("Wrong response code. Expected: " +
                HttpURLConnection.HTTP_UNAUTHORIZED + ", received: " +
                responseCode);
        }
    }

}
