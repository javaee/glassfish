import java.io.*;
import java.net.*;
import com.sun.ejte.ccl.reporter.*;

/*
 * Unit test for 6191757 ("NotSerializableException in
 * com.sun.appserv:type=servlet,category=monitor")
 *
 * This test requires that security manager be disabled (see build.xml).
 */
public class WebTest {

    private static SimpleReporterAdapter stat
        = new SimpleReporterAdapter("appserv-tests");

    private static final String TEST_NAME
        = "serializable-webModule-and-servlet-stats";

    private String host;
    private String port;
    private String contextPath;

    public WebTest(String[] args) {
        host = args[0];
        port = args[1];
        contextPath = args[2];
    }
    
    public static void main(String[] args) {
        stat.addDescription("Unit test for 6191757");
        WebTest webTest = new WebTest(args);
        webTest.doTest();
        stat.printSummary(TEST_NAME);
    }

    public void doTest() {     
        try { 
            invokeServlet();
        } catch (Exception ex) {
            System.out.println(TEST_NAME + " test failed.");
            stat.addStatus(TEST_NAME, stat.FAIL);
            ex.printStackTrace();
        }
    }

    private void invokeServlet() throws Exception {

        URL url = new URL("http://" + host  + ":" + port + contextPath
                          + "/TestServlet?contextPath=" + contextPath);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();

        conn.connect();

        int responseCode = conn.getResponseCode();
        if (responseCode != 200) { 
            System.out.println("Wrong response code. Expected: 200"
                               + ", received: " + responseCode);
            stat.addStatus(TEST_NAME, stat.FAIL);
        } else {
            stat.addStatus(TEST_NAME, stat.PASS);
        }
    }
}
