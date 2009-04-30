import java.io.*;
import java.net.*;
import com.sun.ejte.ccl.reporter.*;

/**
 * Unit test for alternate docroot support of webapps.
 *
 * This test configures the webapp's sun-web.xml with the following
 * alternate docroot properties:
 *
 * Exact match:
 *
 *   <property
 *     name="alternatedocroot_1"
 *     value="from=/domain.xml dir=/tmp/tmpDir/config"/>
 *
 * Extension match:
 *
 *   <property
 *     name="alternatedocroot_2"
 *     value="from=*.policy dir=/tmp/tmpDir/config"/>
 *
 * Path prefix match:
 *
 *   <property
 *     name="alternatedocroot_3"
 *     value="from=/config/* dir=/tmp/tmpDir"/>
 *
 * and then ensures that a request with a URI of the form
 *   "/<context-root>/domain.xml"
 * is mapped to:
 *   /tmp/tmpDir/config/domain.xml
 * (because of "exact match"),
 *
 * a second request with a URI of the form
 *   "/<context-root>/server.policy"
 * is mapped to:
 *   /tmp/tmpDir/config/server.policy
 * (because of "extension match"),
 *
 * and a third request with a URI of the form
 *  "/<context-root>/config/login.conf"
 * is mapped to:
 *   /tmp/tmpDir/config/login.conf
 * (because of "path prefix match").
 *
 * If it were not for the alternate docroots, the above requests would have
 * all resulted in 404 responses, since the requested resources have not
 * been bundled with the webapp.
 */
public class WebTest {

    private static SimpleReporterAdapter stat
        = new SimpleReporterAdapter("appserv-tests");

    private static final String TEST_NAME = "webapp-alternate-docroot";

    private String host;
    private String port;
    private String contextRoot;

    public WebTest(String[] args) {
        host = args[0];
        port = args[1];
        contextRoot = args[2];
    }
    
    public static void main(String[] args) {
        stat.addDescription("Unit test for alternate docroot support");
        WebTest webTest = new WebTest(args);
        webTest.doTest();
        stat.printSummary(TEST_NAME);
    }

    public void doTest() {     
        try { 
            invoke("/domain.xml", "<domain ");
            invoke("/server.policy", "grant codeBase");
            invoke("/config/login.conf", "fileRealm");
            stat.addStatus(TEST_NAME, stat.PASS);
        } catch (Exception ex) {
            stat.addStatus(TEST_NAME, stat.FAIL);
            ex.printStackTrace();
        }
    }

    /*
     * @param uri The URI to connect to
     * @param expected The string that must be present in the returned contents
     * in order for the test to pass
     */
    private void invoke(String uri, String expected) throws Exception {
        
        URL url = new URL("http://" + host  + ":" + port + contextRoot + uri);
        System.out.println("Connecting to: " + url.toString());
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.connect();

        int responseCode = conn.getResponseCode();
        if (responseCode != 200) {
            throw new Exception("Wrong response code. Expected: 200"
                                + ", received: " + responseCode);
        }

        InputStream is = null;
        BufferedReader input = null;
        try {
            is = conn.getInputStream();
            input = new BufferedReader(new InputStreamReader(is));
            String line = null;
            while ((line = input.readLine()) != null) {
                // Search resource contents for expected string
                if (line.contains(expected)) {
                    break;
                }
            }

            if (line == null) {
                throw new Exception("Missing content for " + uri);
            }
        } finally {
            try {
                if (is != null) {
                    is.close();
                }
            } catch (IOException ioe) {
                // ignore
            }
            try {
                if (input != null) {
                    input.close();
                }
            } catch (IOException ioe) {
                // ignore
            }
        }
    }
}
