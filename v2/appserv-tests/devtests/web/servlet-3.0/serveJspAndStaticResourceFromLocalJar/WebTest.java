import java.io.*;
import java.net.*;
import com.sun.ejte.ccl.reporter.*;

/*
 * Unit test for serving JSP and static resources from
 * WEB-INF/lib/[*.jar]/META-INF/resources
 *
 * In this unit test, the client makes a request for
 *   http://localhost:8080/abc.jsp
 * and
 *   http://localhost:8080/abc.txt
 * and the requested resource is supposed to be served from
 *   WEB-INF/lib/nested.jar!META-INF/resources/abc.jsp
 * (by the JspServlet) and
 *   WEB-INF/lib/nested.jar!META-INF/resources/abc.txt
 * (by the DefaultServlet), respectively.
 */
public class WebTest {

    private static SimpleReporterAdapter stat =
        new SimpleReporterAdapter("appserv-tests");
    private static final String TEST_NAME =
        "serve-jsp-and-static-resource-from-local-jar";

    private String host;
    private String port;
    private String contextRoot;

    public WebTest(String[] args) {
        host = args[0];
        port = args[1];
        contextRoot = args[2];
    }
    
    public static void main(String[] args) {
        stat.addDescription("Unit test for serving JSP and static " +
                            "resources from JAR inside WEB-INF/lib");
        new WebTest(args).doTest();
        stat.printSummary(TEST_NAME);
    }

    public void doTest() {
        try { 
            invokeJspServlet();
            //invokeDefaultServlet();
            stat.addStatus(TEST_NAME, stat.PASS);
        } catch (Exception ex) {
            stat.addStatus(TEST_NAME, stat.FAIL);
            ex.printStackTrace();
        }
    }

    private void invokeJspServlet() throws Exception {
        URL url = new URL("http://" + host  + ":" +
                          port + contextRoot + "/abc.jsp");
        System.out.println("Invoking URL: " + url.toString());
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.connect();
        int responseCode = conn.getResponseCode();
        if (responseCode != 200) {
            throw new Exception("Unexpected response code: " + responseCode);
        }
    }

    private void invokeDefaultServlet() throws Exception {
        URL url = new URL("http://" + host  + ":" +
                          port + contextRoot + "/abc.txt");
        System.out.println("Invoking URL: " + url.toString());
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.connect();
        int responseCode = conn.getResponseCode();
        if (responseCode != 200) {
            throw new Exception("Unexpected response code: " + responseCode);
        }
    }
}
