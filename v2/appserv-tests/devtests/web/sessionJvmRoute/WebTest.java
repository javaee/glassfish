import java.io.*;
import java.net.*;
import com.sun.ejte.ccl.reporter.*;

/*
 * Unit test for:
 *
 *  https://glassfish.dev.java.net/issues/show_bug.cgi?id=3796
 *  ("Add support for AJP/mod_jk load balancing")
 *
 * This unit test defines a system property with name jvmRoute and value
 * MYINSTANCE, invokes a servlet that creates a session, and expects the
 * string ".MYINSTANCE" to be appended to the value of the JSESSIONID response
 * cookie.
 *
 * In a following request, it includes the JSESSIONID from the response
 * and invokes a session that resumes this session. The servlet checks to make
 * sure that the string ".MYINSTANCE" has been removed (by the container) from
 * the session id.
 */
public class WebTest {

    private static SimpleReporterAdapter stat
        = new SimpleReporterAdapter("appserv-tests");
    private static final String TEST_NAME = "session-jvm-route";
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
        stat.addDescription("Unit test for GlassFish Issue 3796");
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

        String cookie = conn.getHeaderField("Set-Cookie");
        System.out.println("Response cookie: " + cookie);
        if (cookie.indexOf(".MYINSTANCE") == -1) {
            System.err.println("Session cookie does not have any JVMROUTE");
            stat.addStatus(TEST_NAME, stat.FAIL);
            return;
        }

        Socket sock = new Socket(host, new Integer(port).intValue());
        OutputStream os = sock.getOutputStream();
        os.write(("GET " + contextRoot + "/CheckSession"
            + " HTTP/1.0\n").getBytes());
        os.write(("Cookie: " + cookie + "\n").getBytes());
        os.write("\n".getBytes());
        
        InputStream is = sock.getInputStream();
        BufferedReader bis = new BufferedReader(new InputStreamReader(is));
        String line = null;
        while ((line = bis.readLine()) != null) {
            if (EXPECTED_RESPONSE.equals(line)) {
                stat.addStatus(TEST_NAME, stat.PASS);
                break;
            }
        }

        if (line == null) {
            stat.addStatus(TEST_NAME, stat.FAIL);
        }
    }
}
