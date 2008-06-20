import java.io.*;
import java.net.*;
import com.sun.ejte.ccl.reporter.*;

/*
 * Unit test for CR 6716503 ("AS9.1 Session deserialization does not
 * handle primitive class types")
 */
public class WebTest {

    private static String TEST_NAME;
    private static final String TEST_ROOT_NAME = "session-memory-persist-primitive-class";

    private static final String EXPECTED_RESPONSE = "Test passed!";
    private static final String JSESSIONID = "JSESSIONID";

    private static SimpleReporterAdapter stat
        = new SimpleReporterAdapter("appserv-tests");

    private String host;
    private String port;
    private String contextRoot;
    private String run;

    public WebTest(String[] args) {
        host = args[0];
        port = args[1];
        contextRoot = args[2];
        run = args[3];
    }
    
    public static void main(String[] args) {

        stat.addDescription("Unit test for CR 6716503");
        WebTest webTest = new WebTest(args);

        try {
            if ("first".equals(webTest.run)) {
                TEST_NAME = TEST_ROOT_NAME + "-first";
                webTest.firstRun();
            } else {
                TEST_NAME = TEST_ROOT_NAME + "-second";
                webTest.secondRun();
            }
        } catch( Exception ex) {
            ex.printStackTrace();
            stat.addStatus(TEST_NAME, stat.FAIL);
        }

	stat.printSummary();
    }

    public void firstRun() throws Exception {

        Socket sock = new Socket(host, new Integer(port).intValue());
        OutputStream os = sock.getOutputStream();
        String get = "GET " + contextRoot + "/test.jsp" + " HTTP/1.0\n";
        System.out.println(get);
        os.write(get.getBytes());
        os.write("\n".getBytes());
        
        InputStream is = sock.getInputStream();
        BufferedReader br = new BufferedReader(new InputStreamReader(is));

        // Get the JSESSIONID from the response
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

        String jsessionId = getSessionIdFromCookie(line, JSESSIONID);

        // Store the JSESSIONID in a file
        FileOutputStream fos = new FileOutputStream(JSESSIONID);
        OutputStreamWriter osw = new OutputStreamWriter(fos);
        osw.write(jsessionId);
        osw.close();

        stat.addStatus(TEST_NAME, stat.PASS);
    }

    public void secondRun() throws Exception {
        // Read the JSESSIONID from the previous run
        FileInputStream fis = new FileInputStream(JSESSIONID);
        BufferedReader br = new BufferedReader(new InputStreamReader(fis));
        String jsessionId = br.readLine();
        new File(JSESSIONID).delete();

        Socket sock = new Socket(host, new Integer(port).intValue());
        OutputStream os = sock.getOutputStream();
        String get = "GET " + contextRoot + "/ResumeSession" + " HTTP/1.0\n";
        System.out.println(get);
        os.write(get.getBytes());
        String cookie = "Cookie: " + jsessionId + "\n";
        os.write(cookie.getBytes());
        os.write("\n".getBytes());
        
        InputStream is = sock.getInputStream();
        br = new BufferedReader(new InputStreamReader(is));

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
