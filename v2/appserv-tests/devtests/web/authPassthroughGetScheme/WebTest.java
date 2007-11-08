import java.io.*;
import java.net.*;
import com.sun.ejte.ccl.reporter.*;

/*
 * Unit test for 6170450 ("request.getScheme() does not return the correct
 * protocol when using passthrough plugin").
 *
 * This test sets the HTTP listener's authPassthroughEnabled property to TRUE
 * and includes a 'Proxy-keysize' header in the request. The test therefore
 * expects that ServletRequest.getScheme() return HTTPS instead of HTTP.
 */
public class WebTest {

    private static SimpleReporterAdapter stat
        = new SimpleReporterAdapter("appserv-tests");
    private static final String TEST_NAME = "auth-passthrough-get-scheme";

    private String host;
    private String port;
    private String contextRoot;

    public WebTest(String[] args) {
        host = args[0];
        port = args[1];
        contextRoot = args[2];
    }
    
    public static void main(String[] args) {
        stat.addDescription("Unit test for 6170450");
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
    }

    private void invokeJsp() throws Exception {
         
        Socket sock = new Socket(host, new Integer(port).intValue());
        OutputStream os = sock.getOutputStream();
        String get = "GET " + contextRoot + "/jsp/test.jsp" + " HTTP/1.0\n";
        System.out.println(get);
        os.write(get.getBytes());
        os.write("Proxy-keysize: 512\n".getBytes());
        os.write("Proxy-ip: 123.456.789\n".getBytes());
        os.write("\n".getBytes());
        
        InputStream is = sock.getInputStream();
        BufferedReader bis = new BufferedReader(new InputStreamReader(is));

        String line = null;
        String lastLine = null;
        while ((line = bis.readLine()) != null) {
            System.out.println(line);
            lastLine = line;
        }

        if ("Scheme=https".equals(lastLine)) {
            stat.addStatus(TEST_NAME, stat.PASS);
        } else {
            System.err.println("Unexpected response. Expected: Scheme=https, "
                               + "received: " + lastLine);
            stat.addStatus(TEST_NAME, stat.FAIL);
        }
    }
}
