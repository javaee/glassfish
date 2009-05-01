import java.io.*;
import java.net.*;
import com.sun.ejte.ccl.reporter.*;

/*
 * Unit test for page directive trimDirectiveWhitespaces
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
        stat.addDescription("Unit test for enum support in EL");
        WebTest webTest = new WebTest(args);
        webTest.doTest("/trim.jsp", "jsp-trimDirectiveWhitespaces");
	stat.printSummary();
    }

    public void doTest(String path, String testName) {

        InputStream is = null;
        BufferedReader input = null;
        try {
            URL url = new URL("http://" + host  + ':' + port + '/' + contextRoot + path);
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
                input = new BufferedReader(new InputStreamReader(
is));
                String line1 = input.readLine();
//                System.out.println("line1:" + line1);
                String line2 = input.readLine();
//                System.out.println("line2:" + line2);
                String line3 = input.readLine();
//                System.out.println("line3:" + line3);
                String line4 = input.readLine();
//                System.out.println("line4:" + line4);
                
                if ("".equals(line1) &&
                    "xyz".equals(line2) &&
                    "".equals(line3) &&
                    "<h1>Hello World!</h1>".equals(line4)) {
                    stat.addStatus(testName, stat.PASS);
                } else {
                    stat.addStatus(testName, stat.FAIL);
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
