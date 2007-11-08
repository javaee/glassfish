import java.io.*;
import java.net.*;
import com.sun.ejte.ccl.reporter.*;

/*
 * Unit test for https://glassfish.dev.java.net/issues/show_bug.cgi?id=1703
 * ("Ensure accept-language request header conforms to RFC 2616 and ignore it
 * if it doesn't")
 */
public class WebTest {

    private static SimpleReporterAdapter stat
        = new SimpleReporterAdapter("appserv-tests");

    private static final String TEST_NAME
        = "http-request-ignore-invalid-accept-language-header";

    private static final String EXPECTED_RESPONSE = "true";

    private String host;
    private String port;
    private String contextRoot;

    public WebTest(String[] args) {
        host = args[0];
        port = args[1];
        contextRoot = args[2];
    }
    
    public static void main(String[] args) {
        stat.addDescription("Unit test for GlassFish Issue 1703");
        WebTest webTest = new WebTest(args);
        webTest.doTest();
        stat.printSummary(TEST_NAME);
    }

    public void doTest() {
     
        try { 
            invoke();
        } catch (Exception ex) {
            System.out.println(TEST_NAME + " test failed");
            stat.addStatus(TEST_NAME, stat.FAIL);
            ex.printStackTrace();
        }
    }

    private void invoke() throws Exception {
         
        Socket sock = new Socket(host, new Integer(port).intValue());
        OutputStream os = sock.getOutputStream();
        String get = "GET " + contextRoot + "/TestServlet" + " HTTP/1.0\n";
        System.out.println(get);
        os.write(get.getBytes());
        os.write("Accept-Language: de\n".getBytes());
        // Send Accept-Language header with invalid locale code. Make sure 
        // this locale is getting ignored 
        os.write("Accept-Language: 12\n".getBytes());
        os.write("Accept-Language: FR\n".getBytes());
        os.write("\n".getBytes());
        
        InputStream is = sock.getInputStream();
        BufferedReader bis = new BufferedReader(new InputStreamReader(is));

        String line = null;
        String lastLine = null;
        while ((line = bis.readLine()) != null) {
            lastLine = line;
        }

        if (EXPECTED_RESPONSE.equals(lastLine)) {
            stat.addStatus(TEST_NAME, stat.PASS);
        } else {
            System.err.println("Unexpected response: Expected: "
                               + EXPECTED_RESPONSE
                               + ", received: " + lastLine);
            stat.addStatus(TEST_NAME, stat.FAIL);
        }
    }
}
