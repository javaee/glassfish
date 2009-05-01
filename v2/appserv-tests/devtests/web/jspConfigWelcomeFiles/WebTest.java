import java.io.*;
import java.net.*;
import com.sun.ejte.ccl.reporter.*;

/*
 * Unit test for Bugzilla 27664 ("Welcome files not found in combination with
 * jsp-property-group").
 *
 * See http://nagoya.apache.org/bugzilla/show_bug.cgi?id=27664 for details.
 */
public class WebTest {

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
        stat.addDescription("Unit test for Bugzilla 27664");
        WebTest webTest = new WebTest(args);
        webTest.doTest("http://" + webTest.host  + ":" + webTest.port
                       + webTest.contextRoot + "/subdir1/subdir2/",
                       "jsp-config-welcome-files");
        webTest.doTest("http://" + webTest.host  + ":" + webTest.port
                       + webTest.contextRoot + "/TestServlet",
                       "jsp-config-welcome-files-request-dispatcher");
	stat.printSummary();
    }

    public void doTest(String urlString, String testName) {

        InputStream is = null;
        BufferedReader input = null;
        try { 
            URL url = new URL(urlString);
            System.out.println("Connecting to: " + url.toString());
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.connect();
            int responseCode = conn.getResponseCode();
            if (responseCode != 200) { 
                System.err.println("Wrong response code. Expected: 200"
                                   + ", received: " + responseCode);
                stat.addStatus(testName, stat.FAIL);
            } else {
                is = conn.getInputStream();
                input = new BufferedReader(new InputStreamReader(is));
                String line = input.readLine();
                if (!"Welcome".equals(line)) {
                    System.err.println("Wrong response. Expected: Welcome"
                                       + ", received: " + line);
                    stat.addStatus(testName, stat.FAIL);
                } else {
                    stat.addStatus(testName, stat.PASS);
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            stat.addStatus(testName, stat.FAIL);
        } finally {
            try {
                if (is != null) is.close();
            } catch (IOException ex) {}
            try {
                if (input != null) input.close();
            } catch (IOException ex) {}
        }
    }

}
