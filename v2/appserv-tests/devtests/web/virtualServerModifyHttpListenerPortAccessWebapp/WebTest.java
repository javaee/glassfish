import java.io.*;
import java.net.*;
import com.sun.ejte.ccl.reporter.*;

/**
 * Unit test for:
 *
 *  https://glassfish.dev.java.net/issues/show_bug.cgi?id=4407
 *  ("After changing the port of an http listener the mapped webapps of
 *  virtual servers get lost")
 *
 * This unit test does the following:
 * 
 * - Creates virtual server myvs, and associates it with http-listener-1
 * - Deploys webapp to myvs, and assigns this webapp as the default-web-module
 *   of myvs
 * - Modifies the port of http-listener-1
 * - Accesses the webapp deployed on myvs at the new port. The webapp is
 *   accessed both at its regular context root, as well as at the root context
 *   of myvs
 */
public class WebTest {

    private static SimpleReporterAdapter stat
        = new SimpleReporterAdapter("appserv-tests");

    private static final String TEST_NAME = "virtual-server-default-web-module--modify-http-listener-port";

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
        stat.addDescription("Unit test for GlassFish Issue 4407");
        WebTest webTest = new WebTest(args);
        webTest.doTest();
        stat.printSummary(TEST_NAME);
    }

    public void doTest() {     
        try { 
            invokeAtContextRoot();
            invokeAtRootContext();
            stat.addStatus(TEST_NAME, stat.PASS);
        } catch (Exception ex) {
            stat.addStatus(TEST_NAME, stat.FAIL);
            ex.printStackTrace();
        }
    }

    private void invokeAtContextRoot() throws Exception {
    
        Socket sock = new Socket(host, new Integer(port).intValue());
        OutputStream os = sock.getOutputStream();
        String get = "GET " + contextRoot + "/ HTTP/1.1\n";
        System.out.println(get);
        os.write(get.getBytes());
        os.write("Host: myvs\n".getBytes());
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

    private void invokeAtRootContext() throws Exception {
    
        Socket sock = new Socket(host, new Integer(port).intValue());
        OutputStream os = sock.getOutputStream();
        String get = "GET / HTTP/1.1\n";
        System.out.println(get);
        os.write(get.getBytes());
        os.write("Host: myvs\n".getBytes());
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
