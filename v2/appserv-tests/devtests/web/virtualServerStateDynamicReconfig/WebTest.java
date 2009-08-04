import java.io.*;
import java.net.*;
import com.sun.ejte.ccl.reporter.*;

/**
 * Unit test to ensure that the state of a virtual server may be
 * changed without requiring a server restart.
 *
 * This test:
 *
 * 1. Creates a virtual-server myvs and deploys a webapp to it
 *
 * 2. Makes sure that the webapp may be accessed
 *
 * 3. Disables the virtual server and ensures that a 403 response is
 *    returned when the webapp is accessed
 *
 * 4. Turns off the virtual server and ensures that a 404 response is
 *    returned when the webapp is accessed
 *
 * 5. Re-enables the virtual server and makes sure that the webapp may be
 *    accessed again.
 */
public class WebTest {

    private static SimpleReporterAdapter stat
        = new SimpleReporterAdapter("appserv-tests");

    private static String TEST_NAME = null;

    private static final String TEST_ROOT_NAME
        = "virtual-server-state-dynamic-reconfig";

    private static final String ON_RESPONSE = "Success!";

    private static final String DISABLED_RESPONSE
        = "HTTP/1.1 403 Virtual server myvs has been disabled";

    private static final String OFF_RESPONSE
        = "HTTP/1.1 404 Virtual server myvs has been turned off";

    private String host;
    private String port;
    private String contextRoot;
    private String run;

    public WebTest(String[] args) {
        host = args[0];
        port = args[1];
        contextRoot = args[2];
        run = args[3];
    }
    
    public static void main(String[] args) {
        stat.addDescription("Unit test for disabling a virtual server");
        WebTest webTest = new WebTest(args);

        try { 
            if ("on".equals(webTest.run)) {
                TEST_NAME = TEST_ROOT_NAME + "-on";
                webTest.onRun();
            } else if ("disabled".equals(webTest.run)) {
                TEST_NAME = TEST_ROOT_NAME + "-disabled";
                webTest.disabledRun();
            } else if ("off".equals(webTest.run)) {
                TEST_NAME = TEST_ROOT_NAME + "-off";
                webTest.offRun();
            }
            stat.addStatus(TEST_NAME, stat.PASS);
        } catch (Exception ex) {
            stat.addStatus(TEST_NAME, stat.FAIL);
            ex.printStackTrace();
        }

        stat.printSummary(TEST_NAME);
    }

    /**
     * Make sure the webapp may be accessed on the newly created virtual
     * server.
     */
    private void onRun() throws Exception {
        
        Socket sock = new Socket(host, new Integer(port).intValue());
        OutputStream os = sock.getOutputStream();
        String get = "GET " + contextRoot + "/test.txt HTTP/1.1\n";
        System.out.println(get);
        os.write(get.getBytes());
        os.write("Host: myvs\n".getBytes());
        os.write("Connnection: Close\n".getBytes());
        os.write("\n".getBytes());

        InputStream is = sock.getInputStream();
        BufferedReader bis = new BufferedReader(new InputStreamReader(is));

        String line = null;
        while ((line = bis.readLine()) != null) {
            System.out.println(line);
            if (line.equals(ON_RESPONSE)) {
                break;
            }
        }

        if (line == null) {
            throw new Exception("Missing expected response: " +
                                ON_RESPONSE);
        }
    }

    /**
     * Make sure that a 403 response is returned when trying to access the
     * webapp after the virtual server on which it is deployed has been
     * disabled.
     */
    private void disabledRun() throws Exception {

        Socket sock = new Socket(host, new Integer(port).intValue());
        OutputStream os = sock.getOutputStream();
        String get = "GET " + contextRoot + "/test.txt HTTP/1.1\n";
        System.out.println(get);
        os.write(get.getBytes());
        os.write("Host: myvs\n".getBytes());
        os.write("Connnection: Close\n".getBytes());
        os.write("\n".getBytes());

        InputStream is = sock.getInputStream();
        BufferedReader bis = new BufferedReader(new InputStreamReader(is));

        String line = null;
        while ((line = bis.readLine()) != null) {
            System.out.println(line);
            if (line.contains(DISABLED_RESPONSE)) {
                break;
            }
        }

        if (line == null) {
            throw new Exception("Missing expected response: " +
                                DISABLED_RESPONSE);
        }
    }

    /**
     * Make sure that a 404 response is returned when trying to access the
     * webapp after the virtual server on which it is deployed has been
     * turned off.
     */
    private void offRun() throws Exception {

        Socket sock = new Socket(host, new Integer(port).intValue());
        OutputStream os = sock.getOutputStream();
        String get = "GET " + contextRoot + "/test.txt HTTP/1.1\n";
        System.out.println(get);
        os.write(get.getBytes());
        os.write("Host: myvs\n".getBytes());
        os.write("Connnection: Close\n".getBytes());
        os.write("\n".getBytes());

        InputStream is = sock.getInputStream();
        BufferedReader bis = new BufferedReader(new InputStreamReader(is));

        String line = null;
        while ((line = bis.readLine()) != null) {
            System.out.println(line);
            if (line.contains(OFF_RESPONSE)) {
                break;
            }
        }

        if (line == null) {
            throw new Exception("Missing expected response: " +
                                OFF_RESPONSE);
        }
    }

}
