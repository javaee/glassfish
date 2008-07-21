import java.io.*;
import java.net.*;
import com.sun.ejte.ccl.reporter.*;

/**
 * Unit test for:
 *
 *  https://issues.apache.org/bugzilla/show_bug.cgi?id=42727
 *  ("readLine with max lenght")
 */
public class WebTest {

    private static SimpleReporterAdapter stat
        = new SimpleReporterAdapter("appserv-tests");

    private static final String TEST_NAME
        = "readLine-with-max-length";

    private String host;
    private String port;
    private String contextRoot;
    private int length;

    public WebTest(String[] args) {
        host = args[0];
        port = args[1];
        contextRoot = args[2];
        length = Integer.parseInt(args[3]);
    }
    
    public static void main(String[] args) {
        stat.addDescription("Unit test for readLine with max length");
        WebTest webTest = new WebTest(args);
        webTest.doTest();
        stat.printSummary(TEST_NAME);
    }

    public void doTest() {
        try {
            boolean status = invoke();

            if (status) {
                stat.addStatus(TEST_NAME, stat.PASS);
            } else {
                System.err.println("Missing expected response: " + length + " A");
                stat.addStatus(TEST_NAME, stat.FAIL);
            }
        } catch (Exception ex) {
            stat.addStatus(TEST_NAME, stat.FAIL);
            ex.printStackTrace();
        }
    }

    private boolean invoke() throws Exception {
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < length; i++) {
            sb.append("A");
        }
        String data = sb.toString();
        Socket sock = new Socket(host, new Integer(port).intValue());
        OutputStream os = sock.getOutputStream();

        StringBuffer post = new StringBuffer();
        post.append("POST " + contextRoot + "/echo HTTP/1.1\r\n");
        post.append("Host: localhost\r\n");
        post.append("Connection: close\r\n");
        post.append("Content-Length: " + data.length() + "\r\n\r\n");

        System.out.println(post);
        os.write(post.toString().getBytes());
        os.write(data.getBytes());

        InputStream is = sock.getInputStream();
        BufferedReader bis = new BufferedReader(new InputStreamReader(is));

        String line = null;
        boolean hasExpectedResponse = false;
        while ((line = bis.readLine()) != null) {
            System.out.println(line);
            if (line.equals(data)) {
                hasExpectedResponse = true;
                break;
            } else {
                System.out.println("line with length = " + line.length());
            }
        }
        bis.close();
        is.close();
        os.close();
        sock.close();

        return hasExpectedResponse;
    }

}
