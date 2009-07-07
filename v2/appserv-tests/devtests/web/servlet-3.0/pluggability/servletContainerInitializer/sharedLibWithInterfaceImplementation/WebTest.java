import java.io.*;
import java.net.*;
import com.sun.ejte.ccl.reporter.*;

/*
 * Unit test for puggability
 */
public class WebTest {

    private static SimpleReporterAdapter stat
        = new SimpleReporterAdapter("appserv-tests");
    private static final String TEST_NAME = "pluggability-sharedlib-interfaceImpl";
    private static final String EXPECTED_RESPONSE = "4CALLED INTFIMPL-SHAREDLIB-1;null;null;null;null;null;";

    private String host;
    private String port;
    private String contextRoot;

    public WebTest(String[] args) {
        host = args[0];
        port = args[1];
        contextRoot = args[2];
    }
    
    public static void main(String[] args) {
        stat.addDescription("Unit test for pluggability of sharedlib with interrace implementation and extensions");
        WebTest webTest = new WebTest(args);
        webTest.doTest("/mytest1");
        stat.printSummary(TEST_NAME);
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
            InputStream is = null;
            BufferedReader input = null;
            String line = null;
            try {
                is = conn.getInputStream();
                input = new BufferedReader(new InputStreamReader(is));
                line = input.readLine();
            } finally {
                try {
                    if (is != null) {
                        is.close();
                    }
                } catch(IOException ex) {
                    // ignore
                }
                try {
                    if (input != null) {
                        input.close();
                    }
                } catch(IOException ex) {
                    // ignore
                }
            }
            if (EXPECTED_RESPONSE.equals(line)) {
                System.out.println("RESPONSE : " + line);
                stat.addStatus(TEST_NAME, stat.PASS);
            } else {
                System.out.println("Wrong response. Expected: " + 
                        EXPECTED_RESPONSE + ", received: " + line);
                stat.addStatus(TEST_NAME, stat.FAIL);
            }
        }    
    }
}
