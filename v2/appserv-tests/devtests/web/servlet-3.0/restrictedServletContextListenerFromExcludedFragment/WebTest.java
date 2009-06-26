import java.io.*;
import java.net.*;
import com.sun.ejte.ccl.reporter.*;

/*
 * Unit test for
 *
 *  https://glassfish.dev.java.net/issues/show_bug.cgi?id=8565
 *  ("[SPEC] Special treatment of ServletContextListeners declared
 *  in TLDs of web-fragment JAR files omitted from <absolute-ordering>"):
 *
 * This unit test creates and deploys a WAR file that bundles two web
 * fragments with names webFragment1 and webFragment2, respectively.
 *
 * Either fragment bundles a Tag Library Descriptor (TLD) resource
 * declaring a ServletContextListener.
 *
 * The main web.xml declares an absolute ordering of the fragments, but
 * omits webFragment1 from it.
 * 
 * Either ServletContextListener attempts to register a Servlet. However,
 * since webFragment1 was omitted from the absolute ordering, the call
 * to ServletContext#addServlet by its ServletContextListener must result
 * in an IllegalStateException, as required by the Servlet spec. The
 * ServletContextListener catches this exception and stores it as a
 * ServletContext attribute.
 *
 * On the other hand, the Servlet registration by the ServletContextListener
 * in webFragment2 is expected to succeed, and the client attempts to access
 * this Servlet. When the Servlet is accessed, it checks for the presence of
 * the ServletContext attribute stored by the ServletContextListener of 
 * webFragment1, and throws a ServletException if the attribute is missing,
 * causing the test to fail.
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
