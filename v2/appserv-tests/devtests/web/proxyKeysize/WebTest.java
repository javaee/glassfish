import java.io.*;
import java.net.*;
import com.sun.ejte.ccl.reporter.*;

/*
 * Unit test for 6170450 ("request.getScheme() does not return the correct
 * protocol when using passthrough plugin").
 *
 * This test includes a 'Proxy-keysize' header in the request and therefore
 * expects the scheme of the URL returned as the value of the 'Location'
 * response header to be HTTPS (as opposed to regular HTTP).
 */
public class WebTest {

    private static SimpleReporterAdapter stat
        = new SimpleReporterAdapter("appserv-tests");
    private static final String TEST_NAME = "web-proxy-keysize";

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
            stat.addStatus(TEST_NAME, stat.FAIL);
            ex.printStackTrace();
        }
    }

    private void invokeJsp() throws Exception {
         
        Socket sock = null;
        OutputStream os = null;
        InputStream is = null;
        BufferedReader bis = null;
        String line = null;
        try {
            sock = new Socket(host, new Integer(port).intValue());
            os = sock.getOutputStream();
            String get = "GET " + contextRoot + "/jsp/test.jsp" + " HTTP/1.0\n";
            System.out.println(get);
            os.write(get.getBytes());
            os.write("Proxy-keysize: 512\n".getBytes());
            os.write("\n".getBytes());
        
            is = sock.getInputStream();
            bis = new BufferedReader(new InputStreamReader(is));

            while ((line = bis.readLine()) != null) {
                if (line.startsWith("Location:")) {
                    break;
                }
            }
        } finally {
            try {
                if (sock != null) {
                    sock.close();
                }
            } catch(IOException ioe) {
                // ignore
            }
            try {
                if (os != null) {
                    os.close();
                }
            } catch(IOException ioe) {
                // ignore
            }
            try {
                if (is != null) {
                    is.close();
                }
            } catch(IOException ioe) {
                // ignore
            }
            try {
                if (bis != null) {
                    bis.close();
                }
            } catch(IOException ioe) {
                // ignore
            }
        }

        if (line != null) {
            System.out.println("Location header: " + line);

            String location = line.substring("Location:".length()).trim();
            if (location.startsWith("https")) {
                stat.addStatus(TEST_NAME, stat.PASS);
            } else {
                System.err.println("Wrong schema in Location response header, "
                                   + "expected: https");
                stat.addStatus(TEST_NAME, stat.FAIL);
            }
        } else {
            System.err.println("Missing Location response header");
            stat.addStatus(TEST_NAME, stat.FAIL);
        }
    }
}
