import java.io.*;
import java.net.*;
import com.sun.ejte.ccl.reporter.*;

/*
 * Unit test for
 *
 *   https://glassfish.dev.java.net/issues/show_bug.cgi?id=542
 *   ("WAR from an EAR as default webapp no more working after stop/start")
 *
 * This unit test deploys an EAR, dynamically configures the EAR's embedded
 * WAR as the default-web-module of virtual server "server", accesses the 
 * WAR's JSP resource under both the WAR's context root and the empty context
 * root, stops and restarts the domain, and accesses the WAR's JSP resouce
 * again (under both the WAR's context root and the empty context root).
 */
public class WebTest {

    private static final String TEST_NAME = "virtual-server-ear-wrapped-default-web-module-restart";

    private static final String EXPECTED = "Hello, world!";

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

        stat.addDescription("Unit test for GlassFish Issue 542");
        WebTest webTest = new WebTest(args);

        try {
            webTest.doTest("/mywar/test.jsp");
            webTest.doTest("/test.jsp");
            stat.addStatus(TEST_NAME, stat.PASS);
        } catch (Exception ex) {
            ex.printStackTrace();
            stat.addStatus(TEST_NAME, stat.FAIL);
        }

        stat.printSummary();
    }

    public void doTest(String uri) throws Exception {
     
        URL url = new URL("http://" + host  + ":" + port + uri);
        System.out.println("Connecting to: " + url.toString());
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.connect();
        int responseCode = conn.getResponseCode();
        if (responseCode != 200) { 
            throw new Exception("Wrong response code. Expected: 200" +
                                ", received: " + responseCode);
        } else {
            BufferedReader bis = new BufferedReader(
                new InputStreamReader(conn.getInputStream()));
            String line = null;
            while ((line = bis.readLine()) != null) {
                System.out.println(line);
                if (EXPECTED.equals(line)) {
                    break;
                }
            }
            if (line == null) {
                throw new Exception("Wrong response body. Could not find " +
                                    "expected string: " + EXPECTED);
            }
        }
    }
}
