import java.io.*;
import java.net.*;
import com.sun.ejte.ccl.reporter.*;

/*
 * Unit test for Bugtraq 4994881 ("Parser for JSP pages in XML syntax ignores
 * custom action's body type").
 *
 * Force parse error (and subsequent 500 response code) because body of
 * <my:jspBodyContent> contains scriptlet, violating the scriptless body
 * declaration of the encapsulating <tag:scriptless>
 */
public class WebTest {

    private static final String TEST_NAME
        = "jsp-scriptless-tag-body-jsp-document";

    private static SimpleReporterAdapter stat
        = new SimpleReporterAdapter("appserv-tests");

    private String host;
    private String port;
    private String contextRoot;

    public WebTest(String[] args) {
        host = args[0];
        port = args[1];
        contextRoot = args[2];
    }
    
    public static void main(String[] args) {
        WebTest webTest = new WebTest(args);
        webTest.doTest();
        stat.printSummary();
    }

    public void doTest() {
     
        try {

            URL url = new URL("http://" + host  + ":" + port
                       + contextRoot + "/jsp/test.jspx");
            System.out.println("Connecting to: " + url.toString());
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.connect();
            int code = conn.getResponseCode();
            if (code == 500) {
                stat.addStatus(TEST_NAME, stat.PASS);
            } else {
                System.err.println("Unexpected return code: " + code);
                stat.addStatus(TEST_NAME, stat.FAIL);
            }
        } catch (Exception ex) {
            stat.addStatus(TEST_NAME, stat.FAIL);
            ex.printStackTrace();
        }
    }

}
