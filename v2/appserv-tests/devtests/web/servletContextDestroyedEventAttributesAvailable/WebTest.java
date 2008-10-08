import java.io.*;
import java.net.*;
import com.sun.ejte.ccl.reporter.*;

/*
 * Unit test for:
 *
 *   https://glassfish.dev.java.net/issues/show_bug.cgi?id=6442
 *   ("IllegalStateException: No WebApplicationContext found")
 *
 * Make sure a ServletContextListener is called at its contextDestroyed()
 * method before any ServletContext attributes are cleared
 */
public class WebTest {

    private static final String TEST_NAME = "servlet-context-destroyed-event-attributes-available";

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

        stat.addDescription("Unit test for Issue 6442");
        WebTest webTest = new WebTest(args);

        try {
            if ("firstRun".equals(webTest.run)) {
                webTest.firstRun();
            } else {
                webTest.secondRun();
            }
            stat.addStatus(TEST_NAME, stat.PASS);
        } catch (Exception ex) {
            ex.printStackTrace();
            stat.addStatus(TEST_NAME, stat.FAIL);
        }

	stat.printSummary();
    }

    /**
     * Access servlet that adds ServletContext attribute.
     * As the app is undeployed, its ServletContextListener will be invoked
     * at its contextDestroyed() method and will attempt to access the
     * ServletContext attribute that was added by the servlet.
     * If the attribute is present, the ServletContextListener will write
     * the word "SUCCESS" to /tmp/mytest. Otherwise, it will write FAIL to 
     * that file.
     */
    public void firstRun() throws Exception {
        URL url = new URL("http://" + host  + ":" + port + contextRoot +
                          "/TestServlet");
        System.out.println("Connecting to: " + url.toString());
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setInstanceFollowRedirects(false);
        conn.connect();

        int responseCode = conn.getResponseCode();
        if (responseCode != HttpURLConnection.HTTP_OK) {
            throw new Exception("Unexpected response code: Got " +
                                responseCode + ", expected: " +
                                HttpURLConnection.HTTP_OK);
        }
    }

    /**
     * Read the contents of /tmp/mytest
     */
    public void secondRun() throws Exception {

        File inFile = new File("/tmp/mytest");
        FileInputStream fis = new FileInputStream(inFile);
        BufferedReader br = new BufferedReader(new InputStreamReader(fis));
        String line = br.readLine();
        inFile.delete();

        if (!line.equals("SUCCESS")) {
            throw new Exception("No success");
        }
    }
}
