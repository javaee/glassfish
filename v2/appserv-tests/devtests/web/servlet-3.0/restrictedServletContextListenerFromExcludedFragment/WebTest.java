import java.io.*;
import java.net.*;
import com.sun.ejte.ccl.reporter.*;

/*
 * Unit test for
 *
 *  https://glassfish.dev.java.net/issues/show_bug.cgi?id=8565
 *  ("[SPEC] Special treatment of ServletContextListeners declared
 *  in TLDs of web-fragment JAR files omitted from <absolute-ordering>")
 */
public class WebTest {

    private static SimpleReporterAdapter stat =
        new SimpleReporterAdapter("appserv-tests");

    private static final String TEST_NAME =
        "restricted-servlet-context-listener-from-excluded-fragment";

    private String host;
    private String port;
    private String contextRoot;

    public WebTest(String[] args) {
        host = args[0];
        port = args[1];
        contextRoot = args[2];
    }
    
    public static void main(String[] args) {
        stat.addDescription("Unit test for IT 8565");

        try {
            new WebTest(args).doTest();
            stat.addStatus(TEST_NAME, stat.PASS);
        } catch (Exception ex) {
            ex.printStackTrace();
            stat.addStatus(TEST_NAME, stat.FAIL);
        }

        stat.printSummary();
    }

    private void doTest() throws Exception {
        
        String url = "http://" + host + ":" + port + contextRoot + "/fragmentServlet";
        HttpURLConnection conn = (HttpURLConnection)
            (new URL(url)).openConnection();
        int code = conn.getResponseCode();
        if (code != 200) {
            throw new Exception("Unexpected return code: " + code);
        }
    }
}
