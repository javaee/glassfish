
import java.lang.*;
import java.io.*;
import java.net.*;

import com.sun.ejte.ccl.reporter.*;
/**
 * Unit Test for 4931092: HttpSessionListener implementors are called to late according to Servlet Spec.
 */
public class WebTest {
    
    private static int count = 0;
    private static int EXPECTED_COUNT = 1;
    
    static SimpleReporterAdapter stat=
        new SimpleReporterAdapter("appserv-tests");

    public static void main(String args[]) {

        // The stat reporter writes out the test info and results
        // into the top-level quicklook directory during a run.
      
        stat.addDescription("Standalone Session Destroyed war test");

        String host = args[0];
        String portS = args[1];
        String contextRoot = args[2];

        int port = new Integer(portS).intValue();
        String name;
        
        try {
            goGet(host, port, "DESTROYED", contextRoot + "/ServletTest" );
            
            if (count != EXPECTED_COUNT){
                stat.addStatus("web-sessionDestroyed", stat.FAIL);
            }           
        } catch (Throwable t) {
            t.printStackTrace();
            stat.addStatus("web-sessionDestroyed", stat.FAIL);
        }

        stat.printSummary("web/sessionDestroyed---> expect 1 PASS");
    }

    private static void goGet(String host, int port,
                              String result, String contextPath)
         throws Exception {
        Socket s = new Socket(host, port);
        OutputStream os = s.getOutputStream();

        System.out.println(("GET " + contextPath + " HTTP/1.0\n"));
        os.write(("GET " + contextPath + " HTTP/1.0\n").getBytes());
        os.write("\n".getBytes());
        
        InputStream is = s.getInputStream();
        BufferedReader bis = new BufferedReader(new InputStreamReader(is));
        String line = null;

        try{
            int index;
            while ((line = bis.readLine()) != null) {
                index = line.indexOf(result);
                System.out.println(line);
                if (index != -1) {
                    index = line.indexOf(":");
                    String status = line.substring(index+1);
                    
                    if (status.equalsIgnoreCase("PASS")){
                        stat.addStatus("web-sessionDestroyed: " + line.substring(0,index), stat.PASS);
                    } else {
                        stat.addStatus("web-sessionDestroyed: " + line.substring(0,index), stat.FAIL);                       
                    }
                    count++;
                } 
            }
        } catch( Exception ex){
            ex.printStackTrace();   
            throw new Exception("Test UNPREDICTED-FAILURE");
         }
   }
  
}
