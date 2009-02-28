import java.io.*;
import java.net.*;
import com.sun.ejte.ccl.reporter.*;

/*
 * Unit test for customizing all session tracking cookie properties
 * programmatically (from a ServletContextListener).
 */
public class WebTest {

    private static String TEST_NAME = "session-cookie-properties-programmatic";

    private static final String MYJSESSIONID = "MYJSESSIONID";

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

        stat.addDescription("Unit test for customizing all session " +
                            "tracking cookie properties");
        WebTest webTest = new WebTest(args);

        try {
            webTest.run();
            stat.addStatus(TEST_NAME, stat.PASS);
        } catch( Exception ex) {
            ex.printStackTrace();
            stat.addStatus(TEST_NAME, stat.FAIL);
        }

	stat.printSummary();
    }

    public void run() throws Exception {

        Socket sock = new Socket(host, new Integer(port).intValue());
        OutputStream os = sock.getOutputStream();
        String get = "GET " + contextRoot + "/CreateSession HTTP/1.0\n";
        System.out.println(get);
        os.write(get.getBytes());
        os.write("\r\n".getBytes());
        
        InputStream is = sock.getInputStream();
        BufferedReader br = new BufferedReader(new InputStreamReader(is));
        String sessionCookie = null;
        while ((sessionCookie = br.readLine()) != null) {
            System.out.println(sessionCookie);
            if (sessionCookie.startsWith("Set-Cookie:")
                    || sessionCookie.startsWith("Set-cookie:")) {
                break;
            }
        }

        if (sessionCookie == null) {
            throw new Exception("Missing Set-Cookie response header");
        }

        if (sessionCookie.indexOf("MYJSESSIONID=") == -1) {
            throw new Exception("Missing session id");
        }

        if (sessionCookie.indexOf("Comment=myComment") == -1) {
            throw new Exception("Missing cookie comment");
        }      

        if (sessionCookie.indexOf("Domain=mydomain") == -1) {
            throw new Exception("Missing cookie domain");
        }      

        if (sessionCookie.indexOf("Path=/myPath") == -1) {
            throw new Exception("Missing cookie path");
        }      

        if (sessionCookie.indexOf("Secure") == -1) {
            throw new Exception("Missing Secure attribute");
        }      

        if (sessionCookie.indexOf("HttpOnly") == -1) {
            throw new Exception("Missing HttpOnly attribute");
        }      

    }
}
