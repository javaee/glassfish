import java.io.*;
import java.net.*;
import com.sun.ejte.ccl.reporter.*;

/*
 * This unit test expects only the relative path when redirect
 * as the following is specified in sun-web.xml. 
 *
 * <property name="relativeRedirectAllowed" value="true"/> 
 *
 */
public class WebTest {

    private static SimpleReporterAdapter stat
        = new SimpleReporterAdapter("appserv-tests");
    private static final String TEST_NAME = "web-relativeRedirectAllowed";

    /*
     * The relative path that is expected. 
     */
    private static final String PATH = "/web-relativeRedirectAllowed/jsp/test2.jsp";

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
        stat.addDescription("Unit test for Bugtraq 4642650");
        WebTest webTest = new WebTest(args);
        webTest.doTest();
        stat.printSummary(TEST_NAME);
    }

    public void doTest() {
     
        try { 
            invokeJsp();
        } catch (Exception ex) {
            stat.addStatus(TEST_NAME, stat.FAIL);
            ex.printStackTrace();
        }

        if (!fail) {
            stat.addStatus(TEST_NAME, stat.PASS);
        }

        return;
    }

    private void invokeJsp() throws Exception {
         
        Socket sock = new Socket(host, new Integer(port).intValue());
        OutputStream os = sock.getOutputStream();
        String get = "GET " + contextRoot + "/jsp/test1.jsp" + " HTTP/1.0\n";
        System.out.println(get);
        os.write(get.getBytes());
        os.write("\n".getBytes());
        
        InputStream is = sock.getInputStream();
        BufferedReader bis = new BufferedReader(new InputStreamReader(is));

        String line = null;
        while ((line = bis.readLine()) != null) {
            if (line.startsWith("Location:")) {
                break;
            }
        }

        if (line != null) {
            System.out.println(line);

            // Check the path
            if (line.startsWith("Location: "+PATH)) {
              fail = false;
            } else {
                System.err.println("Wrong path: " + line
                                   + ", expected: " + PATH); 
                stat.addStatus(TEST_NAME, stat.FAIL);
                fail = true;
            }
        } else {
            System.err.println("Missing Location response header");
            stat.addStatus(TEST_NAME, stat.FAIL);
        }
    }

}
