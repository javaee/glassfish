import java.io.*;
import java.net.*;
import com.sun.ejte.ccl.reporter.*;

/*
 * This unit test tests consistency of URL encoding and decoding between
 * sendDirect and jsp:forward.
 *
 */
public class WebTest {

    private static SimpleReporterAdapter stat
        = new SimpleReporterAdapter("appserv-tests");
    private static final String TEST_NAME = "web-getRequestURI";

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
        stat.addDescription("Unit test for Bugtraq 4894654");
        WebTest webTest = new WebTest(args);
        webTest.doTest();
        stat.printSummary(TEST_NAME);
    }

    public void doTest() {
     
        try { 
            invokeJSP();
        } catch (Exception ex) {
            System.out.println(TEST_NAME + " test failed");
            stat.addStatus(TEST_NAME, stat.FAIL);
            ex.printStackTrace();
        }

        if (!fail) {
            stat.addStatus(TEST_NAME, stat.PASS);
        }

        return;
    }

    private void invokeJSP() throws Exception {
         
        Socket sock = new Socket(host, new Integer(port).intValue());
        OutputStream os = sock.getOutputStream();
        String get = "GET " + contextRoot + "/jsp/main.jsp" + " HTTP/1.0\n";
        System.out.println(get);
        os.write(get.getBytes());
        os.write("\n".getBytes());
        
        InputStream is = sock.getInputStream();
        BufferedReader bis = new BufferedReader(new InputStreamReader(is));

        String line = null;
        int j=0;
        while ((line = bis.readLine()) != null) {
            System.out.println(j++ + ": " + line);
            if (line.toLowerCase().startsWith("location:")) {
            break;
            }
        }

        String param1 = null;
        String param2 = null;

        if (line != null) {
            System.out.println(line);
            int i = line.indexOf("/jsp/first.jsp");
            line = line.substring(i);
            System.out.println(line);
        
            Socket sock2 = new Socket(host, new Integer(port).intValue());
            os = sock2.getOutputStream();
            get = "GET "+ contextRoot + line + " HTTP/1.0\n";
            System.out.println(get);
            os.write(get.getBytes());
            os.write("\n".getBytes());
            is = sock2.getInputStream();
            bis = new BufferedReader(new InputStreamReader(is));

            while ((line = bis.readLine()) != null) {
               i = line.indexOf("iPlanetDirectoryPro=");
               if (i > 0) { 
                   param1 = line.substring(i);
                   System.out.println("First param is " + param1);
                   if ((line = bis.readLine()) != null) {
                       i = line.indexOf("iPlanetDirectoryPro=");
                       if (i > 0) {
                           param2 = line.substring(i);
                           System.out.println("Second param is " + param2);
                       } 
                   }
               }
            }
        } else {
            fail = true;
            stat.addStatus("Missing Location response header", stat.FAIL);
        }


        // Check the parameters for consistent decoding
        if ((param1 != null) && (param1.equals(param2))) {
            fail = false;
        } else {
           stat.addStatus("Parameters encoding/decoding is not consistent",
                           stat.FAIL); 
           fail = true;
        }
    }

}
