import java.io.*;
import java.net.*;
import com.sun.ejte.ccl.reporter.*;

/*
 * Make sure a custom tag's release() method is called when tag pooling has
 * been disabled.
 */
public class WebTest {

    private static final String TEST_NAME
        = "jsp-tag-release-called-when-pooling-disabled";

    private static final String EXPECTED = "RELEASE CALLED";

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

        try {
            webTest.doTest();
        } catch (Exception ex) {
            ex.printStackTrace();
            stat.addStatus(TEST_NAME, stat.FAIL);
        }

        stat.printSummary();
    }

    public void doTest() throws Exception {

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
                while ((line = bis.readLine()) != null) {
                    if (EXPECTED.equals(line)) {
                        stat.addStatus(TEST_NAME, stat.PASS);
                        return;
                    }
                }

                System.err.println("Response does not contain expected string: "
                                   + EXPECTED);
                stat.addStatus(TEST_NAME, stat.FAIL);
            }
        } finally {
            try {
                if (bis != null) bis.close();
            } catch (IOException ex) {}
        }
    }

}
