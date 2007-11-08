import java.io.*;
import java.net.*;
import com.sun.ejte.ccl.reporter.*;

/*
 * Unit test for 6251872 ("JSP compilation does not include classes in common
 * classloader (domain1/lib) level"):
 *
 * This test has JSP precompilation during deployment turned ON.
 */
public class WebTest {

    private static final String TEST_NAME =
        "jsp-precompile-consider-domain-lib-jarfiles-and-classes";

    private static SimpleReporterAdapter stat
        = new SimpleReporterAdapter("appserv-tests");

    private String host;
    private String port;
    private String contextRoot;

    public WebTest(String[] args) {
        host = args[0];
        port = args[1];
        contextRoot = args[2];
    }
    
    public static void main(String[] args) {

        stat.addDescription("Unit test for 6251872");
        WebTest webTest = new WebTest(args);

        try {
            webTest.doTest();
        } catch (Exception ex) {
            ex.printStackTrace();
            stat.addStatus(TEST_NAME, stat.FAIL);
        }

	stat.printSummary();
    }

    public void doTest() throws Exception {
     
        URL url = new URL("http://" + host  + ":" + port
                          + contextRoot + "/jsp/test.jsp");
        System.out.println("Connecting to: " + url.toString());
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.connect();

        int responseCode = conn.getResponseCode();
        if (responseCode == 200) {
            stat.addStatus(TEST_NAME, stat.PASS);
        } else {
            System.err.println("Wrong response code. Expected: 200"
                               + ", received: " + responseCode);
            stat.addStatus(TEST_NAME, stat.FAIL);
        }
    }
}
