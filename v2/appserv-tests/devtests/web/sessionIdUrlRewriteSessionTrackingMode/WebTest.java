import java.io.*;
import java.net.*;
import com.sun.ejte.ccl.reporter.*;

/*
 * Unit test for making sure that if SessionTrackingMode.URL is set on the
 * ServletContext (by the test's ServletContextListener), then there will be
 * no session cookie present in the response, and the redirect URL will get
 * rewritten with the session id appended to it.
 */
public class WebTest {

    private static SimpleReporterAdapter stat
        = new SimpleReporterAdapter("appserv-tests");
    private static final String TEST_NAME = "session-id-url-rewrite-session-tracking-mode";

    private static final String EXPECTED = "MY_SESSION_ATTRIBUTE";

    private String host;
    private String port;
    private String contextRoot;
    private boolean fail = false;

    public WebTest(String[] args) {
        host = args[0];
        port = args[1];
        contextRoot = args[2];
    }
    
    public static void main(String[] args) {
        stat.addDescription("URL rewriting with session id");
        WebTest webTest = new WebTest(args);
        webTest.doTest();
        stat.printSummary(TEST_NAME);
    }

    public void doTest() {
     
        try { 
            invokeServlet();
        } catch (Exception ex) {
            ex.printStackTrace();
            stat.addStatus(TEST_NAME, stat.FAIL);
        }

        if (fail) {
            stat.addStatus(TEST_NAME, stat.FAIL);
        } else {
            stat.addStatus(TEST_NAME, stat.PASS);
        }

        return;
    }

    private void invokeServlet() throws Exception {
         
        Socket sock = new Socket(host, new Integer(port).intValue());
        OutputStream os = sock.getOutputStream();
        String get = "GET " + contextRoot + "/redirectFrom" + " HTTP/1.0\n";
        System.out.println(get);
        os.write(get.getBytes());
        os.write("\n".getBytes());
        
        InputStream is = sock.getInputStream();
        BufferedReader bis = new BufferedReader(new InputStreamReader(is));

        String line = null;
        String redirectLine = null;
        String cookieLine = null;
        int i=0;
        while ((line = bis.readLine()) != null) {
            System.out.println(i++ + ": " + line);
            if (line.startsWith("Location:")) {
                redirectLine = line;
            } else if (line.startsWith("Set-Cookie:")) {
                cookieLine = line;
            }
        }

        if (cookieLine != null) {
            System.err.println("Unexpected Set-Cookie response header");
            fail = true;
            return;
        }
        
        if (redirectLine == null) {
            System.err.println("Missing Location response header");
            fail = true;
            return;
        }

        int index = redirectLine.indexOf("http");
        if (index == -1) {
            System.err.println(
                "Missing http address in Location response header");
            fail = true;
            return;
        }

        String redirectTo = redirectLine.substring(index);
        if (redirectTo.indexOf(".") != -1){
            redirectTo = redirectTo.replace("localhost.localdomain","localhost");
        }   
        System.out.println("Redirect to: " + redirectTo);
        URL url = new URL(redirectTo);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.connect();
        int responseCode = conn.getResponseCode();
        if (responseCode != 200) { 
            System.err.println("Wrong response code. Expected: 200"
                               + ", received: " + responseCode);
            fail = true;
            return;
        }

        bis = new BufferedReader(new InputStreamReader(conn.getInputStream()));
        while ((line = bis.readLine()) != null) {
            if (line.equals(EXPECTED)) {
                break;
            }
        }

        if (line == null) {
            System.err.println("Did not receive expected response data: "
                               + EXPECTED);
            fail = true;
        }
    }
}
