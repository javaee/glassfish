import java.io.*;
import java.net.*;
import com.sun.ejte.ccl.reporter.*;

/*
 * Unit test for Bugzilla 31171 ("ClassCastException in
 * org.apache.jasper.runtime.PageContextImpl.getException"), see
 * http://issues.apache.org/bugzilla/show_bug.cgi?id=31171
 */
public class WebTest {

    private static final String TEST_NAME
        = "jsp-error-page-class-cast-exception";

    private static final String EXPECTED
        = "javax.servlet.jsp.JspException: java.lang.Throwable: "
            + "The cake fell in the mud";

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
        stat.addDescription("Unit test for Bugzilla 31171");
        WebTest webTest = new WebTest(args);
        webTest.doTest();
	stat.printSummary();
    }

    public void doTest() {

        InputStream is = null;
        BufferedReader bis = null;
        try {
            Socket s = new Socket(host, new Integer(port).intValue());
            OutputStream os = s.getOutputStream();
            String requestUri = contextRoot + "/causeError.jsp";

            System.out.println("GET " + requestUri + " HTTP/1.0");
            os.write(("GET " + requestUri + " HTTP/1.0\n").getBytes());
            os.write("\n".getBytes());
        
            is = s.getInputStream();
            bis = new BufferedReader(new InputStreamReader(is));
            String line = null;
            while ((line = bis.readLine()) != null) {
                System.out.println(line);
                if (line.equals(EXPECTED)) {
                    break;
                }
            }
   
            if (line != null) {
                stat.addStatus(TEST_NAME, stat.PASS);
            } else {
                stat.addStatus(TEST_NAME, stat.FAIL);
            }

        } catch( Exception ex) {
            ex.printStackTrace();
            stat.addStatus(TEST_NAME, stat.FAIL);
        } finally {
            try {
                if (is != null) is.close();
            } catch (IOException ex) {}
            try {
                if (bis != null) bis.close();
            } catch (IOException ex) {}
        }
    }

}
