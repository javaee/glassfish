import java.io.*;
import java.net.*;
import com.sun.ejte.ccl.reporter.*;

/*
 * Unit test for for JSP MR2: jsp-config: default-content-type
 */
public class WebTest {

    private static final String TEST_NAME
        = "jsp-config-default-content-type";

    private static SimpleReporterAdapter stat
        = new SimpleReporterAdapter("appserv-tests");

    private String host;
    private String port;
    private String contextRoot;

    private static String expected[] = {
        "text/xhtml"
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
        } finally {
            try {
                if (bis != null) bis.close();
            } catch (IOException ex) {}
        }
    }

}
