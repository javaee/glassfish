import java.io.*;
import java.net.*;
import com.sun.ejte.ccl.reporter.*;

/*
 * Unit test for 6328909
 */
public class WebTest {

    private static SimpleReporterAdapter stat
        = new SimpleReporterAdapter("appserv-tests");
    private static final String TEST_NAME = "defaultContentType";

    private String host;
    private String port;
    private String contextRoot;

    public WebTest(String[] args) {
        host = args[0];
        port = args[1];
        contextRoot = args[2];
    }
    
    public static void main(String[] args) {
        stat.addDescription("defaultContentType");
        WebTest webTest = new WebTest(args);
        webTest.doTest();
        stat.printSummary(TEST_NAME);
    }

    public void doTest() {     
        try { 
            invoke();
        } catch (Exception ex) {
            System.out.println(TEST_NAME + " test failed.");
            stat.addStatus(TEST_NAME, stat.FAIL);
            ex.printStackTrace();
        }
    }

    private void invoke() throws Exception {
         
        Socket sock = new Socket(host, new Integer(port).intValue());
        sock.setSoTimeout(5000);
        OutputStream os = sock.getOutputStream();
        String get = "GET /test.xyz HTTP/1.1\n";
        System.out.println(get);
        os.write(get.getBytes());
        os.write("Host: localhost\n".getBytes());
        os.write("\n".getBytes());
        
        InputStream is = sock.getInputStream();
        BufferedReader bis = new BufferedReader(new InputStreamReader(is));

        boolean found = false;
        String line = null;
        int i =0;
        try{
            while ((line = bis.readLine()) != null) {
                System.out.println(i++ + ": " + line);
                if ("Content-Type: text/plain; charset=iso-8859-1".equals(line)){
                    found = true;
                }
                if (found) {
                    break;
                }
            }
        }catch (SocketTimeoutException t){
            ;
        }
        sock.close();

        if (found) {
            stat.addStatus(TEST_NAME, stat.PASS);
        } else {
            stat.addStatus(TEST_NAME, stat.FAIL);
        }
    }
}
