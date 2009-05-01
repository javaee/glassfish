import java.io.*;
import java.net.*;
import com.sun.ejte.ccl.reporter.*;

/*
 * Unit test for Bugzilla 33223 ("pageContext.forward and <jsp:include>
 * result in StringIndexOutOfBoundsException"),
 * see http://issues.apache.org/bugzilla/show_bug.cgi?id=33223.
 *
 * This test case used to throw a java.lang.StringIndexOutOfBoundsException
 * when reconstructing the URI of a JSP that is the target of a 
 * RequestDispatcher.include() from the javax.servlet.include.XXX
 * request attributes.
 */
public class WebTest {

    private static SimpleReporterAdapter stat
        = new SimpleReporterAdapter("appserv-tests");

    private static final String TEST_NAME = "jsp-included-welcome-page";
    private static final String EXPECTED_RESPONSE = "INCLUDED WELCOME PAGE";

    private String host;
    private String port;
    private String contextRoot;

    public WebTest(String[] args) {
        host = args[0];
        port = args[1];
        contextRoot = args[2];
    }
    
    public static void main(String[] args) {
        stat.addDescription("Unit test for Bugzilla 33223");
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

    public void invokeJsp() throws Exception {

        InputStream is = null;
        BufferedReader input = null;
        try {
            String url = "http://" + host + ":" + port + contextRoot
                         + "/jsp/including/including.jsp";
            HttpURLConnection conn = (HttpURLConnection)
                (new URL(url)).openConnection();

            int code = conn.getResponseCode();
            if (code != 200) {
                System.err.println("Unexpected return code: " + code);
                stat.addStatus(TEST_NAME, stat.FAIL);
            } else {
                is = conn.getInputStream();
                input = new BufferedReader(new InputStreamReader(is));
                String line = input.readLine();
                if (!EXPECTED_RESPONSE.equals(line)) {
                    System.err.println("Wrong response. "
                                       + "Expected: " + EXPECTED_RESPONSE
                                       + ", received: " + line);
                    stat.addStatus(TEST_NAME, stat.FAIL);
                } else {
                    stat.addStatus(TEST_NAME, stat.PASS);
                }
            }
        } finally {
            try {
                if (is != null) is.close();
            } catch (IOException ex) {}
            try {
                if (input != null) input.close();
            } catch (IOException ex) {}
        }
    }
}
