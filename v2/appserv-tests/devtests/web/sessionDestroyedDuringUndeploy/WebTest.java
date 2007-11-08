import java.io.*;
import java.net.*;
import com.sun.ejte.ccl.reporter.*;

/*
 * Unit test for https://glassfish.dev.java.net/issues/show_bug.cgi?id=834
 * ("Sessions not invalidated on Redeploy"):
 *
 * Make sure that if a client authenticates to a webapp using form-based login,
 * it will have to re-login when accessing the same webapp after it has been
 * redeployed.
 */
public class WebTest {

    private static final String TEST_NAME = "session-destroyed-during-undeploy";
    private static final String JSESSIONID = "JSESSIONID";
    private static final String JSESSIONIDSSO = "JSESSIONIDSSO";

    private static final String EXPECTED = "SUCCESS!";

    private static SimpleReporterAdapter stat
        = new SimpleReporterAdapter("appserv-tests");

    private String host;
    private String port;
    private String contextRoot;
    private String adminUser;
    private String adminPassword;
    private String run;

    public WebTest(String[] args) {
        host = args[0];
        port = args[1];
        contextRoot = args[2];
        adminUser = args[3];
        adminPassword = args[4];
        run = args[5];
    }
    
    public static void main(String[] args) {

        stat.addDescription("Unit test for GlassFish Issue 834");
        WebTest webTest = new WebTest(args);

        try {
            if ("firstRun".equals(webTest.run)) {
                webTest.firstRun();
            } else {
                webTest.secondRun();
            }
        } catch( Exception ex) {
            ex.printStackTrace();
            stat.addStatus(TEST_NAME, stat.FAIL);
        }

	stat.printSummary();
    }

    public void firstRun() throws Exception {

        String jsessionId = accessIndexDotJsp();
        String redirect = accessLoginPage(jsessionId);
        String jsessionIdSSO = followRedirect(new URL(redirect).getPath(),
                                              jsessionId);

        // Store the JSESSIONIDSSO in a file
        FileOutputStream fos = new FileOutputStream(JSESSIONIDSSO);
        OutputStreamWriter osw = new OutputStreamWriter(fos);
        osw.write(jsessionIdSSO);
        osw.close();

        stat.addStatus(TEST_NAME, stat.PASS);
    }

    public void secondRun() throws Exception {
        // Read the JSESSIONIDSSO from the previous run
        FileInputStream fis = new FileInputStream(JSESSIONIDSSO);
        BufferedReader br = new BufferedReader(new InputStreamReader(fis));
        accessIndexDotJsp(br.readLine());
        new File(JSESSIONIDSSO).delete();

        stat.addStatus(TEST_NAME, stat.PASS);
    }

    /*
     * Attempt to access index.jsp resource protected by FORM based login.
     */
    private String accessIndexDotJsp() throws Exception {

        URL url = new URL("http://" + host  + ":" + port + contextRoot
                          + "/index.jsp");
        System.out.println("Connecting to: " + url.toString());
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.connect();
        int responseCode = conn.getResponseCode();
        System.out.println("Response code: " + responseCode);
        if (responseCode == HttpURLConnection.HTTP_MOVED_TEMP) {
            url = new URL(conn.getHeaderField("Location"));
            System.out.println("Redirected to: " + url.toString());
            conn = (HttpURLConnection) url.openConnection();
            conn.connect();
            responseCode = conn.getResponseCode();
        }

        String cookie = null;
        if (responseCode == HttpURLConnection.HTTP_OK) {
            InputStream is = conn.getInputStream();
            cookie = conn.getHeaderField("set-cookie");
        }
        if (cookie == null) {
            throw new Exception("Missing Set-Cookie response header");
        }

        return getSessionIdFromCookie(cookie, JSESSIONID);
    }

    /*
     * Access login.jsp.
     */
    private String accessLoginPage(String jsessionId) throws Exception {

        Socket sock = new Socket(host, new Integer(port).intValue());
        OutputStream os = sock.getOutputStream();
        String get = "GET " + contextRoot
            + "/j_security_check?j_username=" + adminUser
            + "&j_password=" + adminPassword
            + " HTTP/1.0\n";
        System.out.println(get);
        os.write(get.getBytes());
        String cookie = "Cookie: " + jsessionId + "\n";
        os.write(cookie.getBytes());
        os.write("\n".getBytes());
        
        InputStream is = sock.getInputStream();
        BufferedReader br = new BufferedReader(new InputStreamReader(is));

        String line = null;
        while ((line = br.readLine()) != null) {
            System.out.println(line);
            if (line.startsWith("Location:")) {
                break;
            }
        }

        if (line == null) {
            throw new Exception("Missing Location response header");
        }

        return line.substring("Location:".length()).trim();
    }

    /*
     * Follow redirect to
     * http://<host>:<port>/web-session-destroyed-during-undeploy/index.jsp
     * and access this resource.
     */
    private String followRedirect(String path, String jsessionId)
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
            if (line.startsWith("Set-Cookie:")
                    || line.startsWith("Set-cookie:")) {
                cookieHeader = line;
            } else if (line.contains("SUCCESS!")) {
                accessGranted = true;
            }
        }

        if (cookieHeader == null) {
            throw new Exception("Missing Set-Cookie response header");
        }

        if (!accessGranted) {
            throw new Exception("Failed to access index.jsp");
        }

        return getSessionIdFromCookie(cookieHeader, JSESSIONIDSSO);
    }

    /*
     * Attempt to access index.jsp resource protected by FORM based login,
     * supplying JSESSIONIDSSO from previous run.
     */
    private void accessIndexDotJsp(String jsessionIdSSO) throws Exception {
        Socket sock = new Socket(host, new Integer(port).intValue());
        OutputStream os = sock.getOutputStream();
        String get = "GET " + contextRoot + "/index.jsp" + " HTTP/1.0\n";
        System.out.println(get);
        os.write(get.getBytes());
        String cookie = "Cookie: " + jsessionIdSSO + "\n";
        os.write(cookie.getBytes());
        os.write("\n".getBytes());
        
        InputStream is = sock.getInputStream();
        BufferedReader br = new BufferedReader(new InputStreamReader(is));

        /*
         * Make sure that a login is required:
         *
         * The response must container either a redirect to the login page,
         * or if the container dispatches the request to the login page
         * (using RD.forward() internally), it must contain the
         * j_security_check action from the login page.
         *
         * See https://glassfish.dev.java.net/issues/show_bug.cgi?id=3374
         * for why a redirect to the login page may occur.
         */
        String line = null;
        String redirectUrl = "Location: http://" + host + ":" + port
            + contextRoot + "/login.jsp"; 
        boolean loginRequired = false;
        while ((line = br.readLine()) != null) {
            System.out.println(line);
            if (line.equals(redirectUrl)
                    || line.contains("j_security_check")) {
                loginRequired = true;
                break;
            }
        }

        if (!loginRequired) {
            throw new Exception("No login required");
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
