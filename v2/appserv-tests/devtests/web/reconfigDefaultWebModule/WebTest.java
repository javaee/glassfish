
import java.lang.*;
import java.io.*;
import java.net.*;

import com.sun.ejte.ccl.reporter.*;

public class WebTest {
    
    private static SimpleReporterAdapter stat =
        new SimpleReporterAdapter("appserv-tests");

    private static final String TEST_NAME = "reconfigDefaultWebModule";

    public static void main(String args[]) {

        stat.addDescription("Dynamic virtual-server/listener creation");
        String host = args[0];
        String port = "9090";
        String contextRoot = args[2];

        try {
            URL url = new URL("http://" + host  + ":" + port + "/");
            System.out.println("Invoking url: " + url.toString());
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setDoOutput(true);
            int responseCode=  conn.getResponseCode();
            System.out.println("responseCode: " + responseCode);
            if (responseCode == 200) {
                stat.addStatus(TEST_NAME, stat.PASS);
            } else {
                stat.addStatus(TEST_NAME, stat.FAIL);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            stat.addStatus(TEST_NAME, stat.FAIL);
        }

        stat.printSummary(TEST_NAME);
    }
}
