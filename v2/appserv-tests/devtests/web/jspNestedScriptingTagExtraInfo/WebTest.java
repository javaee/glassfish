import java.io.*;
import java.net.*;
import com.sun.ejte.ccl.reporter.*;

/*
 * Unit test for Bugzilla 27665 ("Nested tags with scripting variables
 * generates invalid code")
 */
public class WebTest {

    private static final String TEST_NAME = "jsp-nested-scripting-tag-extra-info";

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
        stat.addDescription("Unit test for Bugzilla 27665");
        WebTest webTest = new WebTest(args);
        webTest.doTest();
	stat.printSummary();
    }

    public void doTest() {

        BufferedReader bis = null;
        try { 
            URL url = new URL("http://" + host  + ":" + port
                       + contextRoot + "/jsp//test.jsp");
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
                boolean foundInner = false;
                boolean foundOuter = false;
                boolean fail = false;
                while ((line = bis.readLine()) != null) {
                    if (line.startsWith("  Outer")) {
                        if (foundOuter) {
                            // "Outer:" must occur only once
                            fail = true;
                        } else {
                            foundOuter = true;
                            if (!line.equals("  Outer: 1")) {
                                fail = true;
                            }
                        }
                    } else if (line.startsWith("  Inner")) {
                        if (foundInner) {
                            // "Inner:" must occur only once
                            fail = true;
                        } else {
                            foundInner = true;
                            if (!line.equals("  Inner: 2")) {
                                fail = true;
                            }
                        }
                    }
                }

                if (!foundInner || !foundOuter || fail) {
                    System.err.println("Wrong response");
                    stat.addStatus(TEST_NAME, stat.FAIL);
                } else {
                    stat.addStatus(TEST_NAME, stat.PASS);
                }
            }
        } catch (Exception ex) {
            stat.addStatus(TEST_NAME, stat.FAIL);
            ex.printStackTrace();
        } finally {
            try {
                if (bis != null) bis.close();
            } catch (IOException ex) {}
        }
    }

}
