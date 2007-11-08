import java.io.*;
import java.net.*;
import com.sun.ejte.ccl.reporter.*;

/*
 * Make sure log4j JAR and configuration file packaged in EAR file's lib
 * directory are getting honored.
 */
public class WebTest {

    private static final String TEST_NAME = "log4j-xml-in-ear-file";

    private static final String EXPECTED_RESPONSE =
        "class org.apache.commons.logging.impl.Log4JLogger: "
        + "This is my test log message";

    private static SimpleReporterAdapter stat
        = new SimpleReporterAdapter("appserv-tests");

    private String host;
    private String port;

    public WebTest(String[] args) {
        host = args[0];
        port = args[1];
    }
    
    public static void main(String[] args) {

        stat.addDescription("Unit test for log4j packaged in EAR file");
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
     
        URL url = new URL("http://" + host  + ":" + port + "/mywar/TestServlet");
        System.out.println("Connecting to: " + url.toString());

        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.connect();
        int responseCode = conn.getResponseCode();

        if (responseCode != 200) {
            System.err.println("Unexpected return code: " + responseCode);
            stat.addStatus(TEST_NAME, stat.FAIL);
        } else {
            InputStream is = conn.getInputStream();
            BufferedReader br = new BufferedReader(new InputStreamReader(is));
            String line = br.readLine();
            if (EXPECTED_RESPONSE.equals(line)) {
                stat.addStatus(TEST_NAME, stat.PASS);
            } else {
                System.err.println("Wrong response. Expected: " + 
                                   EXPECTED_RESPONSE + ", received: " + line);
                stat.addStatus(TEST_NAME, stat.FAIL);
            }
        }    
    }
}
