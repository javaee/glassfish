import java.io.*;
import java.net.*;
import com.sun.ejte.ccl.reporter.*;

/*
 * Unit test for Bugzilla 13499 ("Jasper throws an exception on an immediate
 * pageContext.forward()").
 *
 * If page output is unbuffered, IllegalStateException is no longer
 * thrown on forward if and only if nothing has been written to the page. The
 * IllegalStateException will still be thrown on forward if there has been any
 * unbuffered output (see JSP.5.5)
 */
public class WebTest {

    private static SimpleReporterAdapter stat =
        new SimpleReporterAdapter("appserv-tests");
    private static final String TEST_NAME = "jsp-forward-unbuffered";

    private String host;
    private String port;
    private String contextRoot;

    public WebTest(String[] args) {
        host = args[0];
        port = args[1];
        contextRoot = args[2];
    }
    
    public static void main(String[] args) {
        stat.addDescription("Unit test for Bugzilla 13499");
        WebTest webTest = new WebTest(args);
        try {
            webTest.doTest1();
            webTest.doTest2();
            stat.addStatus(TEST_NAME, stat.PASS);
        } catch (Exception ex) {
            stat.addStatus(TEST_NAME, stat.FAIL);
            ex.printStackTrace();
        }
	stat.printSummary();
    }

    public void doTest1() throws Exception {

        URL url = new URL("http://" + host  + ":" + port + contextRoot +
                          "/from1.jsp");
        System.out.println("Connecting to: " + url.toString());
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.connect();
        int responseCode = conn.getResponseCode();
        if (responseCode != 200) { 
            throw new Exception("Wrong response code. Expected: 200" +
                                ", received: " + responseCode);
	}
    }

    /*
     * This test expects from2.jsp to throw an IllegalStateException due to
     * the newline output as a result of its first line.
     */
    public void doTest2() throws Exception {

        URL url = new URL("http://" + host  + ":" + port + contextRoot +
                          "/from2.jsp");
        System.out.println("Connecting to: " + url.toString());
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.connect();
        int responseCode = conn.getResponseCode();
        if (responseCode != 500) { 
            throw new Exception("Wrong response code. Expected: 500" +
                                ", received: " + responseCode);
        }
    }

}
