import java.io.*;
import java.net.*;
import com.sun.ejte.ccl.reporter.*;

/*
 * Unit test for customizing complete list of session tracking cookie
 * properties programmatically (from a ServletContextListener).
 */
public class WebTest {

    private static String TEST_NAME = "session-cookie-config-programmatic";

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

        stat.addDescription("Unit test for customizing complete list of " +
                            "session tracking cookie properties " +
                            "programmatically");
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

        String url = "http://" + host + ":" + port + contextRoot
                     + "/CreateSession";
        HttpURLConnection conn = (HttpURLConnection)
            (new URL(url)).openConnection();

        int code = conn.getResponseCode();
        if (code != 200) {
            throw new Exception("Unexpected return code: " + code);
        }

        String sessionCookie = conn.getHeaderField("Set-Cookie");
        System.out.println("Response cookie: " + sessionCookie);

        if (sessionCookie == null) {
            throw new Exception("Missing Set-Cookie response header");
        }

        // name
        if (sessionCookie.indexOf("MYJSESSIONID=") == -1) {
            throw new Exception("Missing session id");
        }

        // comment
        if (sessionCookie.indexOf("Comment=myComment") == -1) {
            throw new Exception("Missing cookie comment");
        }      

        // domain
        if (sessionCookie.indexOf("Domain=mydomain") == -1) {
            throw new Exception("Missing cookie domain");
        }      

        // path
        if (sessionCookie.indexOf("Path=/myPath") == -1) {
            throw new Exception("Missing cookie path");
        }      

        // secure
        if (sessionCookie.indexOf("Secure") == -1) {
            throw new Exception("Missing Secure attribute");
        }      

        // http-only
        if (sessionCookie.indexOf("HttpOnly") == -1) {
            throw new Exception("Missing HttpOnly attribute");
        }      

        // max-age
        if (sessionCookie.indexOf("Max-Age=123") == -1) {
            throw new Exception("Missing max-age");
        }      
    }
}
