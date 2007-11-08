import java.io.*;
import java.net.*;
import com.sun.ejte.ccl.reporter.*;

/*
 * Unit test for Bugtraq 4968841 ("ArrayIndexOutOfBoundsException when calling
 * doOptions()")
 */
public class WebTest {

    private static SimpleReporterAdapter stat
        = new SimpleReporterAdapter("appserv-tests");

    private static final String TEST_NAME
        = "http-servlet-get-all-declared-methods";

    private String host;
    private String port;
    private String contextRoot;

    public WebTest(String[] args) {
        host = args[0];
        port = args[1];
        contextRoot = args[2];
    }
    
    public static void main(String[] args) {
        stat.addDescription("Unit test for 4968841");
        WebTest webTest = new WebTest(args);
        webTest.doTest();
        stat.printSummary(TEST_NAME);
    }

    public void doTest() {     
        try { 
            invokeServlet();
        } catch (Exception ex) {
            System.out.println(TEST_NAME + " test failed.");
            stat.addStatus(TEST_NAME, stat.FAIL);
            ex.printStackTrace();
        }
    }

    private void invokeServlet() throws Exception {
         
        Socket sock = new Socket(host, new Integer(port).intValue());
        OutputStream os = sock.getOutputStream();
        System.out.println(("OPTIONS " + contextRoot + "/TestServlet"
            + " HTTP/1.0\n"));
        os.write(("OPTIONS " + contextRoot + "/TestServlet"
            + " HTTP/1.0\n").getBytes());
        os.write("\n".getBytes());
        
        InputStream is = sock.getInputStream();
        BufferedReader bis = new BufferedReader(new InputStreamReader(is));

        String line = null;
        while ((line = bis.readLine()) != null) {
        //WS70 returns "Allow: HEAD, GET, TRACE" in this case, hence adding ||
        if ("Allow: GET, HEAD, TRACE, OPTIONS".equals(line) ||
            "Allow: HEAD, GET, TRACE".equals(line)) {
                stat.addStatus(TEST_NAME, stat.PASS);
                return;
            }
        }

        stat.addStatus(TEST_NAME, stat.FAIL);
    }
}
