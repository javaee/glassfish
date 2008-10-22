import java.io.*;
import java.net.*;
import com.sun.ejte.ccl.reporter.*;

/*
 * Unit test for CR 6376017 ("Erroneous values for request.getPathInfo() and
 * request.getPathTranslated()").
 */
public class WebTest {

    private static final String TEST_NAME =
        "servlet-request-getPathInfo-getPathTranslated";

    private static final String EXPECTED_RESPONSE = "/Page1.jsp";

    private static SimpleReporterAdapter stat
        = new SimpleReporterAdapter("appserv-tests");

    private String host;
    private String port;
    private String contextRoot;
    private String appserverHome;
    private String expectedResponse;
    private boolean fail = false;

    public WebTest(String[] args) {

        host = args[0];
        port = args[1];
        contextRoot = args[2];
        appserverHome = args[3];

        expectedResponse = appserverHome
                + "/domains/domain1/applications"
                + contextRoot + "-web"
                + EXPECTED_RESPONSE;
    }

    public static void main(String[] args) {
        stat.addDescription("Unit test for CR 6376017");
        WebTest webTest = new WebTest(args);
        webTest.doTest();
        stat.printSummary(TEST_NAME);
    }

    public void doTest() {
     
        try { 
            invoke();
        } catch (Exception ex) {
            ex.printStackTrace();
            fail = true;
        }

        if (fail) {
            stat.addStatus(TEST_NAME, stat.FAIL);
        } else {
            stat.addStatus(TEST_NAME, stat.PASS);
        }
    }

    public void invoke() throws Exception {

        Socket sock = new Socket(host, new Integer(port).intValue());
        OutputStream os = sock.getOutputStream();
        String get = "GET " + contextRoot + " HTTP/1.0\n";
        System.out.println(get);
        os.write(get.getBytes());
        os.write("\n".getBytes());
        
        InputStream is = sock.getInputStream();
        BufferedReader bis = new BufferedReader(new InputStreamReader(is));

        String line = null;
        int i=0;
        while ((line = bis.readLine()) != null) {
            System.out.println(i++ + ": " + line);
            if (line.startsWith("Location:")) {
                break;
            }
        }

        if (line == null) {
            System.err.println("Missing Location response header");
            fail = true;
            return;
        }

        int index = line.indexOf("http");
        if (index == -1) {
            System.err.println(
                "Missing http address in Location response header");
            fail = true;
            return;
        }

        String redirectTo = line.substring(index);
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

        processResponse(conn);
    }


    private void processResponse(HttpURLConnection conn) throws Exception {

        BufferedReader br = new BufferedReader(
                        new InputStreamReader(conn.getInputStream()));

        boolean first = true;
        String line = null;
        int pos = expectedResponse.indexOf("workspace");
        if (pos>=0) {
            expectedResponse = expectedResponse.substring(pos);
        }
        while ((line = br.readLine()) != null) {
            pos = line.indexOf("workspace");
            if (pos>=0) {
                line = line.substring(pos);
            }
            if (first) {
                if (!EXPECTED_RESPONSE.equals(line)) {
                    System.err.println("Wrong response, expected: "
                                       + EXPECTED_RESPONSE
                                       + ". received: " + line);
                    fail = true;
                    return;
                }
                first = false;
            } else if (!expectedResponse.equals(line)) {
                System.err.println("Wrong response, expected: "
                                   + expectedResponse
                                   + ". received: " + line);
                fail = true;
                return;
            }
        }
    }
}
