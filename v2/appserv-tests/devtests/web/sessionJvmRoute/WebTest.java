import java.io.*;
import java.net.*;
import com.sun.ejte.ccl.reporter.*;

/*
 * Unit test for:
 *
 *  https://glassfish.dev.java.net/issues/show_bug.cgi?id=3796
 *  ("Add support for AJP/mod_jk load balancing")
 *
 * and
 *
 *  https://glassfish.dev.java.net/issues/show_bug.cgi?id=7101
 *  ("jvmRoute not reset in JSESSIONID Cookie during fail-over")
 *
 * This unit test defines a system property with name jvmRoute and value
 * MYINSTANCE, invokes a servlet that creates a session, and expects a
 * JSESSIONID response cookie.with the string ".MYINSTANCE" appended to its
 * session id.
 *
 * In a subsequent request, it includes the JSESSIONID from the response
 * and invokes a servlet that resumes this session. This servlet first checks
 * to make sure that the string ".MYINSTANCE" has been removed (by the
 * container) from the session id. The servlet then adds its own cookie to
 * the response, in order to mimic the root cause of IT 7101. The client
 * verifies that the response still contains a JSESSIONID response cookie
 * with the string ".MYINSTANCE" appended to its session id.
 */
public class WebTest {

    private static SimpleReporterAdapter stat
        = new SimpleReporterAdapter("appserv-tests");
    private static final String TEST_NAME = "session-jvm-route";

    private String host;
    private String port;
    private String contextRoot;

    public WebTest(String[] args) {
        host = args[0];
        port = args[1];
        contextRoot = args[2];
    }
    
    public static void main(String[] args) {
        stat.addDescription("Unit test for GlassFish Issue 3796");
        new WebTest(args).doTest();
        stat.printSummary(TEST_NAME);
    }

    public void doTest() {
        try { 
            invoke();
            stat.addStatus(TEST_NAME, stat.PASS);
        } catch (Exception ex) {
            System.out.println(TEST_NAME + " test failed");
            stat.addStatus(TEST_NAME, stat.FAIL);
            ex.printStackTrace();
        }
    }

    private void invoke() throws Exception {

        /*
         * Create session
         */        
        String url = "http://" + host + ":" + port + contextRoot
                     + "/CreateSession";
        System.out.println("Connecting to: " + url);
        HttpURLConnection conn = (HttpURLConnection)
            (new URL(url)).openConnection();

        int code = conn.getResponseCode();
        if (code != 200) {
            throw new Exception("Unexpected return code: " + code);
        }

        String sessionCookieHeader = conn.getHeaderField("Set-Cookie");
        System.out.println("Response cookie: " + sessionCookieHeader);
        if (sessionCookieHeader == null) {
            throw new Exception("Missing session cookie response header");
        }
        if (sessionCookieHeader.indexOf(".MYINSTANCE") == -1) {
            throw new Exception("Session cookie does not have any JVMROUTE");
        }

        /*
         * Resume session
         */        
        Socket sock = new Socket(host, new Integer(port).intValue());
        OutputStream os = sock.getOutputStream();
        String getRequestLine = "GET " + contextRoot + "/CheckSession" +
            " HTTP/1.0\n";
        System.out.println("\nConnecting to: " + getRequestLine); 
        os.write(getRequestLine.getBytes());
        os.write(("Cookie: " + sessionCookieHeader + "\n").getBytes());
        os.write("\n".getBytes());
        
        sessionCookieHeader = null;
        InputStream is = null;
        BufferedReader bis = null;
        String line = null;
        boolean okStatus = false;
        try {
            is = sock.getInputStream();
            bis = new BufferedReader(new InputStreamReader(is));
            while ((line = bis.readLine()) != null) {
                System.out.println(line);
                if (line.equals("HTTP/1.1 200 OK")) {
                    okStatus = true;
                } else if (line.startsWith("Set-Cookie:") &&
                        line.indexOf("JSESSIONID") != -1) {
                    sessionCookieHeader = line;
                }
            }
        } finally {
            try {
                if (is != null) {
                    is.close();
                }
            } catch (IOException ioe) {
                // ignore
            }
            try {
                if (bis != null) {
                    bis.close();
                }
            } catch (IOException ioe) {
                // ignore
            }
        }

        if (!okStatus) {
            throw new Exception("Unexpected response status, expected OK");
        }

        if (sessionCookieHeader == null) {
            throw new Exception("Missing session cookie response header");
        }
        if (sessionCookieHeader.indexOf(".MYINSTANCE") == -1) {
            throw new Exception("Session cookie does not have any JVMROUTE");
        }

    }
}
