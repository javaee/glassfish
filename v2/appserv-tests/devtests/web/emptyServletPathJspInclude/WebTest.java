import java.io.*;
import java.net.*;
import com.sun.ejte.ccl.reporter.*;

/*
 * Unit test for Bugzilla 28058 ("JspRuntimeLibrary.getContextRelativePath()
 * can throw StringIndexOutOfBoundsException").
 *
 * See http://issues.apache.org/bugzilla/show_bug.cgi?id=28058 for details.
 */
public class WebTest {

    private static SimpleReporterAdapter stat
        = new SimpleReporterAdapter("appserv-tests");

    private static final String TEST_NAME = "empty-servlet-path-jsp-include";
    private static final String RESPONSE_BODY = "Hello World";

    private String host;
    private String port;
    private String contextRoot;

    public WebTest(String[] args) {
        host = args[0];
        port = args[1];
        contextRoot = args[2];
    }
    
    public static void main(String[] args) {
        stat.addDescription("Unit test for Bugzilla 28058");
        WebTest webTest = new WebTest(args);
        webTest.doTest();
        stat.printSummary(TEST_NAME);
    }

    public void doTest() {     
        try { 
            URL url = new URL("http://" + host  + ":" + port + contextRoot
                              + "/jsp/test.jsp");
            System.out.println("Connecting to: " + url.toString());
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.connect();
            int responseCode = conn.getResponseCode();
            if (responseCode != 200) { 
                System.err.println("Wrong response code. Expected: 200"
                                   + ", received: " + responseCode);
                stat.addStatus(TEST_NAME, stat.FAIL);
            } else {
                InputStream is = null;
                BufferedReader input = null;
                try {
                    is = conn.getInputStream();
                    input = new BufferedReader(new InputStreamReader(is));
                    String line = input.readLine();
                    if (!RESPONSE_BODY.equals(line)) {
                        System.err.println("Wrong response. Expected: "
                                       + RESPONSE_BODY
                                       + ", received: " + line);
                        stat.addStatus(TEST_NAME, stat.FAIL);
                    } else {
                        stat.addStatus(TEST_NAME, stat.PASS);
                    }
                } finally {
                    try {
                        if (is != null) is.close();
                    } catch (IOException ex) { }
                    try {
                        if (input != null) input.close();
                    } catch (IOException ex) { }
                }
            }
        } catch (Exception ex) {
            stat.addStatus(TEST_NAME, stat.FAIL);
            ex.printStackTrace();
        }
    }

}
