import java.io.*;
import java.net.*;
import com.sun.ejte.ccl.reporter.*;

/*
 * Allow custom tag attributes of type array to be specified as
 * "[L<classname>;" in TLD.
 */
public class WebTest {

    private static final String TEST_NAME
        = "jsp-custom-tag-attribute-of-array-type";

    private static final String EXPECTED = "ARGS:aaabbbccc";

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
                System.err.println("Wrong response code. Expected: 200"
                                   + ", received: " + responseCode);
                stat.addStatus(TEST_NAME, stat.FAIL);
            } else {

                bis = new BufferedReader(
                    new InputStreamReader(conn.getInputStream()));
                String line = null;
                String lastLine = null;
                while ((line = bis.readLine()) != null) {
                    lastLine = line;
                }

                if (!EXPECTED.equals(lastLine)) {
                    System.err.println("Wrong response body. Expected: " 
                                       + EXPECTED + ", received: " + lastLine);
                    stat.addStatus(TEST_NAME, stat.FAIL);
                } else {
                    stat.addStatus(TEST_NAME, stat.PASS);
                }
            }

        } catch (Exception ex) {
            ex.printStackTrace();
            stat.addStatus(TEST_NAME, stat.FAIL);
        } finally {
            try {
                if (bis != null) bis.close();
            } catch (IOException ex) {}
        }
    }

}
