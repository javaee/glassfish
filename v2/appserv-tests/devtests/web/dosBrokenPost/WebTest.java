
import java.lang.*;
import java.io.*;
import java.net.*;

import com.sun.ejte.ccl.reporter.*;

public class WebTest extends Thread{
    
    private static int count = 0;
    private static int EXPECTED_COUNT = 1;
    private static boolean executed = false;
    
    static SimpleReporterAdapter stat=
        new SimpleReporterAdapter("appserv-tests");

    public static void main(String args[]) {

        // The stat reporter writes out the test info and results
        // into the top-level quicklook directory during a run.
      
        stat.addDescription("Double content-length header");

        String host = args[0];
        String portS = args[1];
        String contextRoot = args[2];

        int port = new Integer(portS).intValue();
        String name;
        
        try {
            goGet(host, port, contextRoot + "/ServletTest" );
            
        } catch (Throwable t) {
        } finally{
            if (count != EXPECTED_COUNT){
                stat.addStatus("dosBrokenPost", stat.FAIL);
            }           
        }

        stat.printSummary("web/dosBrokenPost---> expect 1 PASS");
    }

    private static void goGet(String host, int port,
                              String contextPath)
         throws Exception {
        Socket s = new Socket(host, port);
        OutputStream os = s.getOutputStream();

        System.out.println(("POST " + contextPath + " HTTP/1.1\n"));
        os.write(("POST " + contextPath + " HTTP/1.1\n").getBytes());
        os.write(("Host: localhost\r").getBytes());
        os.write("content-length: 0\r".getBytes());
        os.write("content-length: 10\r".getBytes());
        os.write("content-type: application/x-www-form-urlencoded\r".getBytes());
        os.write("a\r\n".getBytes());
        InputStream is = s.getInputStream();
        BufferedReader bis = new BufferedReader(new InputStreamReader(is));
        String line = null;

        try{
            int index;
            int i=0;
            while ((line = bis.readLine()) != null) {
                System.out.println(i + ":" + line);
                i++;
            }
            if (i == 0){
                stat.addStatus("dosBrokenPost", stat.PASS);
            }
        } catch( Exception ex){
            ex.printStackTrace();   
        }
   }
}
