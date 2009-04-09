import java.io.*;
import java.net.*;
import com.sun.ejte.ccl.reporter.*;

/*
 * Unit test for puggability
 */
public class WebTest {

    private static SimpleReporterAdapter stat
        = new SimpleReporterAdapter("appserv-tests");
    private static final String TEST_NAME = "pluggable-sharedlibraries-war";
    private static final String[] EXPECTED_RESPONSE = {"none","CALLED SHAREDLIB-1;CALLED SHAREDLIB-2;null;null;CALLED APPLIB-1;null;","CALLED SHAREDLIB-1;CALLED SHAREDLIB-2;CALLED SHAREDLIB-3;null;null;CALLED APPLIB-2;"};

    private String host;
    private String port;
    private String testNumber;
    private String contextRoot;

    public WebTest(String[] args) {
        host = args[0];
        port = args[1];
        contextRoot = args[2];
        testNumber = args[3];
    }
    
    public static void main(String[] args) {
        stat.addDescription("Unit test for pluggable shared libraries in WAR");
        WebTest webTest = new WebTest(args);
	if("1".equals(args[3])) {
            webTest.doTest("/mytest1");
            stat.printSummary(TEST_NAME);
	}
	if("2".equals(args[3])) {
            webTest.doTest("/mytest2");
            stat.printSummary(TEST_NAME);
	}
    }

    public void doTest(String root) {
        try { 
            invoke(root);
        } catch (Exception ex) {
            System.out.println(TEST_NAME + " test failed");
            stat.addStatus(TEST_NAME, stat.FAIL);
            ex.printStackTrace();
        }
    }

    private void invoke(String root) throws Exception {
        
        String url = "http://" + host + ":" + port + contextRoot
                     + root;
        HttpURLConnection conn = (HttpURLConnection)
            (new URL(url)).openConnection();

        int code = conn.getResponseCode();
        if (code != 200) {
            System.out.println("Unexpected return code: " + code);
            stat.addStatus(TEST_NAME, stat.FAIL);
        } else {
            InputStream is = conn.getInputStream();
            BufferedReader input = new BufferedReader(new InputStreamReader(is));
            String line = input.readLine();
            if (EXPECTED_RESPONSE[(new Integer(testNumber)).intValue()].equals(line)) {
                System.out.println("RESPONSE : " + line);
                stat.addStatus(TEST_NAME, stat.PASS);
            } else {
                System.out.println("Wrong response. Expected: " + 
                        EXPECTED_RESPONSE[(new Integer(testNumber)).intValue()] + ", received: " + line);
                stat.addStatus(TEST_NAME, stat.FAIL);
            }
        }    
    }
}
