import java.io.*;
import java.net.*;
import com.sun.ejte.ccl.reporter.*;

/*
 * Unit test for Bugtraq 4869146:
 *
 * Make sure we generate code for scriptlet declarations nested inside custom
 * actions.
 */
public class WebTest {

    private static final String TEST_NAME
                    = "jsp-scriptlet-decl-nested-in-custom-action";
    private static final String EXPECTED = "Counter value: 1";

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
        stat.addDescription("Unit test for Bugtraq 4869146");
        WebTest webTest = new WebTest(args);
        webTest.doTest();
        stat.printSummary();
    }

    public void doTest() {

        BufferedReader bis = null;
        try {

            URL url = new URL("http://" + host  + ":" + port
                       + contextRoot + "/jsp/test.jsp");
            System.out.println("Connecting to: " + url.toString());
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.connect();
            int responseCode = conn.getResponseCode();
            if (responseCode != 200) { 
                stat.addStatus("Wrong response code. Expected: 200"
                               + ", received: " + responseCode, stat.FAIL);
            } else {

                bis = new BufferedReader(
                    new InputStreamReader(conn.getInputStream()));
                String line = null;
                String lastLine = null;
                while ((line = bis.readLine()) != null) {
                    lastLine = line;
                }

                if (!EXPECTED.equals(lastLine)) {
                    stat.addStatus("Wrong response body. Expected: " 
                                   + EXPECTED + ", received: " + lastLine,
                                   stat.FAIL);
                } else {
                    stat.addStatus(TEST_NAME, stat.PASS);
                }
            }

        } catch (Exception ex) {
            System.out.println(TEST_NAME + " test failed.");
            stat.addStatus(TEST_NAME, stat.FAIL);
            ex.printStackTrace();
        } finally {
            try {
                if (bis != null) bis.close();
            } catch (IOException ex) {}
        }
    }

}
