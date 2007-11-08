import java.io.*;
import java.net.*;
import com.sun.ejte.ccl.reporter.*;

/*
 * Unit test for:
 *
 *   https://glassfish.dev.java.net/issues/show_bug.cgi?id=1668
 *   (SQE cache tests failed /w PWC6117: File "/jsp/caching/Sun ONE
 *   Application Server Tags" not found)
 *
 * Make sure that during precompilation, the JSP compiler will not 
 * (erroneously) ignore any locally bundled appserv-tags.jar (and cause
 * precompilation and therefore deployment to fail), even though this JAR
 * file is not listed amongst the JAR files known to contain TLD resources
 * (specified via the com.sun.enterprise.taglibs system property in
 * domain.xml).
 */
public class WebTest {

    private static SimpleReporterAdapter stat
        = new SimpleReporterAdapter("appserv-tests");

    private static final String TEST_NAME = "jsp-cache-taglib-precompile";

    private String host;
    private String port;
    private String contextRoot;

    public WebTest(String[] args) {
        host = args[0];
        port = args[1];
        contextRoot = args[2];
    }
    
    public static void main(String[] args) {
        stat.addDescription("Unit test for GlassFish Issue 1668");
        WebTest webTest = new WebTest(args);
        webTest.doTest();
        stat.printSummary(TEST_NAME);
    }

    public void doTest() {     
        try { 
            invoke();
            stat.addStatus(TEST_NAME, stat.PASS);
        } catch (Exception ex) {
            stat.addStatus(TEST_NAME, stat.FAIL);
            ex.printStackTrace();
        }
    }

    private void invoke() throws Exception {
        
        URL url = new URL("http://" + host  + ":" + port + contextRoot
                          + "/jsp/caching/basicCache.jsp");
        System.out.println("Connecting to: " + url.toString());
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.connect();

        int responseCode = conn.getResponseCode();
        if (responseCode != 200) {
            throw new Exception("Wrong response code. Expected: 200"
                                + ", received: " + responseCode);
        }
    }
}
