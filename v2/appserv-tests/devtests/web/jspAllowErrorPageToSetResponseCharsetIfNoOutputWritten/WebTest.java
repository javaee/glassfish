import java.io.*;
import java.net.*;
import com.sun.ejte.ccl.reporter.*;

/*
 * Unit test for https://glassfish.dev.java.net/issues/show_bug.cgi?id=815
 * ("Impossible for JSP error page to set response encoding when invoked
 * from another JSP"):
 *
 * Test configures error pages for 403 response code and Throwable, see
 * web.xml.
 *
 * Test accesses 403.jsp, which sets the response code to 403 and therefore
 * causes a forward to the error403.jsp error page, which sets the response
 * content type to text/xml;charset=Shift_JIS.
 * 
 * Test then accesses throwable.jsp, which throws a Throwable and therefore
 * causes a forward to the errorThrowable.jsp error page, which sets the
 * response content type to text/xml;charset=Shift_JIS.
 *
 * Test enforces that both responses carry this content type, and reports
 * an error otherwise.
 */
public class WebTest {

    private static SimpleReporterAdapter stat
        = new SimpleReporterAdapter("appserv-tests");

    private static final String TEST_NAME
        = "jsp-allow-error-page-to-set-response-charset-if-no-output-written";

    private static final String EXPECTED_CONTENT_TYPE
        = " text/xml;charset=Shift_JIS";

    private String host;
    private String port;
    private String contextRoot;

    public WebTest(String[] args) {
        host = args[0];
        port = args[1];
        contextRoot = args[2];
    }
    
    public static void main(String[] args) {
        stat.addDescription("Unit test for GlassFish Issue 815");
        WebTest webTest = new WebTest(args);
        webTest.doTest();
        stat.printSummary(TEST_NAME);
    }

    public void doTest() {     
        try { 
            invoke("/403.jsp", "HTTP/1.1 403");
            invoke("/throwable.jsp", "HTTP/1.1 500 java.lang.Throwable");
            stat.addStatus(TEST_NAME, stat.PASS);
        } catch (Exception ex) {
            stat.addStatus(TEST_NAME, stat.FAIL);
            ex.printStackTrace();
        }
    }

    private void invoke(String uri, String expectedResponseCode)
            throws Exception {

        Socket sock = null;
        OutputStream os = null;
        InputStream is = null;
        BufferedReader bis = null;
        try {
            sock = new Socket(host, new Integer(port).intValue());
            os = sock.getOutputStream();
            String get = "GET " + contextRoot + uri + " HTTP/1.0\n";
            System.out.println(get);
            os.write(get.getBytes());
            os.write("\n".getBytes());

            is = sock.getInputStream();
            bis = new BufferedReader(new InputStreamReader(is));

            String line = null;
            boolean expectedResponseCodeSeen = false;
            while ((line = bis.readLine()) != null) {
                if (line.contains(expectedResponseCode)) {
                    expectedResponseCodeSeen = true;
                }
                if (line.toLowerCase().startsWith("content-type:")) {
                    break;
                }
            }

            if (!expectedResponseCodeSeen) {
                throw new Exception("Response does not have expected response "
                                    + "code: " + expectedResponseCode);
            }

            if ((line == null) || (line.toLowerCase().startsWith("content-type:") && !line.contains(EXPECTED_CONTENT_TYPE))) {
                throw new Exception("Wrong response content type. Expected: "
                                    + EXPECTED_CONTENT_TYPE + ", received: "
                                    + line);
            }
        } finally {
            try {
                if (os != null) os.close();
            } catch (IOException ex) {}
            try {
                if (is != null) is.close();
            } catch (IOException ex) {}
            try {
                if (sock != null) sock.close();
            } catch (IOException ex) {}
            try {
                if (bis != null) bis.close();
            } catch (IOException ex) {}
        }
    }
}
