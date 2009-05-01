import java.io.*;
import java.net.*;
import com.sun.ejte.ccl.reporter.*;

/*
 * Unit test for handling of EL expressions #{...} in tag attributes
 * This includes bugs 6372687, 6377689, 6380354
 *
 * Make sure that is treated as a literal when jspversion of the tld is
 * 2.0 or less.
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
        webTest.doTest("/enum.jsp", "jsp-el-enum-test");
	stat.printSummary();
    }

    private boolean checkValue(BufferedReader input,
                               String expectedValue) 
            throws Exception {
        String line = input.readLine();
        if (line == null)
            return true;
        while ("".equals(line.trim())) {
            line = input.readLine();
            if (line == null)
                return true;
        }
//        System.out.println("###" + line + "$$$");
        if (!line.trim().equals(expectedValue)) {
            System.err.println("Wrong response. Expected value: "
                + expectedValue + ", received: " + line);
            return true;
        }
        return false; 
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
                boolean error = false;
                if (checkValue(input, "PASS"))
                    error = true;
                if (checkValue(input, "PASS"))
                    error = true;
                if (checkValue(input, "diamond"))
                    error = true;
                if (checkValue(input, "diamond"))
                    error = true;
                if (error) {
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
