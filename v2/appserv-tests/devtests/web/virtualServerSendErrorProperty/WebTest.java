import java.io.*;
import java.net.*;
import com.sun.ejte.ccl.reporter.*;

/**
 * Unit test for 6324911 ("can not migrate all virtual server functions
 * from 7.1 to 8.1. (eg: custom error page)").
 *
 * The supporting build.xml assigns the following property to the virtual
 * server named "server":
 *
 *   send-error="path=default-web.xml reason=MY404 code=404"
 *
 * As a result of this setting, any 404 response must have a reason string of
 * MY404, and must provide the contents of the
 * domains/domain1/config/default-web.xml file in its body.
 *
 * The code below does not check the entire response body. Instead, it only
 * checks for the presence of a line that starts with "<web-app xmlns=",
 * which is contained in default-web.xml, and uses its presence as an
 * indication that the response contains the expected body.
 */
public class WebTest {

    private static SimpleReporterAdapter stat
        = new SimpleReporterAdapter("appserv-tests");

    private static final String TEST_NAME =
        "virtual-server-send-error-property";

    private String host;
    private String port;
    private String contextRoot;

    public WebTest(String[] args) {
        host = args[0];
        port = args[1];
        contextRoot = args[2];
    }
    
    public static void main(String[] args) {
        stat.addDescription("Unit test for 6324911");
        WebTest webTest = new WebTest(args);
        webTest.doTest();
        stat.printSummary(TEST_NAME);
    }

    public void doTest() {     
        try { 
            invokeServlet();
        } catch (Exception ex) {
            stat.addStatus(TEST_NAME, stat.FAIL);
            ex.printStackTrace();
        }
    }

    private void invokeServlet() throws Exception {
        
        Socket sock = new Socket(host, new Integer(port).intValue());
        OutputStream os = sock.getOutputStream();
        String get = "GET /nonexistent HTTP/1.0\n";
        System.out.println(get);
        os.write(get.getBytes());
        os.write("\n".getBytes());
        
        InputStream is = sock.getInputStream();
        BufferedReader bis = new BufferedReader(new InputStreamReader(is));

        boolean statusHeaderFound = false;
        boolean bodyLineFound = false;
        String line = null;
        while ((line = bis.readLine()) != null) {
            System.out.println(line);
            if ("HTTP/1.1 404 MY404".equals(line)) {
                statusHeaderFound = true;
            }
            if (line.startsWith("<web-app xmlns=")) {
                bodyLineFound = true;
            }
        }

        if (statusHeaderFound && bodyLineFound) {
            stat.addStatus(TEST_NAME, stat.PASS);
        } else {
            System.err.println("Missing response status or body line");
            stat.addStatus(TEST_NAME, stat.FAIL);
        }

    }
}
