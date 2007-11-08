import java.io.*;
import java.net.*;
import com.sun.ejte.ccl.reporter.*;

/*
 * Test case for 4944160 ("InvokerServlet must be disabled in
 * default-web.xml"). This test case makes sure that Tomcat's InvokerServlet,
 * which is declared in the appserver domain's default-web.xml, has been
 * disabled.
 *
 * This client attempts to connect to this URL:
 * 
 *   http://<host>:<port>/web-invoker-servlet-disabled/servlet/TestServlet
 *
 * which must result in a 404, because the test servlet is mapped to this 
 * url-pattern in web.xml: /TestServlet, instead of /servlet/TestServlet.
 *
 * The client will be able to connect successfully to the above URL only if the
 * InvokerServlet has been enabled.
 */
public class WebTest {

    private static SimpleReporterAdapter stat
        = new SimpleReporterAdapter("appserv-tests");

    private String host;
    private String port;
    private String contextRoot;
    private boolean fail;

    public WebTest(String[] args) {
        host = args[0];
        port = args[1];
        contextRoot = args[2];
    }
    
    public static void main(String[] args) {
        stat.addDescription("Unit test for Bugtraq 4944160");
        WebTest webTest = new WebTest(args);
        webTest.doTest();
        stat.printSummary("invoker-servlet-disabled");
    }

    public void doTest() {
     
        URL url = null;
        HttpURLConnection conn = null;
        int responseCode;
        boolean fail = false;

        try { 
            /*
             * Connect to the wrong mapping.
             *
             * This will work only if the InvokerServlet in default-web.xml
             * has been enabled, and therefore must fail (with a 404 response
             * code) since the InvokerServlet should not have been enabled.
             */ 
            url = new URL("http://" + host  + ":" + port + contextRoot
                    + "/servlet/TestServlet");
            System.out.println("Connecting to: " + url.toString());
            conn = (HttpURLConnection) url.openConnection();
            conn.connect();
            responseCode = conn.getResponseCode();
            System.out.println("Response code: " + responseCode);
            if (responseCode != 404){
                fail = true;
            }

            /*
             * Connect to the correct mapping, as specified in the deployment
             * descriptor. This must work.
             */
            url = new URL("http://" + host  + ":" + port + contextRoot
                    + "/TestServlet");
            System.out.println("Connecting to: " + url.toString());
            conn = (HttpURLConnection) url.openConnection();
            conn.connect();
            responseCode = conn.getResponseCode();
            System.out.println("Response code: " + responseCode);
            if (responseCode == 404){
                fail = true;
            }

            if (fail) {
                stat.addStatus("invoker-servlet-disabled", stat.FAIL);
            } else {
                stat.addStatus("invoker-servlet-disabled", stat.PASS);
            }

        } catch (Exception ex) {
            System.out.println("invoker-servlet-disabled test failed.");
            stat.addStatus("invoker-servlet-disabled", stat.FAIL);
            ex.printStackTrace();
        }
    }
}
