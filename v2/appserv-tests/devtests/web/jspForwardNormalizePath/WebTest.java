import java.io.*;
import java.net.*;
import com.sun.ejte.ccl.reporter.*;

/**
 * Unit test for:
 *
 *  ("Tomcat forward and getRequestDispatcher security issue")
 *
 */
public class WebTest {

    private static SimpleReporterAdapter stat
        = new SimpleReporterAdapter("appserv-tests");

    private static final String TEST_NAME
        = "jsp-forward-normalize-path";

    private static final String EXPECTED = "This is OK.";

    private String host;
    private String port;
    private String contextRoot;

    public WebTest(String[] args) {
        host = args[0];
        port = args[1];
        contextRoot = args[2];
    }
    
    public static void main(String[] args) {
        stat.addDescription("Unit test for Tomcat forward and getRequestDispatcher security issue");
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

        System.out.println("Host=" + host + ", port=" + port);        
        Socket sock = null;
        OutputStream os = null;
        InputStream is = null;
        BufferedReader bis = null;
        boolean isExpected = false;

        try {
            sock = new Socket(host, new Integer(port).intValue());
            os = sock.getOutputStream();
            String get = "GET " + contextRoot + "/page.jsp?blah=/../WEB-INF/web.xml HTTP/1.1\n";
            System.out.println(get);
            os.write(get.getBytes());
            os.write("Host: localhost\n".getBytes());
            os.write("\n".getBytes());

            is = sock.getInputStream();
            bis = new BufferedReader(new InputStreamReader(is));

            String line = null;
            while ((line = bis.readLine()) != null) {
                System.out.println(line);
                if (line.equals(EXPECTED)) {
                    isExpected = true;
                    break;
                }
            }
        } finally {
            try {
                if (sock != null) {
                    sock.close();
                }
            } catch(IOException ioe) {
                // ignore
            }
            try {
                if (os != null) {
                    os.close();
                }
            } catch(IOException ioe) {
                // ignore
            }
            try {
                if (is != null) {
                    is.close();
                }
            } catch(IOException ioe) {
                // ignore
            }
            try {
                if (bis != null) {
                    bis.close();
                }
            } catch(IOException ioe) {
                // ignore
            }
        }

        if (isExpected) {
            stat.addStatus(TEST_NAME, stat.PASS);
        } else {
            System.err.println("Missing expected response: " + EXPECTED);
        }
    }
}
