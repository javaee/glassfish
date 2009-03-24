import java.io.*;
import java.net.*;
import com.sun.ejte.ccl.reporter.*;

/*
 * Unit test for:
 *
 *   https://glassfish.dev.java.net/issues/show_bug.cgi?id=7426
 *   ("Allow web applications to configure session tracking cookie
 *   path dynamically (depending on incoming request)")
 */
public class WebTest {

    private static final String TEST_NAME = "context-valve-set-requested-session-cookie-path";

    private static SimpleReporterAdapter stat =
        new SimpleReporterAdapter("appserv-tests");

    private String host;
    private String port;
    private String contextRoot;

    public WebTest(String[] args) {
        host = args[0];
        port = args[1];
        contextRoot = args[2];
    }
    
    public static void main(String[] args) {

        stat.addDescription("Unit test for IT 7426");
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

        String sessionCookie = conn.getHeaderField("Set-Cookie");
        System.out.println("Response cookie: " + sessionCookie);

        if (sessionCookie == null) {
            throw new Exception("Missing Set-Cookie response header");
        }

        if (sessionCookie.indexOf("Path=" + contextRoot + contextRoot) == -1) {
            throw new Exception("Missing cookie path");
        }
    }
}
