import java.io.*;
import java.net.*;
import com.sun.ejte.ccl.reporter.*;

/*
 * Unit test Issue 646: Grizzly Header Index out of bounds
 */
public class WebTest {

    private static final String TEST_NAME = "headerBufferFull";

    private static SimpleReporterAdapter stat
        = new SimpleReporterAdapter("appserv-tests");

    private String host;
    private String port;
    private String contextRoot;

    public WebTest(String[] args) {
        host = args[0];
        port = args[1];
        contextRoot = args[2];
    }
    
    public static void main(String[] args) {

        stat.addDescription("headerBufferSize");
        WebTest webTest = new WebTest(args);

        try {
            webTest.doTest();
        } catch (Exception ex) {
            ex.printStackTrace();
            stat.addStatus(TEST_NAME, stat.FAIL);
        }

        stat.printSummary();
    }

    public void doTest() throws Exception {
        Socket sock = new Socket(host, new Integer(port).intValue());
        sock.setSoTimeout(5000);
        OutputStream os = sock.getOutputStream();
        String get = "GET " + contextRoot + "/TestServlet HTTP/1.1\n";
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
                //System.out.println(i++ + ": " + line);
                if ( line.toLowerCase().indexOf("header4095") != -1){
                    found = true;
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
