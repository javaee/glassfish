import java.io.*;
import java.net.*;
import com.sun.ejte.ccl.reporter.*;

/*
 * Unit test for Bugzilla 16181 ("JspWriter not restored properly when
 * exception thrown in a tag's body content"), see
 * http://issues.apache.org/bugzilla/show_bug.cgi?id=16181
 */
public class WebTest {

    private static SimpleReporterAdapter stat
        = new SimpleReporterAdapter("appserv-tests");

    private static final String TEST_NAME = "jsp-exception-in-tag-body-restore-jsp-writer";
    private static final String EXPECTED = "java.lang.Exception: exception thrown by throwEx";

    private String host;
    private String port;
    private String contextRoot;

    public WebTest(String[] args) {
        host = args[0];
        port = args[1];
        contextRoot = args[2];
    }
    
    public static void main(String[] args) {
        stat.addDescription("Unit test for Bugzilla 16181");
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

        BufferedReader bis = null;
        try {
            String url = "http://" + host + ":" + port + "/" + contextRoot
                + "/jsp/test.jsp";
            HttpURLConnection conn = (HttpURLConnection)
                (new URL(url)).openConnection();

            int code = conn.getResponseCode();
            if (code != 200) {
                System.err.println("Unexpected return code: " + code);
                stat.addStatus(TEST_NAME, stat.FAIL);
                return;
            }

            bis = new BufferedReader(
                        new InputStreamReader(conn.getInputStream()));
            String line = null;
            while ((line = bis.readLine()) != null) {
                if (EXPECTED.equals(line)) {
                    stat.addStatus(TEST_NAME, stat.PASS);
                    break;
                }
            }

            if (line == null) {
                stat.addStatus(TEST_NAME, stat.FAIL);
            }
        } finally {
            try {
                if (bis != null) bis.close();
            } catch (IOException ex) {}
        }
    }
}
