import java.io.*;
import java.net.*;
import com.sun.ejte.ccl.reporter.*;

/**
 * Unit test for 6573545 ("virtual is not active after creation and needs
 * restart of the instance")
 */
public class WebTest {

    private static SimpleReporterAdapter stat
        = new SimpleReporterAdapter("appserv-tests");

    private static final String TEST_NAME
        = "virtual-server-update-http-listeners-dynamic-reconfig";

    private static final String EXPECTED = "Success!";

    private String host;
    private String port;
    private String contextRoot;

    public WebTest(String[] args) {
        host = args[0];
        port = args[1];
        contextRoot = args[2];
    }
    
    public static void main(String[] args) {
        stat.addDescription("Unit test for 6573545");
        WebTest webTest = new WebTest(args);
        webTest.doTest();
        stat.printSummary(TEST_NAME);
    }

    public void doTest() {     
        try { 
            invoke();
        } catch (Exception ex) {
            stat.addStatus(TEST_NAME, stat.FAIL);
            ex.printStackTrace();
        }
    }

    private void invoke() throws Exception {
        
        Socket sock = new Socket(host, new Integer(port).intValue());
        OutputStream os = sock.getOutputStream();
        String get = "GET " + contextRoot + "/test.txt HTTP/1.1\n";
        System.out.println(get);
        os.write(get.getBytes());
        os.write("Host: vs2\n".getBytes());
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

        if (line != null) {
            stat.addStatus(TEST_NAME, stat.PASS);
        } else {
            System.err.println("Missing expected response: " + EXPECTED);
            stat.addStatus(TEST_NAME, stat.FAIL);
        }
    }
}
