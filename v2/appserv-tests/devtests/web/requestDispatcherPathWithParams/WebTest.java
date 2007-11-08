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
        } catch (Exception ex) {
            System.out.println(TEST_NAME + " test failed.");
            stat.addStatus(TEST_NAME, stat.FAIL);
            ex.printStackTrace();
        }
    }

    private void invokeJsp() throws Exception {
         
        Socket sock = new Socket(host, new Integer(port).intValue());
        OutputStream os = sock.getOutputStream();
        String get = "GET " + contextRoot + "/jsp/forwardFrom.jsp HTTP/1.0\n";
        System.out.println(get);
        os.write(get.getBytes());
        os.write("\n".getBytes());
        
        InputStream is = sock.getInputStream();
        BufferedReader bis = new BufferedReader(new InputStreamReader(is));
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
            System.out.println("Wrong requestURI. Received: " + secondToLastLine 
                           + ", Expected: " + REQUEST_URI);
            stat.addStatus(TEST_NAME, stat.FAIL);
        } else if (!QUERY_STRING.equals(lastLine)) {
            System.out.println("Wrong query string. Received: " + lastLine 
                           + ", Expected: " + QUERY_STRING);
            stat.addStatus(TEST_NAME, stat.FAIL);
        } else {
            stat.addStatus(TEST_NAME, stat.PASS);
        }
    }
}
