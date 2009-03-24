import java.io.*;
import java.net.*;
import com.sun.ejte.ccl.reporter.*;

/*
 * Unit test for new ServletContext.getContextPath() method
 * (Servlet 2.5 feature).
 */
public class WebTest {

    private static final String TEST_NAME = "servlet-context-get-context-path";

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

        stat.addDescription("Unit test for ServletContext.getContextPath()");
        WebTest webTest = new WebTest(args);

        try {
            webTest.doTest();
            stat.addStatus(TEST_NAME, stat.PASS);
        } catch (Exception ex) {
            ex.printStackTrace();
            stat.addStatus(TEST_NAME, stat.FAIL);
        }

	stat.printSummary();
    }

    public void doTest() throws Exception {
     
        URL url = new URL("http://" + host  + ":" + port + contextRoot
            + "/TestServlet");
        System.out.println("Connecting to: " + url.toString());
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.connect();
        int responseCode = conn.getResponseCode();
        if (responseCode != 200) {
            throw new Exception("Unexpected return code: " + responseCode);
        }

        InputStream is = conn.getInputStream();
        BufferedReader input = new BufferedReader(new InputStreamReader(is));
        String line = input.readLine();
        if (!contextRoot.equals(line)) {
            throw new Exception("Wrong response. Expected: " + 
                                contextRoot + ", received: " + line);
        }    
    }
}
