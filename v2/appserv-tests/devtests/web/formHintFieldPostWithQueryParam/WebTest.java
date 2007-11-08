import java.lang.*;
import java.io.*;
import java.net.*;

import com.sun.ejte.ccl.reporter.*;

/**
 * Unit test for 6286450: AS cannot determine request charset using
 * form-hint-field if form action contains query parameters.
 */
public class WebTest {
    
    private static SimpleReporterAdapter stat
        = new SimpleReporterAdapter("appserv-tests");
    private static final String TEST_NAME
        = "form-hint-field-post-with-query-param";
    private static final String EXPECTED_RESPONSE = "Shift_JIS";

    private String host;
    private String port;
    private String contextRoot;

    public WebTest(String[] args) {
        host = args[0];
        port = args[1];
        contextRoot = args[2];
    }

    public static void main(String args[]) {
        stat.addDescription("Unit test for 6286450");
        WebTest webTest = new WebTest(args);
        webTest.doTest();
        stat.printSummary(TEST_NAME);
    }

    public void doTest() {     
        try { 
            run();
        } catch (Exception ex) {
            stat.addStatus(TEST_NAME, stat.FAIL);
            ex.printStackTrace();
        }
    }

    /*
     * Sends a POST request with a query param and body
     */
    private void run() throws Exception {

        // Construct body
        String body = URLEncoder.encode("j_encoding", "UTF-8")
            + "=" + URLEncoder.encode("Shift_JIS", "UTF-8");
    
        // Create a socket to the host
        Socket socket = new Socket(host, new Integer(port).intValue());
    
        // Send header
        BufferedWriter wr = new BufferedWriter(new OutputStreamWriter(
            socket.getOutputStream(), "UTF8"));
        wr.write("POST " + contextRoot + "/a.jsp?query1=value1"
                 + " HTTP/1.0\r\n");
        wr.write("Content-Length: " + body.length() + "\r\n");
        wr.write("Content-Type: application/x-www-form-urlencoded\r\n");
        wr.write("\r\n");
    
        // Send body
        wr.write(body);
        wr.flush();

        // Read response
        BufferedReader bis = new BufferedReader(
            new InputStreamReader(socket.getInputStream()));
        String line = null;
        String lastLine = null;
        while ((line = bis.readLine()) != null) {
            lastLine = line;
        }

        if (!EXPECTED_RESPONSE.equals(lastLine)) {
            System.err.println("Wrong response. Expected: "
                               + EXPECTED_RESPONSE
                               + ", received: " + lastLine);
            stat.addStatus(TEST_NAME, stat.FAIL);
        } else {
            stat.addStatus(TEST_NAME, stat.PASS);
        }
   }
  
}
