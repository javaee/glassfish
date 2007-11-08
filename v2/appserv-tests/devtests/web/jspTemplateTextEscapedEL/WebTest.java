import java.io.*;
import java.net.*;
import com.sun.ejte.ccl.reporter.*;

/*
 * Unit test for escaping EL expressions in template text:
 * In the target JSP page, neither EL expression must be evaluated, because
 * both are escaped. There used to be a problem with the first EL expression
 * in the page being evaluated, even though it was escaped.
 */
public class WebTest {

    private static SimpleReporterAdapter stat
        = new SimpleReporterAdapter("appserv-tests");

    private static final String TEST_NAME = "jsp-template-text-escaped-el";

    private String host;
    private String port;
    private String contextRoot;

    public WebTest(String[] args) {
        host = args[0];
        port = args[1];
        contextRoot = args[2];
    }
    
    public static void main(String[] args) {
        stat.addDescription("Unit test for escaping EL expressions in template text");
        WebTest webTest = new WebTest(args);
        webTest.doTest();
        stat.printSummary(TEST_NAME);
    }

    public void doTest() {     
        try { 
            invokeJsp();
        } catch (Exception ex) {
            System.out.println(TEST_NAME + " test failed.");
            stat.addStatus(TEST_NAME, stat.FAIL);
            ex.printStackTrace();
        }
    }

    public void invokeJsp() throws Exception {
     
        String url = "http://" + host + ":" + port + "/" + contextRoot
            + "/jsp/test.jsp";
        HttpURLConnection conn = (HttpURLConnection)
            (new URL(url)).openConnection();

        int code = conn.getResponseCode();
        if (code != 200) {
            System.err.println("Unexpected return code: " + code);
            stat.addStatus(TEST_NAME, stat.FAIL);
        } 

        InputStream is = conn.getInputStream();
        BufferedReader input = new BufferedReader(new InputStreamReader(is));
        String line = null;
        boolean found1 = false;
        boolean found2 = false;

        while ((line = input.readLine()) != null) {
            if (line.equals("${4+3}")) {
                found1 = true;
            } else if (line.equals("${2+2}")) {
                found2 = true;
	    }
        }
	
        if (found1 && found2) {
            stat.addStatus(TEST_NAME, stat.PASS);
        } else {
            System.err.println("Wrong response body");
            stat.addStatus(TEST_NAME, stat.FAIL);
        }
    }
}
