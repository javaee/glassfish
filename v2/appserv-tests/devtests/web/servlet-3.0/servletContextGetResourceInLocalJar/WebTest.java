import java.io.*;
import java.net.*;
import com.sun.ejte.ccl.reporter.*;

/*
 * Unit test for ServletContext#getResource, where the requested resource
 * is embedded in a JAR file inside WEB-INF/lib and interpreted as relative
 * to the META-INF/resources directory of the JAR file
 */
public class WebTest {

    private static SimpleReporterAdapter stat =
        new SimpleReporterAdapter("appserv-tests");
    private static final String TEST_NAME =
        "servlet-context-get-resource-in-local-jar";

    private String host;
    private String port;
    private String contextRoot;

    public WebTest(String[] args) {
        host = args[0];
        port = args[1];
        contextRoot = args[2];
    }
    
    public static void main(String[] args) {
        stat.addDescription("Unit test for ServletContext#getResource");
        new WebTest(args).doTest();
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
        URL url = new URL("http://" + host  + ":" +
                          port + contextRoot + "/test");
        System.out.println("Invoking URL: " + url.toString());
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.connect();
        int responseCode = conn.getResponseCode();
        if (responseCode != 200) {
            throw new Exception("Unexpected response code: " + responseCode);
        }
    }
}
