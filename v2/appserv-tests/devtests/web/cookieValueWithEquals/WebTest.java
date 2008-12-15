import java.io.*;
import java.net.*;
import com.sun.ejte.ccl.reporter.*;

/*
 * Unit test for issue 6875 ("cannot set cookie with value having =").
 */
public class WebTest {

    private static SimpleReporterAdapter stat
        = new SimpleReporterAdapter("appserv-tests");

    private static final String TEST_NAME = "cookie-value-with-equals";

    private static final String EXPECTED_RESPONSE = "SUCCESS";
    private static final String COOKIE = "mycookie=\"aaa=bbb=ccc\"";

    private String host;
    private String port;
    private String contextRoot;

    public WebTest(String[] args) {
        host = args[0];
        port = args[1];
        contextRoot = args[2];
    }
    
    public static void main(String[] args) {
        stat.addDescription("Unit test for CR 6456553");
        WebTest webTest = new WebTest(args);
        webTest.doTest();
        stat.printSummary(TEST_NAME);
    }

    public void doTest() {
     
        try { 
            invokeCreateCookie();
            invokeGetCookie();
            stat.addStatus(TEST_NAME, stat.PASS);
        } catch (Exception ex) {
            System.out.println(TEST_NAME + " test failed");
            stat.addStatus(TEST_NAME, stat.FAIL);
            ex.printStackTrace();
        }

        return;
    }

    private void invokeCreateCookie() throws Exception {

        URL url = new URL("http://" + host  + ":" + port + contextRoot
                          + "/createCookie");
        System.out.println(url.toString());

        URLConnection conn = url.openConnection();
        String cookie = conn.getHeaderField("Set-Cookie");
        System.out.println("Response cookie: " + cookie);
        if (cookie == null || !cookie.startsWith(COOKIE)) {
            throw new Exception("Missing or invalid Set-Cookie response header");
        }
    }

    private void invokeGetCookie() throws Exception {

        Socket sock = new Socket(host, new Integer(port).intValue());
        OutputStream os = sock.getOutputStream();
        String get = "GET " + contextRoot + "/getCookie HTTP/1.0\n";
        System.out.print(get);
        os.write(get.getBytes());
        String cookie = "Cookie: " + COOKIE + "\n";
        System.out.print("Request cookie header: " + cookie);
        os.write(cookie.getBytes());
        os.write("\n".getBytes());

        InputStream is = sock.getInputStream();
        BufferedReader br = new BufferedReader(new InputStreamReader(is));

        String line = null;
        while ((line = br.readLine()) != null) {
            if (EXPECTED_RESPONSE.equals(line)) {
                break;
            }
        }

        if (line == null) {
            throw new Exception("Wrong response body. Could not find "
                                + "expected string: " + EXPECTED_RESPONSE);
        }
    }
}
