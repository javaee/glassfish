import java.io.*;
import java.net.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import com.sun.ejte.ccl.reporter.*;

/**
 * Unit test for:
 *
 *  ("XSS for HttpServletResponse.sendError()")
 *
 */
public class WebTest {

    private static SimpleReporterAdapter stat
        = new SimpleReporterAdapter("appserv-tests");

    private static final String TEST_NAME
        = "http-response-error-message";

    private static final Pattern PATTERN = Pattern.compile("http/\\d\\.\\d 403 .*Hi, there.*", Pattern.CASE_INSENSITIVE);

    private String host;
    private String port;
    private String contextRoot;

    public WebTest(String[] args) {
        host = args[0];
        port = args[1];
        contextRoot = args[2];
    }
    
    public static void main(String[] args) {
        stat.addDescription("Unit test for XSS HttpServletResponse.sendError");
        WebTest webTest = new WebTest(args);
        webTest.doTest();
        stat.printSummary(TEST_NAME);
    }

    public void doTest() {     
        try { 
            invoke();
        } catch (Exception ex) {
            stat.addStatus(TEST_NAME, stat.FAIL);
            ex.printStackTrace();
        }
    }

    private void invoke() throws Exception {

        System.out.println("Host=" + host + ", port=" + port);        
        Socket sock = new Socket(host, new Integer(port).intValue());
        OutputStream os = sock.getOutputStream();
        String get = "GET " + contextRoot + "/index.jsp HTTP/1.1\n";
        System.out.println(get);
        os.write(get.getBytes());
        os.write("Host: localhost\n".getBytes());
        os.write("Connection: close\n".getBytes());
        os.write("\n".getBytes());

        InputStream is = null;
        BufferedReader bis = null;
        boolean isExpected = false;

        try {
            is = sock.getInputStream();
            bis = new BufferedReader(new InputStreamReader(is));
            String line = null;
            while ((line = bis.readLine()) != null) {
                System.out.println(line);
                Matcher m = PATTERN.matcher(line);
                if (m.matches()) {
                    isExpected = true;
                    break;
                }
            }
        } finally {
            try {
                if (is != null) {
                    is.close();
                }
            } catch (IOException ioe) {
                // ignore
            }
            try {
                if (bis != null) {
                    bis.close();
                }
            } catch (IOException ioe) {
                // ignore
            }
        }

        if (isExpected) {
            stat.addStatus(TEST_NAME, stat.PASS);
        } else {
            stat.addStatus(TEST_NAME, stat.FAIL);
            System.err.println("Missing expected response: " + PATTERN.toString());
        }
    }
}
