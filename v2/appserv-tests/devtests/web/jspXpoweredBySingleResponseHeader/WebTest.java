import java.io.*;
import java.net.*;
import com.sun.ejte.ccl.reporter.*;

/*
 * Unit test for https://glassfish.dev.java.net/issues/show_bug.cgi?id=1093
 * ("x-powered-by header has redundant information"):
 *
 * Make sure that if one JSP forwards to another, the response does not 
 * contain any duplicate 'X-Powered-By: JSP' header.
 */
public class WebTest {

    private static SimpleReporterAdapter stat
        = new SimpleReporterAdapter("appserv-tests");
    private static final String TEST_NAME = "jsp-xpowered-by-single-response-header";

    private String host;
    private String port;
    private String contextRoot;

    public WebTest(String[] args) {
        host = args[0];
        port = args[1];
        contextRoot = args[2];
    }
    
    public static void main(String[] args) {
        stat.addDescription("Unit test for GlassFish issue 1093");

        try {
            new WebTest(args).doTest();
            stat.addStatus(TEST_NAME, stat.PASS);
        } catch (Exception ex) {
            System.out.println(TEST_NAME + " test failed");
            stat.addStatus(TEST_NAME, stat.FAIL);
            ex.printStackTrace();
        }

        stat.printSummary(TEST_NAME);
    }

    private void doTest() throws Exception {
         
        Socket sock = new Socket(host, new Integer(port).intValue());
        OutputStream os = sock.getOutputStream();
        String get = "GET " + contextRoot + "/from.jsp" + " HTTP/1.0\n";
        System.out.println(get);
        os.write(get.getBytes());
        os.write("\n".getBytes());
        
        InputStream is = sock.getInputStream();
        BufferedReader bis = new BufferedReader(new InputStreamReader(is));

        String line = null;
        int count = 0;
        while ((line = bis.readLine()) != null) {
            if (line.startsWith("X-Powered-By: JSP")) {
                count++;
            }
        }

        if (count != 1) {
            throw new Exception("Expected one 'X-Powered-By: JSP' response " +
                                "header, got: " + count);
        }
    }

}
