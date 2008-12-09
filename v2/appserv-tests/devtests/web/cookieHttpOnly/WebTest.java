import java.io.*;
import java.net.*;
import com.sun.ejte.ccl.reporter.*;

/*
 * Unit test for HttpOnly cookies
 */
public class WebTest {

    private static SimpleReporterAdapter stat
        = new SimpleReporterAdapter("appserv-tests");
    private static final String TEST_NAME = "cookie-http-only";

    private String host;
    private String port;
    private String contextRoot;

    public WebTest(String[] args) {
        host = args[0];
        port = args[1];
        contextRoot = args[2];
    }
    
    public static void main(String[] args) {
        stat.addDescription("Unit test for HttpOnly cookies");
        WebTest webTest = new WebTest(args);
        webTest.doTest();
        stat.printSummary(TEST_NAME);
    }

    public void doTest() {
        try { 
            invokeServlet();
            stat.addStatus(TEST_NAME, stat.PASS);
        } catch (Exception ex) {
            stat.addStatus(TEST_NAME, stat.FAIL);
            ex.printStackTrace();
        }
    }

    private void invokeServlet() throws Exception {

        URL url = new URL("http://" + host  + ":" + port + contextRoot
                          + "/setHttpOnly");
        System.out.println("Invoking URL: " + url.toString());

        URLConnection conn = url.openConnection();
        String cookie = conn.getHeaderField("Set-Cookie");
        System.out.println("Response Set-Cookie: " + cookie);
        if (!"abc=def; HttpOnly".equals(cookie)) {
            throw new Exception("Response cookie not HttpOnly");
        }
    }
}
