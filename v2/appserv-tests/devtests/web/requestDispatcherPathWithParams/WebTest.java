import java.io.*;
import java.net.*;
import com.sun.ejte.ccl.reporter.*;

/*
 * Unit test for 4873423 ("getRequestURI not returning expected value").
 */
public class WebTest {

    private static SimpleReporterAdapter stat
        = new SimpleReporterAdapter("appserv-tests");
    private static final String TEST_NAME = "request-dispatcher-path-with-params";
    private static final String REQUEST_URI = "/web-" + TEST_NAME + "/jsp/forwardTo.jsp;abcd=xyz";
    private static final String QUERY_STRING = "test=john";

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
        stat.addDescription("Unit test for 4873423");
        WebTest webTest = new WebTest(args);
        webTest.doTest();
        stat.printSummary(TEST_NAME);
    }

    public void doTest() {     
        try { 
            invokeJsp();
            stat.addStatus(TEST_NAME, stat.PASS);
        } catch (Exception ex) {
            System.out.println(TEST_NAME + " test failed.");
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

    private void invokeJsp() throws Exception {
         
        sock = new Socket(host, new Integer(port).intValue());
        OutputStream os = sock.getOutputStream();
        String get = "GET " + contextRoot + "/jsp/forwardFrom.jsp HTTP/1.0\n";
        System.out.println(get);
        os.write(get.getBytes());
        os.write("\n".getBytes());

        InputStream is = null;
        BufferedReader bis = null;
        try {
            is = sock.getInputStream();
            bis = new BufferedReader(new InputStreamReader(is));
            String line = null;
            String secondToLastLine = null;
            String lastLine = null;
            int i=0;
            while ((line = bis.readLine()) != null) {
                System.out.println(i++ + ": " + line);
                secondToLastLine = lastLine;
                lastLine = line;
            }
            System.out.println("requestURI: " + secondToLastLine);
            System.out.println("queryString: " + lastLine);
            if (!REQUEST_URI.equals(secondToLastLine)) {
                throw new Exception("Wrong requestURI. Received: " +
                    secondToLastLine  + ", Expected: " + REQUEST_URI);
            }
            if (!QUERY_STRING.equals(lastLine)) {
                throw new Exception("Wrong query string. Received: " +
                    lastLine  + ", Expected: " + QUERY_STRING);
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
    }
}
