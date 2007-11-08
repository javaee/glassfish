import java.io.*;
import java.net.*;
import com.sun.ejte.ccl.reporter.*;

/*
 * Unit test for Expression.equals().
 * 6524656
 */

public class WebTest {

    private static SimpleReporterAdapter stat
        = new SimpleReporterAdapter("appserv-tests");

    private static String testName = "EL Expression equals test";
    private String host;
    private String port;
    private String contextRoot;

    public WebTest(String[] args) {
        host = args[0];
        port = args[1];
        contextRoot = args[2];
    }

    public static void main(String[] args) {
        stat.addDescription("Unit tests for Expression.equals");
        WebTest webTest = new WebTest(args);

        try {
            webTest.test("/equals.jsp");
        } catch (Exception ex) {
            ex.printStackTrace();
            stat.addStatus(testName, stat.FAIL);                
        }

        stat.printSummary();
    }

    public void test(String path) throws Exception {
        URL url = new URL("http://" + host  + ':' + port + '/' + contextRoot
 + path);
                    System.out.println("Connecting to: " + url.toString());
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.connect();
        int responseCode = conn.getResponseCode();
        if (responseCode != 200) {
            System.err.println("Wrong response code. Expected: 200"
                                   + ", received: " + responseCode);
                stat.addStatus(testName, stat.FAIL);
        } else {
            InputStream is = conn.getInputStream();
            BufferedReader input = new BufferedReader(new InputStreamReader(is));
            boolean error = false;
            String line;
            while ((line = input.readLine()) != null) {
                if (line.trim().length()> 0)
                    System.out.println(line);
                if (line.indexOf("FAIL") > 0)
                    error = true;
            }
            stat.addStatus(testName, error? stat.FAIL: stat.PASS);
        }
    }
}

