import java.io.*;
import java.net.*;
import com.sun.ejte.ccl.reporter.*;

/*
 * Unit test for https://glassfish.dev.java.net/issues/show_bug.cgi?id=871
 * ("[JSTL] Remove dependency of javax.servlet.jsp.jstl.fmt.LocaleSupport on
 * RI classes"):
 *
 * Make sure this API still functions after removing its dependencies on the
 * RI.
 */
public class WebTest {

    private static final String TEST_NAME = "jstl-locale-support-api";

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

        stat.addDescription("Unit test for GlassFish 871");
        WebTest webTest = new WebTest(args);
        try {
            webTest.doTest();
        } catch (Exception ex) {
            stat.addStatus(TEST_NAME, stat.FAIL);
            ex.printStackTrace();
        }
	stat.printSummary();
    }

    public void doTest() throws Exception {
     
        // Access test1.jsp
        Socket sock = new Socket(host, new Integer(port).intValue());
        OutputStream os = sock.getOutputStream();
        String get = "GET " + contextRoot + "/test1.jsp" + " HTTP/1.0\n";
        System.out.println(get);
        os.write(get.getBytes());
        os.write("\n".getBytes());
        
        InputStream is = sock.getInputStream();
        BufferedReader br = new BufferedReader(new InputStreamReader(is));

        String line = null;
        while ((line = br.readLine()) != null) {
            System.out.println(line);
            if ("Guten Morgen".equals(line)) {
                break;
            }
        }

        if (line == null) {
            throw new Exception("Missing Guten Morgen in response");
        }

        // Access test2.jsp
        sock = new Socket(host, new Integer(port).intValue());
        os = sock.getOutputStream();
        get = "GET " + contextRoot + "/test2.jsp" + " HTTP/1.0\n";
        System.out.println(get);
        os.write(get.getBytes());
        String acceptLanguage = "Accept-Language: de\n";
        os.write(acceptLanguage.getBytes());
        os.write("\n".getBytes());
        
        is = sock.getInputStream();
        br = new BufferedReader(new InputStreamReader(is));

        while ((line = br.readLine()) != null) {
            System.out.println(line);
            if ("Guten Abend".equals(line)) {
                break;
            }
        }

        if (line == null) {
            throw new Exception("Missing Guten Abend in response");
        }

        stat.addStatus(TEST_NAME, stat.PASS);
    }

}
