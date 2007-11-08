import java.io.*;
import java.net.*;
import com.sun.ejte.ccl.reporter.*;

/*
 * Make sure the response content type contains the default charset
 * (ISO-8859-1) if a JSP includes a static resource, and does not itself
 * add any output.
 */
public class WebTest {

    private static SimpleReporterAdapter stat
        = new SimpleReporterAdapter("appserv-tests");
    private static final String TEST_NAME = "jspStaticFileEncoding";

    private String host;
    private String port;
    private String contextRoot;

    public WebTest(String[] args) {
        host = args[0];
        port = args[1];
        contextRoot = args[2];
    }
    
    public static void main(String[] args) {
        stat.addDescription("jspStaticFileEncoding");
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

        URL url = new URL("http://" + host  + ":" + port + contextRoot
                          + "/include.jsp");
        System.out.println("Invoking URL: " + url.toString());

        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        int responseCode = conn.getResponseCode();
        if (responseCode != 200) {
            System.err.println("Unexpected return code: " + responseCode);
            stat.addStatus(TEST_NAME, stat.FAIL);
            return;
        }

        String contentType = conn.getContentType();
        System.out.println("Response Content-Type: " + contentType);
        if (contentType != null) {
            int index = contentType.indexOf("ISO-8859-1");
            if (index != -1) {
                stat.addStatus(TEST_NAME, stat.PASS);
            } else {
                System.err.println("Missing ISO-8859-1 charset in response "
                                   + "content type");
                stat.addStatus(TEST_NAME, stat.FAIL);
            }
        } else {
            System.err.println("Missing response content type header");
            stat.addStatus("TEST_NAME", stat.FAIL);
        }
    }
}
