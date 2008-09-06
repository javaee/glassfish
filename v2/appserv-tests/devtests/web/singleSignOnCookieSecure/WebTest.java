import java.io.*;
import java.net.*;
import com.sun.ejte.ccl.reporter.*;

/*
 * Unit test for:
 *
 *  https://glassfish.dev.java.net/issues/show_bug.cgi?id=3823
 *  ("Make 'Secure' flag of JSESSIONIDSSO cookie configurable")
 *
 * This test sets both the 'sso-enabled' and 'ssoCookieSecure' properties
 * of the virtual-server named 'server' to true, and expects that
 * the response contains a JSESSIONIDSSO cookie with a 'Secure' flag.
 */
public class WebTest {

    private static final String TEST_NAME = "single-sign-on-cookie-secure";
    private static final String SSO_COOKIE = "Set-Cookie: JSESSIONIDSSO";

    private static SimpleReporterAdapter stat
        = new SimpleReporterAdapter("appserv-tests");

    private String host;
    private String port;
    private String contextRoot;
    private String adminUser;
    private String adminPassword;

    public WebTest(String[] args) {
        host = args[0];
        port = args[1];
        contextRoot = args[2];
        adminUser = "anonymous";
        adminPassword = "";
    }
    
    public static void main(String[] args) {

        stat.addDescription("Unit test for GlassFish Issue 3823");
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
        String get = "GET " + contextRoot
            + "/j_security_check?j_username=" + adminUser
            + "&j_password=" + adminPassword
            + " HTTP/1.0\n";
        System.out.println(get);
        os.write(get.getBytes());
        os.write("\n".getBytes());

        InputStream is = sock.getInputStream();
        BufferedReader br = new BufferedReader(new InputStreamReader(is));

        String line = null;
        while ((line = br.readLine()) != null) {
            System.out.println(line);
            if (line.startsWith(SSO_COOKIE)) {
                if (line.indexOf("Secure") == -1) {
                    throw new Exception("Missing 'Secure' flag in '" +
                                        SSO_COOKIE + "' response header");

                }
                break;
            }
        }

        if (line == null) {
            throw new Exception("Missing '" + SSO_COOKIE + "' response header");
        }
    }
}
