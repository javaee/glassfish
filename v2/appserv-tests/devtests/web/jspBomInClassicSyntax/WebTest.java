import java.io.*;
import java.net.*;
import com.sun.ejte.ccl.reporter.*;

/*
 * Unit test for BOM in JSP classic (standard) syntax.
 *
 * Each of the JSP pages accessed by this test is preceded by a BOM from
 * which the JSP container derives the page encoding:
 *
 *  Page           Page Encoding          Bytes         
 *  UTF-16BE.jsp   UTF-16, big-endian     FE FF         
 *  UTF-16LE.jsp   UTF-16, little-endian  FF FE
 *  UTF-8.jsp      UTF-8                  EF BB BF
 *
 * This test enforces that the BOM does not appear in the generated page
 * output, and that the charset component of the Content-Type response header
 * matches the charset identified by the BOM.
 */
public class WebTest {

    private static final String TEST_NAME = "jsp-bom-in-classic-syntax";

    private static final String TEXT_HTML_UTF_16_BE
        = "text/html;charset=UTF-16BE";
    private static final String TEXT_HTML_UTF_16_LE
        = "text/html;charset=UTF-16LE";
    private static final String TEXT_HTML_UTF_8
        = "text/html;charset=UTF-8";

    private static final String EXPECTED_RESPONSE = "this is a test";

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

        stat.addDescription("Unit test for BOM in JSP classic syntax");
        WebTest webTest = new WebTest(args);

        try {
            boolean pass = webTest.doTest("UTF-16BE.jsp");
            if (!pass) {
                stat.addStatus(TEST_NAME, stat.FAIL);
            } else {
                pass = webTest.doTest("UTF-16LE.jsp");
                if (!pass) {
                    stat.addStatus(TEST_NAME, stat.FAIL);
                } else {
                    pass = webTest.doTest("UTF-8.jsp");
                    if (!pass) {
                        stat.addStatus(TEST_NAME, stat.FAIL);
                    } else {
                        stat.addStatus(TEST_NAME, stat.PASS);
                    }
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            stat.addStatus(TEST_NAME, stat.FAIL);
        }

	stat.printSummary();
    }

    /*
     * @return true if passed, false if failed
     */
    public boolean doTest(String jspPage) throws Exception {
     
        InputStream is = null;
        BufferedReader input = null;
        try {
            URL url = new URL("http://" + host  + ":" + port
                              + contextRoot + "/" + jspPage);
            System.out.println("Connecting to: " + url.toString());
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.connect();

            int responseCode = conn.getResponseCode();
            if (responseCode != 200) {
                System.err.println("Wrong response code. Expected: 200"
                                   + ", received: " + responseCode);
                return false;
            }

            is = conn.getInputStream();

            String contentType = conn.getHeaderField("Content-Type");
            if ("UTF-16BE.jsp".equals(jspPage)) {
                if (!TEXT_HTML_UTF_16_BE.equals(contentType)) {
                    System.err.println("Wrong response content-type. "
                                       + "Expected: " + TEXT_HTML_UTF_16_BE
                                       + ", received: " + contentType);
                    return false;
                }
            } else if ("UTF-16LE.jsp".equals(jspPage)) {
                if (!TEXT_HTML_UTF_16_LE.equals(contentType)) {
                    System.err.println("Wrong response content-type. "
                                       + "Expected: " + TEXT_HTML_UTF_16_LE
                                       + ", received: " + contentType);
                    return false;
                }
            } else if ("UTF-8.jsp".equals(jspPage)) {
                if (!TEXT_HTML_UTF_8.equals(contentType)) {
                    System.err.println("Wrong response content-type. "
                                       + "Expected: " + TEXT_HTML_UTF_8
                                       + ", received: " + contentType);
                    return false;
                }
            } else {
                return false;
            }

            String charSet = getCharSet(contentType);
            if (charSet == null) {
                return false;
            }

            input = new BufferedReader(
                    new InputStreamReader(is, charSet));
            String line = input.readLine();
            if (!EXPECTED_RESPONSE.equals(line)) {
                System.err.println("Wrong response. "
                                   + "Expected: " + EXPECTED_RESPONSE
                                   + ", received: " + line);
                return false;
            }

            // Success
            return true;
        } finally {
            try {
                if (is != null) is.close();
            } catch (IOException ex) {}
            try {
                if (input != null) input.close();
            } catch (IOException ex) {}
        }
    }


    private String getCharSet(String contentType) {

        int index = contentType.indexOf('=');
        if (index < 0) {
            System.err.println("Unable to get charset from content-type");
            return null;
        }

        return contentType.substring(index+1);
    }
}
