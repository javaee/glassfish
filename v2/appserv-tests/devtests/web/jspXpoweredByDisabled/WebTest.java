import java.io.*;
import java.net.*;
import com.sun.ejte.ccl.reporter.*;

/*
 * Makes sure x-powered-by response header may be disabled for JSPs by setting
 * corresponding jsp-config property in sun-web.xml to FALSE.
 */
public class WebTest {

    private static SimpleReporterAdapter stat
        = new SimpleReporterAdapter("appserv-tests");
    private static final String TEST_NAME = "jsp-xpowered-by-disabled";

    private String host;
    private String port;
    private String contextRoot;

    public WebTest(String[] args) {
        host = args[0];
        port = args[1];
        contextRoot = args[2];
    }
    
    public static void main(String[] args) {
        stat.addDescription("Unit test for disabling 'X-Powered-By: JSP/2.0' response header");
        WebTest webTest = new WebTest(args);
        webTest.doTest();
        stat.printSummary(TEST_NAME);
    }

    public void doTest() {
     
        try { 
            invokeJsp();
        } catch (Exception ex) {
            System.out.println(TEST_NAME + " test failed");
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
            String get = "GET " + contextRoot + "/jsp/test.jsp" + " HTTP/1.0\n";
            System.out.println(get);
            os.write(get.getBytes());
            os.write("\n".getBytes());

            is = sock.getInputStream();
            bis = new BufferedReader(new InputStreamReader(is));

            String line = null;
            while ((line = bis.readLine()) != null) {
                if (line.startsWith("X-Powered-By: JSP")) {
                    break;
                }
            }

            if (line == null) {
                stat.addStatus(TEST_NAME, stat.PASS);
            } else {
                System.err.println("Unexpected response header: " + line);
                stat.addStatus(TEST_NAME, stat.FAIL);
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
