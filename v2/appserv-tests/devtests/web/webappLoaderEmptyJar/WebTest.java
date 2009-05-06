import java.io.*;
import java.net.*;
import com.sun.ejte.ccl.reporter.*;

/*
 * Unit test for:
 *
 * - 4670099 ("Empty jar files causes server to abort loading the
 *   other jar files in the dir") and
 * - 4649821 ("JSP:<jsp:useBean> failed to read jar file from lib
 *   directory")
 */
public class WebTest {

    private static SimpleReporterAdapter stat =
        new SimpleReporterAdapter("appserv-tests");
    private static final String TEST_NAME = "webapploader-empty-jar";

    private String host;
    private String port;
    private String contextRoot;

    public WebTest(String[] args) {
        host = args[0];
        port = args[1];
        contextRoot = args[2];
    }
    
    public static void main(String[] args) {
        stat.addDescription("Unit test for Bugtraq 4670099 and 4649821");
        WebTest webTest = new WebTest(args);
        webTest.doTest();
        stat.printSummary(TEST_NAME);
    }

    public void doTest() {
     
        try { 
            invokeJSP();
            stat.addStatus(TEST_NAME, stat.PASS);
        } catch (Exception ex) {
            System.out.println(TEST_NAME + " test failed");
            stat.addStatus(TEST_NAME, stat.FAIL);
            ex.printStackTrace();
        }
    }

    private void invokeJSP() throws Exception {

        String url = "http://" + host + ":" + port + "/" + contextRoot +
            "/jsp/stringBean.jsp";
        System.out.println("Invoking " + url);
        HttpURLConnection conn = (HttpURLConnection)
            (new URL(url)).openConnection();
        int code = conn.getResponseCode();
        if (code != 200) {
            throw new Exception("Unexpected response code: " + code);
        }         
    }

}
