import java.io.*;
import java.net.*;
import com.sun.ejte.ccl.reporter.*;

/*
 * Unit test issue 8591 for handling Error.
 *
 */
public class WebTest {

    private static SimpleReporterAdapter stat
        = new SimpleReporterAdapter("appserv-tests");

    private static final String TEST_NAME
        = "error-page-handling-for-error";

    private String host;
    private String port;
    private String contextRoot;
    private Socket sock = null;

    public WebTest(String[] args) {
        host = args[0];
        port = args[1];
        contextRoot = args[2];
    }
    
    public static void main(String[] args) {
        stat.addDescription(
            "Unit test for error-page-handling-for-error");
        WebTest webTest = new WebTest(args);
        webTest.doTest();
        stat.printSummary(TEST_NAME);
    }

    public void doTest() {     
        try { 
            invoke("TestServlet",
                    "HTTP/1.1 500 ",
                    "Error page for NoClassDefFoundError");
            stat.addStatus(TEST_NAME +"_servlet", stat.PASS);

            invoke("noClassDefFoundError.jsp",
                    "HTTP/1.1 500 ",
                    "Error page for NoClassDefFoundError");
            invoke("illegalStateException.jsp",
                    "HTTP/1.1 500 ",
                    "Error page for Exception");
            stat.addStatus(TEST_NAME + "_jsp", stat.PASS);
        } catch (Exception ex) {
            stat.addStatus(TEST_NAME, stat.FAIL);
            ex.printStackTrace();
        } finally {
            try {
                if (sock != null) {
                    sock.close();
                }
            } catch (IOException ioe) {
                // ignore
            }
        }
    }

    private void invoke(String page, String expectedStatus, String expectedResponseBody) throws Exception {
         
        sock = new Socket(host, new Integer(port).intValue());
        OutputStream os = sock.getOutputStream();
        String get = "GET " + contextRoot + "/" + page + " HTTP/1.0\n";
        System.out.println(get);
        os.write(get.getBytes());
        os.write("\n".getBytes());

        InputStream is = null;
        BufferedReader bis = null;
        boolean statusMatched = false;
        boolean responseBodyMatched = false;
        try {
            is = sock.getInputStream();
            bis = new BufferedReader(new InputStreamReader(is));
            String line = null;
            while ((line = bis.readLine()) != null) {
                System.out.println(line);
                if (line.startsWith(expectedStatus)) {
                    statusMatched = true;
                }
                if (expectedResponseBody.equals(line)) {
                    responseBodyMatched = true;
                }
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
                if (bis != null) {
                    bis.close();
                }
            } catch (IOException ioe) {
                // ignore
            }
        }

        if (!statusMatched) {
            throw new Exception("Response status does not match expected: "
                                + expectedStatus);
        }

        if (!responseBodyMatched) {
            throw new Exception("Response body does not match expected: "
                                + expectedResponseBody);
        }
    }
}
