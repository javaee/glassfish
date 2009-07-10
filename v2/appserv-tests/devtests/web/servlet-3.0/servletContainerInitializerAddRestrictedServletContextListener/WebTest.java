import java.io.*;
import java.net.*;
import com.sun.ejte.ccl.reporter.*;

/**
 * Unit test for
 *
 *  https://glassfish.dev.java.net/issues/show_bug.cgi?id=8565
 *  ("[SPEC] Restricted ServletContextListeners")
 *
 * This unit test declares a ServletContainerInitializer, which registers
 * a ServletContextListener, which in turn adds a ServletContext attribute
 * when it is invoked at its contextInitialized method. The test then accesses
 * a Servlet which checks for the presence of this ServletContext attribute,
 * and will throw an Exception if the attribute is missing.
 *
 * This unit test also makes sure that the ServletContextListener registered
 * by the ServletContainerInitializer is restricted by checking that any
 * attempt by it to register a Servlet will fail and result in an
 * IllegalStateException.
 */
public class WebTest {

    private static SimpleReporterAdapter stat =
        new SimpleReporterAdapter("appserv-tests");

    private static final String TEST_NAME =
        "servlet-container-initializer-add-restricted-servlet-context-listener";

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
        
        String url = "http://" + host + ":" + port + contextRoot + "/test";
        HttpURLConnection conn = (HttpURLConnection)
            (new URL(url)).openConnection();
        int code = conn.getResponseCode();
        if (code != 200) {
            throw new Exception("Unexpected return code: " + code);
        }
    }
}
