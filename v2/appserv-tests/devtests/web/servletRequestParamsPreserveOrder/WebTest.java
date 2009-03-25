import java.io.*;
import java.net.*;
import com.sun.ejte.ccl.reporter.*;

/*
 * Unit test for 6057385 ("WS6.1 Web Application: form fields out of order,
 * works fine in WS6.0").
 */
public class WebTest {

    private static SimpleReporterAdapter stat
        = new SimpleReporterAdapter("appserv-tests");

    private static final String TEST_NAME =
        "servlet-request-params-preserve-order";
    private static final String EXPECTED_RESPONSE =
        "param1=value1param2=value2param3=value3";

    private String host;
    private String port;
    private String contextRoot;

    public WebTest(String[] args) {
        host = args[0];
        port = args[1];
        contextRoot = args[2];
    }
    
    public static void main(String[] args) {
        stat.addDescription("Unit test for 6057385");

        try {
            new WebTest(args).doTest();
            stat.addStatus(TEST_NAME, stat.PASS);
        } catch (Exception ex) {
            stat.addStatus(TEST_NAME, stat.FAIL);
            ex.printStackTrace();
        }

        stat.printSummary(TEST_NAME);
    }

    public void doTest() throws Exception {
     
        String url = "http://" + host + ":" + port + contextRoot
                    + "/TestServlet?param1=value1&param2=value2&param3=value3";
        HttpURLConnection conn = (HttpURLConnection)
            (new URL(url)).openConnection();

        int code = conn.getResponseCode();
        if (code != 200) {
            throw new Exception("Unexpected return code: " + code);
        } 

        InputStream is = conn.getInputStream();
        BufferedReader input = new BufferedReader(new InputStreamReader(is));
        String line = input.readLine();
        if (!EXPECTED_RESPONSE.equals(line)) {
            throw new Exception("Wrong response. " + "Expected: " +
                                EXPECTED_RESPONSE + ", received: " + line);
        }    
    }
}
