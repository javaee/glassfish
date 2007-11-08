import java.io.*;
import java.net.*;

import com.sun.ejte.ccl.reporter.*;

/**
 * Unit test for 6190900 ("httplistener created with --enabled=false doesn't
 * disable the listener").
 *
 * See also https://glassfish.dev.java.net/issues/show_bug.cgi?id=1301
 * ("Change semantics of <http-listener> "enabled" attribute to truely
 * reflect an HTTP listener's state").
 */
public class WebTest{

    private static final String TEST_NAME = "listener-disabled";

    private static final SimpleReporterAdapter stat =
        new SimpleReporterAdapter("appserv-tests");
    
    public static void main(String args[]) throws Exception {

        String host = args[0];
        String port = "8079";
        String contextRoot = args[2];

        stat.addDescription("Ensure disabled HTTP listener gives ConnectException");
            
        URL url = new URL("http://" + host  + ":" + port + contextRoot
                          + "/ServletTest");
        System.out.println("Invoking url: " + url.toString());

        try {            
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.getResponseCode();
            System.err.println("Expected java.net.ConnectException: "
                               + "Connection refused");
            stat.addStatus(TEST_NAME, stat.FAIL);
        } catch (ConnectException e) {
            stat.addStatus(TEST_NAME, stat.PASS);
        }

	stat.printSummary();
    }
}
