import java.io.*;
import java.net.*;
import java.security.*;
import javax.net.ssl.*;
import com.sun.ejte.ccl.reporter.*;

/*
 * Test case for issue 5141: request.getPathInfo() returning "/"
 * even when request URL has no trailing "/"
 */
public class WebTest {

    private static SimpleReporterAdapter stat
        = new SimpleReporterAdapter("appserv-tests");
    private static final String TEST_NAME = "redirect-with-root-wild-card-url-pattern";

    private String host;
    private String port;
    private String contextRoot;
    private String expected;

    public WebTest(String[] args) {
        host = args[0];
        port = args[1];
        contextRoot = args[2];
        expected = "uri = " + contextRoot + "/, pathInfo = /";
    }
    
    public static void main(String[] args) {
        stat.addDescription("Unit test for Issue 5141");
        WebTest webTest = new WebTest(args);
        webTest.doTest();
        stat.printSummary(TEST_NAME);
    }

    public void doTest() {

        URL url = null;
        int responseCode;
        boolean fail = false;

        try {

            url = new URL("http://" + host  + ":" + port + contextRoot);
            System.out.println("Connecting to: " + url.toString());
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setInstanceFollowRedirects(false);
            conn.connect();
            responseCode = conn.getResponseCode();
            System.out.println("Response code: " + responseCode);
            if (responseCode != HttpURLConnection.HTTP_MOVED_TEMP &&
                    responseCode != HttpURLConnection.HTTP_MOVED_PERM) {
                fail = true;
            } else {
                url = new URL(conn.getHeaderField("Location"));
                System.out.println("Redirected to: " + url.toString());
                conn = (HttpURLConnection)url.openConnection();
                conn.connect();
                responseCode = conn.getResponseCode();
                System.out.println("Response code: " + responseCode);
                if (responseCode != HttpURLConnection.HTTP_OK) {
                    fail = true;
                } else {
                    BufferedReader bis = new BufferedReader(
                            new InputStreamReader(conn.getInputStream()));
                    String line = null;
                    boolean hasExpectedResponse = false;
                    while ((line = bis.readLine()) != null) {
                        System.out.println(line);
                        if (expected.equals(line)) {
                            hasExpectedResponse = true;
                            break;
                        }
                    }
                    if (!hasExpectedResponse) {
                        fail = true;
                    }
                }
            }

            if (fail) {
                stat.addStatus(TEST_NAME, stat.FAIL);
            } else {
                stat.addStatus(TEST_NAME, stat.PASS);
            }

        } catch (Exception ex) {
            System.out.println(TEST_NAME + " test failed");
            stat.addStatus(TEST_NAME, stat.FAIL);
            ex.printStackTrace();
        }
    }
}
