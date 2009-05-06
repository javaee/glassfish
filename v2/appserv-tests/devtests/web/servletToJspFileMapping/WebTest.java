import java.io.*;
import java.net.*;
import com.sun.ejte.ccl.reporter.*;

/**
 * Unit test for Bugtraq 4924326 ("Diff servlets that map to the same
 * jsp-file use the same JMX registration name").
 */
public class WebTest {

    private static final String TEST_NAME = "web-servlet-jspfile-mapping";

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
        stat.addDescription("Unit test for Bugtraq 4924326");
        WebTest webTest = new WebTest(args);
        try {
            webTest.doTest();
            stat.addStatus(TEST_NAME, stat.PASS);
        } catch (Exception ex) {
            stat.addStatus(TEST_NAME, stat.FAIL);
            ex.printStackTrace();
        }
        stat.printSummary(TEST_NAME);
    }

    public void doTest() throws Exception {
     
        String url = "http://" + host + ":" + port + "/" + contextRoot;
        int code = invokeServlet(url + "/TestServlet1");
        if (code != 200) {
            throw new Exception("Incorrect return code: " + code);
        }

        code = invokeServlet(url + "/TestServlet2");
        if (code != 200) {
            throw new Exception("Incorrect return code: " + code);
        }
    }

    private int invokeServlet(String url) throws Exception {
            
        System.out.println("Invoking servlet at: " + url);

        HttpURLConnection conn = (HttpURLConnection)
            (new URL(url)).openConnection();
        int code = conn.getResponseCode();

        InputStream is = null;
        BufferedReader input = null;
        try {
            is = conn.getInputStream();
            input = new BufferedReader(new InputStreamReader(is));
            String line = null;
            while ((line = input.readLine()) != null) {
                System.out.println(line);
            }
        } finally {
            try {
                if (is != null) {
                    is.close();
                }
            } catch (IOException ioe) {
                // ignore
            }
            try {
                if (input != null) {
                    input.close();
                }
            } catch (IOException ioe) {
                // ignore
            }
        }

        return code;
    }
}
