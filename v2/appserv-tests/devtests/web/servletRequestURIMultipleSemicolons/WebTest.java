import java.io.*;
import java.net.*;
import com.sun.ejte.ccl.reporter.*;

/*
 * Unit test for https://glassfish.dev.java.net/issues/show_bug.cgi?id=1512
 * ("2nd semicolon in request URI never found"):
 *
 * Make sure that if a request URI carries 2 params, one of which is named
 * for the standard "jsessionid" and therefore is supposed to be consumed by
 * the container, the other is preserved in the return value of
 * ServletRequest.getRequestURI().
 */
public class WebTest {

    private static final String TEST_NAME =
        "servlet-request-uri-multiple-semicolons";

    private static SimpleReporterAdapter stat
        = new SimpleReporterAdapter("appserv-tests");

    private String expectedResponse;

    private String host;
    private String port;
    private String contextRoot;

    public WebTest(String[] args) {

        host = args[0];
        port = args[1];
        contextRoot = args[2];

        expectedResponse = contextRoot + "/test.jsp;jsessionwhatever=456";
    }
    
    public static void main(String[] args) {

        stat.addDescription("Unit test for resource injection into "
                            + "Servlet instance");
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
            + contextRoot + "/test.jsp;jsessionid=12;jsessionwhatever=456");
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
            if (expectedResponse.equals(line)) {
                stat.addStatus(TEST_NAME, stat.PASS);
            } else {
                System.err.println("Wrong response. Expected: " + 
                                   expectedResponse + ", received: " + line);
                stat.addStatus(TEST_NAME, stat.FAIL);
            }
        }    
    }
}
