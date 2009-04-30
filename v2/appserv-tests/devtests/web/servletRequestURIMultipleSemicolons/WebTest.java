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

        try {
            new WebTest(args).doTest();
            stat.addStatus(TEST_NAME, stat.PASS);
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
            throw new Exception("Unexpected return code: " + responseCode);
        }

        InputStream is = null;
        BufferedReader input = null;
        try {
            is = conn.getInputStream();
            input = new BufferedReader(new InputStreamReader(is));
            String line = input.readLine();
            if (!expectedResponse.equals(line)) {
                throw new Exception("Wrong response. Expected: " + 
                    expectedResponse + ", received: " + line);
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
