import java.io.*;
import java.net.*;
import com.sun.ejte.ccl.reporter.*;

/**
 * Unit test for:
 *
 *  https://glassfish.dev.java.net/issues/show_bug.cgi?id=6043
 *  ("apps on new virtual servers with existing http-listeners fail")
 *
 * Notice how the id of the newly created virtual server starts with an
 * upper-case letter ("TestServer").
 */
public class WebTest {

    private static SimpleReporterAdapter stat
        = new SimpleReporterAdapter("appserv-tests");

    private static final String TEST_NAME = "virtual-server-create-upper-case-deploy-exclusive";

    private static final String EXPECTED = "This is my personal welcome page!";

    private String host;
    private String port;
    private String contextRoot;

    public WebTest(String[] args) {
        host = args[0];
        port = args[1];
        contextRoot = args[2];
    }
    
    public static void main(String[] args) {
        stat.addDescription("Unit test for GlassFish Issue 6043");
        WebTest webTest = new WebTest(args);
        webTest.doTest();
        stat.printSummary(TEST_NAME);
    }

    public void doTest() {     
        try { 
            invoke();
            stat.addStatus(TEST_NAME, stat.PASS);
        } catch (Exception ex) {
            stat.addStatus(TEST_NAME, stat.FAIL);
            ex.printStackTrace();
        }
    }

    private void invoke() throws Exception {
    
        Socket sock = new Socket(host, new Integer(port).intValue());
        OutputStream os = sock.getOutputStream();
        String get = "GET " + contextRoot + "/ HTTP/1.0\n";
        System.out.println(get);
        os.write(get.getBytes());
        os.write("Host: abc\n".getBytes());
        os.write("\n".getBytes());

        InputStream is = sock.getInputStream();
        BufferedReader bis = new BufferedReader(new InputStreamReader(is));

        String line = null;
        while ((line = bis.readLine()) != null) {
            System.out.println(line);
            if (line.equals(EXPECTED)) {
                break;
            }
        }

        bis.close();

        if (line == null) {
            throw new Exception("Unable to find expected response: " +
                                EXPECTED);
        }
    }
}
