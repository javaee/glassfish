import java.io.*;
import java.net.*;
import com.sun.ejte.ccl.reporter.*;

/*
 * Unit test for https://glassfish.dev.java.net/issues/show_bug.cgi?id=3374
 * (FORM authenticator should issue a redirect (instead of a request
 * dispatch "forward") to the login page)
 */
public class WebTest {

    private static final String TEST_NAME
        = "form-login-transport-guarantee-confidential";

    private static SimpleReporterAdapter stat
        = new SimpleReporterAdapter("appserv-tests");

    private String host;
    private String httpPort;
    private String httpsPort;
    private String contextRoot;

    public WebTest(String[] args) {
        host = args[0];
        httpPort = args[1];
        httpsPort = args[2];
        contextRoot = args[3];
    }
    
    public static void main(String[] args) {

        stat.addDescription("Unit test for GlassFish Issue 3374");
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

        URL url = new URL("http://" + host  + ":" + httpPort + contextRoot
                          + "/protected.jsp");
        System.out.println(url.toString());
        URLConnection conn = url.openConnection();
        String redirectLocation = conn.getHeaderField("Location");
        System.out.println("Location: " + redirectLocation);
        
        String expectedRedirectLocation = "https://" + host + ":" + httpsPort
            + contextRoot + "/login.jsp";
        if (!expectedRedirectLocation.equals(redirectLocation)) {
            throw new Exception("Unexpected redirect location");
        }   
    }
}
