import java.io.*;
import java.net.*;
import com.sun.ejte.ccl.reporter.*;

/*
 * Unit test for
 *
 *   https://glassfish.dev.java.net/issues/show_bug.cgi?id=4394
 *   ("server log message says enableURLRewriting is not supported")
 *
 * Make sure that if "enableURLRewriting" is set to false in sun-web.xml,
 * the JSESSIONID of a newly created session will be present only in a
 * response cookie, but not in a rewritten redirect URL.
 */
public class WebTest {

    private static SimpleReporterAdapter stat = new SimpleReporterAdapter(
        "appserv-tests");
    private static final String TEST_NAME = "session-id-present-only-in-cookie-if-url-rewriting-disabled";
    private static final String JSESSIONID = "JSESSIONID";

    private String host;
    private String port;
    private String contextRoot;

    public WebTest(String[] args) {
        host = args[0];
        port = args[1];
        contextRoot = args[2];
    }
    
    public static void main(String[] args) {
        stat.addDescription("Unit test for IT 4394");
        WebTest webTest = new WebTest(args);
        webTest.doTest();
        stat.printSummary(TEST_NAME);
    }

    public void doTest() {     
        try { 
            runTest();
            stat.addStatus(TEST_NAME, stat.PASS);
        } catch (Exception ex) {
            ex.printStackTrace();
            stat.addStatus(TEST_NAME, stat.FAIL);
        }
    }

    private void runTest() throws Exception {
         
        Socket sock = new Socket(host, new Integer(port).intValue());
        OutputStream os = sock.getOutputStream();
        String get = "GET " + contextRoot + "/createSession" + " HTTP/1.0\n";
        System.out.println(get);
        os.write(get.getBytes());
        os.write("\n".getBytes());
        
        InputStream is = sock.getInputStream();
        BufferedReader bis = new BufferedReader(new InputStreamReader(is));
        String line = null;
        String locationHeader = null;
        String cookieHeader = null;
        while ((line = bis.readLine()) != null) {
            System.out.println(line);
            if (line.startsWith("Location:")) {
                locationHeader = line;
            } else if (line.startsWith("Set-Cookie:")) {
                cookieHeader = line;
            }
        }

        if (cookieHeader == null) {
            throw new Exception("Missing Set-Cookie response header");
        }
        
        if (locationHeader == null) {
            throw new Exception("Missing Location response header");
        }

        String sessionId = getSessionIdFromCookie(cookieHeader, JSESSIONID);
        if (sessionId == null) {
            throw new Exception("Missing JSESSIONID in Set-Cookie response header");
        }

        // Make sure that there is no JSESSIONID present in the redirect URL
        if (locationHeader.indexOf(";jsessionid=" + sessionId) != -1) {
            throw new Exception("Unexpected jsessionid in redirect URL");
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
            ret = ret.substring("JSESSIONID=".length());
        }

        return ret;
    }

}
