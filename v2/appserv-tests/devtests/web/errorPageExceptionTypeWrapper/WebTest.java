import java.io.*;
import java.net.*;
import com.sun.ejte.ccl.reporter.*;

/*
 * Unit test for Bugzilla 13924 ("error-page directive does not always
 * work correctly").
 *
 * According to SRV.9.9.2:
 *
 *  If no error-page declaration containing an exception-type
 *  fits using the class-hierarchy match, and the exception thrown is a
 *  ServletException or subclass thereof, the container extracts the
 *  wrapped exception, as defined by the ServletException.getRootCause
 *  method. A second pass is made over the error page declarations, again
 *  attempting to match against the error page declarations, but using the
 *  wrapped exception instead.
 *
 * We used to match only the wrapped exception against any error-page
 * decls, but never the wrapping ServletException.
 */
public class WebTest {

    private static SimpleReporterAdapter stat
        = new SimpleReporterAdapter("appserv-tests");

    private static final String TEST_NAME = "error-page-exception-type-wrapper";
    private static final String EXPECTED = "Custom error page";

    private String host;
    private String port;
    private String contextRoot;

    public WebTest(String[] args) {
        host = args[0];
        port = args[1];
        contextRoot = args[2];
    }
    
    public static void main(String[] args) {
        stat.addDescription("Unit test for Bugzilla 13924");
        WebTest webTest = new WebTest(args);
        webTest.doTest();
        stat.printSummary(TEST_NAME);
    }

    public void doTest() {     
        try { 
            Socket sock = new Socket(host, new Integer(port).intValue());
            OutputStream os = sock.getOutputStream();
            String get = "GET " + contextRoot + "/test" + " HTTP/1.0\n";
            System.out.println(get);
            os.write(get.getBytes());
            os.write("\n".getBytes());
        
            InputStream is = sock.getInputStream();
            BufferedReader bis = new BufferedReader(new InputStreamReader(is));

            String line = null;
            String lastLine = null;
            while ((line = bis.readLine()) != null) {
                lastLine = line;
            }

            if (lastLine == null) {
                stat.addStatus("Missing response", stat.FAIL);
            } else {
                if (!lastLine.equals(EXPECTED)) {
                    stat.addStatus("Wrong response: EXPECTED: " + EXPECTED
                                   + ", received: " + lastLine, stat.FAIL);
                } else {
                    stat.addStatus(TEST_NAME, stat.PASS);
                }
            }
        } catch (Exception ex) {
            System.out.println(TEST_NAME + " test failed.");
            stat.addStatus(TEST_NAME, stat.FAIL);
            ex.printStackTrace();
        }
    }

}
