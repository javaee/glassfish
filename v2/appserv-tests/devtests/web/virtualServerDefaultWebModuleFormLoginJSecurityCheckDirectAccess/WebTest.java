import java.io.*;
import java.net.*;
import com.sun.ejte.ccl.reporter.*;

/*
 * Unit test for:
 *
 *   https://glassfish.dev.java.net/issues/show_bug.cgi?id=4438
 *   ("Form-based authentication with default-web-module problematic")
 *
 * This unit test deploys a webapp that uses FORM based authentication, sets
 * the webapp as the virtual server's default-web-module, and then accesses
 * /j_security_check directly, which must not result in a 404 response.
 */
public class WebTest {

    private static final String TEST_NAME = "virtual-server-default-web-module-form-login-jsecurity-check-direct-access";
    private static final String JSESSIONID = "JSESSIONID";

    private static final String EXPECTED = "SUCCESS!";

    private static SimpleReporterAdapter stat
        = new SimpleReporterAdapter("appserv-tests");

    private String host;
    private String port;
    private String adminUser;
    private String adminPassword;

    public WebTest(String[] args) {
        host = args[0];
        port = args[1];
        adminUser = args[2];
        adminPassword = args[3];
    }
    
    public static void main(String[] args) {

        stat.addDescription("Unit test for GlassFish Issue 4438");
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

        /*
         * Access login.jsp
         */
        Socket sock = new Socket(host, new Integer(port).intValue());
        OutputStream os = sock.getOutputStream();
        String get = "GET "
            + "/j_security_check?j_username=" + adminUser
            + "&j_password=" + adminPassword
            + " HTTP/1.0\n";
        System.out.println(get);
        os.write(get.getBytes());
        os.write("\n".getBytes());

        InputStream is = sock.getInputStream();
        BufferedReader br = new BufferedReader(new InputStreamReader(is));

        String line = null;
        String location = null;
        String cookie = null;
        while ((line = br.readLine()) != null) {
            System.out.println(line);
            if (line.startsWith("Location:")) {
                location = line;
            } else if (line.startsWith("Set-Cookie:")
                    || line.startsWith("Set-cookie:")) {
                cookie = line;
            }
        }

        if (cookie == null) {
            throw new Exception("Missing Set-Cookie response header");
        } else if (location == null) {
            throw new Exception("Missing Location response header");
        }

        String jsessionId = getSessionIdFromCookie(cookie, JSESSIONID);
        String redirect = location.substring("Location:".length()).trim();
        followRedirect(new URL(redirect).getPath(), jsessionId);
    }

    /*
     * Follow redirect to
     * http://<host>:<port>/web-form-login-jsecurity-check-direct-access/
     */
    private void followRedirect(String path, String jsessionId)
            throws Exception {

        Socket sock = new Socket(host, new Integer(port).intValue());
        OutputStream os = sock.getOutputStream();
        String get = "GET " + path + " HTTP/1.0\n";
        System.out.println(get);
        os.write(get.getBytes());
        String cookie = "Cookie: " + jsessionId + "\n";
        os.write(cookie.getBytes());
        os.write("\n".getBytes());
        
        InputStream is = sock.getInputStream();
        BufferedReader br = new BufferedReader(new InputStreamReader(is));

        String line = null;
        String cookieHeader = null;
        boolean accessGranted = false;
        while ((line = br.readLine()) != null) {
            System.out.println(line);
            if (line.contains("SUCCESS!")) {
                accessGranted = true;
            }
        }

        if (!accessGranted) {
            throw new Exception("Failed to access index.jsp");
        }
    }


    private String getSessionIdFromCookie(String cookie, String field) {

        String ret = null;

        int index = cookie.indexOf(field);
        if (index != -1) {
            int endIndex = cookie.indexOf(';', index);
            if (endIndex != -1) {
                ret = cookie.substring(index, endIndex);
            } else {
                ret = cookie.substring(index);
            }
            ret = ret.trim();
        }

        return ret;
    }
}
