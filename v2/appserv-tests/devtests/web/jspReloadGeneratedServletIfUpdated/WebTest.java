import java.io.*;
import java.net.*;
import com.sun.ejte.ccl.reporter.*;

/*
 * Unit test to ensure that servlet that was generated from JSP is reloaded
 * when its class file is updated (without the JSP getting recompiled).
 *
 * This unit test accesses a JSP, which causes the JSP to be compiled to a
 * servlet, and the servlet class to be loaded. The unit test checks the
 * servlet response and makes sure it matches the expected output ("This is
 * my output").
 *
 * The test then updates the servlet class file (without recompiling the
 * JSP!). The updated servlet class file generates a different output ("This
 * is my UPDATED output") than the original class file. The unit test checks
 * that the servlet is reloaded (by comparing its output against the expected
 * modified output) the next time the JSP is accessed.
 */
public class WebTest {

    private static String TEST_NAME;

    private static final String TEST_ROOT_NAME
        = "jsp-reload-generated-servlet-if-updated";

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

        stat.addDescription("Unit test to ensure that servlet that "
                            + "was generated from JSP is reloaded when its "
                            + "class file is updated");
        WebTest webTest = new WebTest(args);

        if ("first".equals(webTest.run)) {
            TEST_NAME = TEST_ROOT_NAME + "-first";
        } else {
            TEST_NAME = TEST_ROOT_NAME + "-second";
        }

        try {
            webTest.doTest();
        } catch (Exception ex) {
            ex.printStackTrace();
            stat.addStatus(TEST_NAME, stat.FAIL);
        }

	stat.printSummary();
    }

    public void doTest() throws Exception {

        InputStream is = null;
        BufferedReader input = null;
        try {
            String expectedResponse = null;
            if ("first".equals(run)) {
                expectedResponse = "This is my output";
            } else {
                expectedResponse = "This is my UPDATED output";
            }

            URL url = new URL("http://" + host  + ":" + port
                              + contextRoot + "/jsp/test.jsp");
            System.out.println("Connecting to: " + url.toString());
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.connect();
            int responseCode = conn.getResponseCode();

            if (responseCode != 200) {
                System.err.println("Unexpected return code: " + responseCode);
                stat.addStatus(TEST_NAME, stat.FAIL);
            } else {
                is = conn.getInputStream();
                input = new BufferedReader(new InputStreamReader(is));
                String line = input.readLine();
                if (expectedResponse.equals(line)) {
                    stat.addStatus(TEST_NAME, stat.PASS);
                } else {
                    System.err.println("Wrong response. Expected: " +
                                       expectedResponse + ", received: " + line);
                    stat.addStatus(TEST_NAME, stat.FAIL);
                }
            }
        } finally {
            try {
                if (is != null) is.close();
            } catch (IOException ex) {}
            try {
                if (input != null) input.close();
            } catch (IOException ex) {}
        }
    }
}
