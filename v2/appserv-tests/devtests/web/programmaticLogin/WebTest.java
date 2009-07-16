import java.io.*;
import java.net.*;

import com.sun.ejte.ccl.reporter.*;

/**
 * Unit test for 4862098 ("ProgrammaticLogin fails for webcontainer -
 * regression on AS8")
 */
public class WebTest {
    
    private static final String TEST_NAME = "programmatic-login";

    private static final SimpleReporterAdapter stat =
        new SimpleReporterAdapter("appserv-tests");

    private String host;
    private String port;
    private String contextRoot;
    private String user;
    private String password;

    public WebTest(String[] args) {
        host = args[0];
        port = args[1];
        contextRoot = args[2];
        user = args[3];
        password = args[4];
    }

    public static void main(String[] args) {

        stat.addDescription("Unit test for 4862098");
        
        WebTest webTest = new WebTest(args);
        
        try {
            webTest.invoke();            
            stat.addStatus(TEST_NAME, stat.PASS);
        } catch (Throwable t) {
            stat.addStatus(TEST_NAME, stat.FAIL);
            t.printStackTrace();
        }

        stat.printSummary(TEST_NAME);
    }

    private void invoke() throws Exception {

        String uri = contextRoot +
            "/ServletTest?user=testuser3&password=secret";
        URL url = new URL("http://" + host  + ":" + port + uri);
        System.out.println("Connecting to: " + url.toString());

        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.connect();

        int responseCode = conn.getResponseCode();
        if (responseCode != 200) {
            throw new Exception("Wrong response code. Expected: 200" +
                ", received: " + responseCode);
        }
   }
  
}
