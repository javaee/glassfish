import java.io.*;
import java.net.*;
import com.sun.ejte.ccl.reporter.*;

/*
 * Make sure HttpSessionListener's sessionDestroyed(), and 
 * HttpSessionBindingListener's valueUnbound() methods are called when
 * session expires.
 *
 * Implementation note: According to SRV.7.5 ("Session Timeouts"), "the session
 * invalidation will not take effect util all servlets using that session have
 * exited the service method." This means that for this unit test, the
 * servlet that creates the session and the HttpSessionListener that waits for
 * the session expiration event are implemented as separate classes.
 */
public class WebTest {

    private static SimpleReporterAdapter stat
        = new SimpleReporterAdapter("appserv-tests");
    private static final String TEST_NAME = "session-expired";
    private static final String EXPECTED_RESPONSE = "true";

    private String host;
    private String port;
    private String contextRoot;

    public WebTest(String[] args) {
        host = args[0];
        port = args[1];
        contextRoot = args[2];
    }
    
    public static void main(String[] args) {
        stat.addDescription("Unit test for "
                            + "HttpSessionListener.sessionDestroyed()");
        WebTest webTest = new WebTest(args);
        webTest.doTest();
        stat.printSummary(TEST_NAME);
    }

    public void doTest() {
        try { 
            invoke();
        } catch (Exception ex) {
            System.out.println(TEST_NAME + " test failed");
            stat.addStatus(TEST_NAME, stat.FAIL);
            ex.printStackTrace();
        }
    }

    private void invoke() throws Exception {
        
        String url = "http://" + host + ":" + port + contextRoot
                     + "/CreateSession";
        HttpURLConnection conn = (HttpURLConnection)
            (new URL(url)).openConnection();
        int code = conn.getResponseCode();
        if (code != 200) {
            System.err.println("Unexpected return code: " + code);
            stat.addStatus(TEST_NAME, stat.FAIL);
            return;
        }

        Thread.sleep(15 * 1000);

        url = "http://" + host + ":" + port + contextRoot
                     + "/CheckResult";
        conn = (HttpURLConnection) (new URL(url)).openConnection();
        code = conn.getResponseCode();
        if (code != 200) {
            System.err.println("Unexpected return code: " + code);
            stat.addStatus(TEST_NAME, stat.FAIL);
            return;
        }

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
