import java.io.*;
import java.net.*;
import java.util.List;
import java.util.Map;
import com.sun.ejte.ccl.reporter.*;

/** 
 * Unit test for:
 *
 *  Multi-byte context root with session cookie
 *
 * Multi-byte context root is specified in sun-web.xml.
 */
public class WebTest {

    private static final String TEST_NAME = "multi-byte-context-root-with-cookie";
    private static final String JSESSIONID = "JSESSIONID";

    private static final String EXPECTED_RESPONSE = "abc=def; myid=123@456";

    private static SimpleReporterAdapter stat
        = new SimpleReporterAdapter("appserv-tests");

    private String host;
    private String port;

    public WebTest(String[] args) {
        host = args[0];
        port = args[1];
    }
    
    public static void main(String[] args) {

        stat.addDescription("Unit test for Multi-Byte Context Root with Session Cookie");
        WebTest webTest = new WebTest(args);

        try {
            String sessionId = webTest.doSetJsp();
            boolean expected = false;

            if (sessionId != null) {
                expected = webTest.doGetJsp(sessionId);
            }
            if (expected) {
                stat.addStatus(TEST_NAME, stat.PASS);
            } else {
                stat.addStatus(TEST_NAME, stat.FAIL);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            stat.addStatus(TEST_NAME, stat.FAIL);
        }

	    stat.printSummary();
    }

    public String doSetJsp() throws Exception {
     
        String sessionId = null;

        URL url = new URL("http://" + host  + ":" + port + "/"
            + "good-%E5%A5%BD-good/set.jsp");
        System.out.println("Connecting to: " + url.toString());

        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.connect();
        int responseCode = conn.getResponseCode();

        if (responseCode == 200) {
            Map<String, List<String>> headers = conn.getHeaderFields();
            List<String> cookies = headers.get("Set-Cookie");
            if (cookies == null) {
                cookies = headers.get("Set-cookie");
            }
            System.out.println("Cookies = " + cookies);

            sessionId = getCookieField(cookies, JSESSIONID);
            System.out.println("sessionId = " + sessionId);
        } else {   
            System.err.println("Unexpected return code: " + responseCode);
        }

        return sessionId;
    }

    private boolean doGetJsp(String sessionId) throws Exception {
        boolean expected = false;

        URL url = new URL("http://" + host  + ":" + port + "/"
            + "good-%E5%A5%BD-good/get.jsp");
        System.out.println("Connecting to: " + url.toString());

        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        //String cookie = sessionId + "; myid=123@456";
        String cookie = "$Version=1; " + sessionId + "; myid=\"123@456\"";
        conn.setRequestProperty("Cookie", cookie); 
        conn.connect();
        int responseCode = conn.getResponseCode();

        if (responseCode == 200) {
            InputStream is = conn.getInputStream();
            BufferedReader br = new BufferedReader(new InputStreamReader(is));
            String line = br.readLine();
            System.out.println("Response=" + line);
            if (EXPECTED_RESPONSE.equals(line)) {
                expected = true;
            } else {
                System.err.println("Wrong response. Expected: " + 
                                   EXPECTED_RESPONSE + ", received: " + line);
            }
        } else {   
            System.err.println("Unexpected return code: " + responseCode);
        }

        return expected;
    }

    private String getCookieField(List<String> cookies, String field) {
        String ret = null;

        if (cookies != null) {
            for (String cookie : cookies) {
                if (cookie.startsWith(field)) {
                    int index = 0;
                    int endIndex = cookie.indexOf(';', index);
                    if (endIndex != -1) {
                        ret = cookie.substring(index, endIndex);
                    } else {
                        ret = cookie.substring(index);
                    }
                    ret = ret.trim();
                    break;
                }
            }
        }

        return ret;
    }
}
