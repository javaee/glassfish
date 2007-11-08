import java.io.*;
import java.net.*;
import com.sun.ejte.ccl.reporter.*;

/*
 * Unit test for 4655010 ("Method sendError() of class HttpServletResponse
 * does not send multi byte data").
 */
public class WebTest {

    private static SimpleReporterAdapter stat
        = new SimpleReporterAdapter("appserv-tests");
    private static final String TEST_NAME = "responseErrorMessageEncoding";

    private String host;
    private String port;
    private String contextRoot;

    public WebTest(String[] args) {
        host = args[0];
        port = args[1];
        contextRoot = args[2];
    }
    
    public static void main(String[] args) {
        stat.addDescription("Unit test for 4655010");
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
        String get = "GET " + contextRoot + "/HelloJapan" + " HTTP/1.0\n";
        System.out.println(get);
        os.write(get.getBytes());
        os.write("\n".getBytes());
        
        InputStream is = sock.getInputStream();
        // Decode error message using the same charset that it was encoded in
        BufferedReader bis = new BufferedReader(
                    new InputStreamReader(is, "Shift_JIS"));
        String line = null;
        String bodyLine = null;

        while ((line = bis.readLine()) != null) {
            bodyLine = line;
        }

        if (bodyLine != null) {
            System.out.println("Response body: " + bodyLine);
            int beginIndex = bodyLine.indexOf("BEGIN_JAPANESE");
            int endIndex = bodyLine.indexOf("END_JAPANESE");
            if (endIndex != -1) {
                String helloWorld = bodyLine.substring(beginIndex
                                                       + "BEGIN_JAPANESE".length(),
                                                       endIndex);
                String helloWorldOrig = "\u4eca\u65e5\u306f\u4e16\u754c";
                if (helloWorld.equals(helloWorldOrig)) {
                    stat.addStatus(TEST_NAME, stat.PASS);
                } else {
                    System.err.println("Error message decoding problem");
                    stat.addStatus(TEST_NAME, stat.FAIL);
                }
            } else {
                System.err.println("Wrong error message");
                stat.addStatus(TEST_NAME, stat.FAIL);
            }
        } else {
            System.err.println("Wrong error message. No response body");
            stat.addStatus(TEST_NAME, stat.FAIL);
        }
    }
}
