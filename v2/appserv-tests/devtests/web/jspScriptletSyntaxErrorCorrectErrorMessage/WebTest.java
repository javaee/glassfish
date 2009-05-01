import java.io.*;
import java.net.*;
import com.sun.ejte.ccl.reporter.*;

/*
 * Unit test for Bugtraq 5052205 ("Wrong error message for JSP pages whose
 * java code has syntax errors")
 */
public class WebTest {

    private static SimpleReporterAdapter stat
        = new SimpleReporterAdapter("appserv-tests");
    private static final String TEST_NAME
        = "scriptlet-syntax-error-correct-error-message";
    private static final String EXPECTED_ERROR = "[javac] 3 errors";
    private static final String EXPECTED_ERROR_JDK6 = "PWC6033: Error in Javac compilation for JSP";

    private String host;
    private String port;
    private String contextRoot;

    public WebTest(String[] args) {
        host = args[0];
        port = args[1];
        contextRoot = args[2];
    }
    
    public static void main(String[] args) {
        stat.addDescription("Unit test for Bugtraq 5052205");
        WebTest webTest = new WebTest(args);
        webTest.doTest();
        stat.printSummary(TEST_NAME);
    }

    public void doTest() {
     
        try { 
            invokeJsp();
            stat.addStatus(TEST_NAME, stat.PASS);
        } catch (Exception ex) {
            stat.addStatus(TEST_NAME, stat.FAIL);
            ex.printStackTrace();
        }

        return;
    }

    private void invokeJsp() throws Exception {

        Socket sock = null;
        OutputStream os = null;
        InputStream is = null;
        BufferedReader bis = null;
        try {
            sock = new Socket(host, new Integer(port).intValue());
            os = sock.getOutputStream();
            String get = "GET " + contextRoot + "/jsp/test.jsp HTTP/1.0\n";
            System.out.println(get);
            os.write(get.getBytes());
            os.write("\n".getBytes());

            is = sock.getInputStream();
            bis = new BufferedReader(new InputStreamReader(is));

            boolean found = false;
            String line = null;
            while ((line = bis.readLine()) != null) {
                if (line.endsWith(EXPECTED_ERROR) ||
                    line.endsWith(EXPECTED_ERROR_JDK6)) {
                    found = true;
                    break;
                }
            }

            if (!found) {
                throw new Exception("Wrong response, expected: \n" +
                                 "For JDK 5: " + EXPECTED_ERROR + '\n' +
                                 "For JDK 6: " + EXPECTED_ERROR_JDK6);
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
