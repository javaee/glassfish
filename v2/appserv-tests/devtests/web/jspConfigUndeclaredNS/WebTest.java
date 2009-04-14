import java.io.*;
import java.net.*;
import com.sun.ejte.ccl.reporter.*;

/*
 * Unit test for for JSP MR2: property group: error-on-undeclared-namespace
 */
public class WebTest {

    private static final String TEST_NAME
        = "jsp-config-error-on-undeclared-namespace";

    private static SimpleReporterAdapter stat
        = new SimpleReporterAdapter("appserv-tests");

    private String host;
    private String port;
    private String contextRoot;

    private static String expected[] = {
        "<foo:bar/>"
    };

    public WebTest(String[] args) {
        host = args[0];
        port = args[1];
        contextRoot = args[2];
    }
    
    public static void main(String[] args) {
        stat.addDescription("Unit test for jsp:attribute with omit");
        WebTest webTest = new WebTest(args);
        webTest.doTest();
        webTest.doTest2();
	stat.printSummary();
    }

    public void doTest() {

        // An error should be raised with undeclared tag
     
        try { 
            URL url = new URL("http://" + host  + ":" + port
                              + contextRoot + "/jsp/test.jsp");
            System.out.println("Connecting to: " + url.toString());
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.connect();
            int responseCode = conn.getResponseCode();
            if (responseCode != 500) { 
                System.err.println("Wrong response code. Expected: 200"
                                   + ", received: " + responseCode);
                stat.addStatus(TEST_NAME, stat.FAIL);
            } else {
                stat.addStatus(TEST_NAME, stat.PASS);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            stat.addStatus(TEST_NAME, stat.FAIL);
        }
    }

    public void doTest2() {

        try {
            URL url = new URL("http://" + host  + ":" + port
                              + contextRoot + "/jsp/test2.jsp");
            System.out.println("Connecting to: " + url.toString());
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.connect();
            int responseCode = conn.getResponseCode();
            if (responseCode != 200) {
                System.err.println("Wrong response code. Expected: 200"
                                   + ", received: " + responseCode);
                stat.addStatus(TEST_NAME, stat.FAIL);
            } else {

                BufferedReader bis = new BufferedReader(
                    new InputStreamReader(conn.getInputStream()));
                String line = null;
                int index = 0;
                while ((line = bis.readLine()) != null) {
                    if (line.trim().length() == 0)
                        continue;
                    if (!line.equals(expected[index++])) {
                        System.err.println("Wrong response: " + line
                                       + ", expected: " + expected[index]);
                        stat.addStatus(TEST_NAME, stat.FAIL);
                        return;
                    }
                }
                stat.addStatus(TEST_NAME, stat.PASS);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            stat.addStatus(TEST_NAME, stat.FAIL);
        }
    }
}
