import java.io.*;
import java.net.*;
import com.sun.ejte.ccl.reporter.*;

/*
 * Unit test for customizing the name of the session tracking cookie.
 *
 * This test leverages a ServletContextListener to change the name
 * of the session tracking cookie from JSESSIONID to MYJSESSIONID.
 */
public class WebTest {

    private static String TEST_NAME = "session-cookie-custom-name-programmtic";

    private static final String MYJSESSIONID = "MYJSESSIONID";

    private static final String EXPECTED_RESPONSE = "HTTP/1.1 200 OK";
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

        stat.addDescription("Unit test for customizing name of session " +
                            "tracking cookie");
        WebTest webTest = new WebTest(args);

        try {
            webTest.secondRun(webTest.firstRun());
            stat.addStatus(TEST_NAME, stat.PASS);
        } catch( Exception ex) {
            ex.printStackTrace();
            stat.addStatus(TEST_NAME, stat.FAIL);
        }

	stat.printSummary();
    }

    public String firstRun() throws Exception {

        Socket sock = new Socket(host, new Integer(port).intValue());
        OutputStream os = sock.getOutputStream();
        String get = "GET " + contextRoot + "/CreateSession" + " HTTP/1.0\n";
        System.out.println(get);
        os.write(get.getBytes());
        os.write("\r\n".getBytes());
        
        // Get the MYJSESSIONID from the response
        InputStream is = sock.getInputStream();
        BufferedReader br = new BufferedReader(new InputStreamReader(is));
        String line = null;
        while ((line = br.readLine()) != null) {
            System.out.println(line);
            if (line.startsWith("Set-Cookie:")
                    || line.startsWith("Set-cookie:")) {
                break;
            }
        }

        if (line == null) {
            throw new Exception("Missing Set-Cookie response header");
        }

        return getSessionCookie(line, MYJSESSIONID);
    }

    public void secondRun(String sessionCookie) throws Exception {

        Socket sock = new Socket(host, new Integer(port).intValue());
        OutputStream os = sock.getOutputStream();
        String get = "GET " + contextRoot + "/ResumeSession" + " HTTP/1.0\n";
        System.out.println(get);
        os.write(get.getBytes());
        String cookie = "Cookie: " + sessionCookie + "\n";
        os.write(cookie.getBytes());
        os.write("\r\n".getBytes());
        
        InputStream is = sock.getInputStream();
        BufferedReader br = new BufferedReader(new InputStreamReader(is));

        String line = null;
        boolean found = false;
        while ((line = br.readLine()) != null) {
            System.out.println(line);
            if (line.contains(EXPECTED_RESPONSE)) {
                found = true;
                break;
            }
        }

        if (found) {
            stat.addStatus(TEST_NAME, stat.PASS);
        } else {
            throw new Exception("Wrong response. Expected response: "
                                + EXPECTED_RESPONSE + " not found");
        }
    }

    private String getSessionCookie(String header, String cookieName) {

        String ret = null;

        int index = header.indexOf(cookieName);
        if (index != -1) {
            int endIndex = header.indexOf(';', index);
            if (endIndex != -1) {
                ret = header.substring(index, endIndex);
            } else {
                ret = header.substring(index);
            }
            ret = ret.trim();
        }

        return ret;
    }
}
