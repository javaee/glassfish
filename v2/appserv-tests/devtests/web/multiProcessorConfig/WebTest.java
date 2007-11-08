
import java.lang.*;
import java.io.*;
import java.net.*;

import com.sun.ejte.ccl.reporter.*;

/**
* Issue Issue 464: Multi-Processor configuration aren't applied
*/
public class WebTest {
    
    private static int count = 0;
    private static int EXPECTED_COUNT = 3;
    
    static SimpleReporterAdapter stat=
        new SimpleReporterAdapter("appserv-tests");

    public static void main(String args[])
    {

        // The stat reporter writes out the test info and results
        // into the top-level quicklook directory during a run.
      
        stat.addDescription("multiProcessorConfig");

        String host = args[0];
        String portS = args[1];
        String contextRoot = args[2];

        int port = new Integer(portS).intValue();
        String name;
        
        try {
            goGet(host, port, "FILTER", contextRoot + "/ServletTest" );
        } catch (Throwable t) {
            System.out.println(t.getMessage());
            stat.addStatus("multiProcessorConfig", stat.PASS); 
        }

        if (count != EXPECTED_COUNT){
            stat.addStatus("multiProcessorConfig", stat.FAIL);
        }           
        stat.printSummary("web/standalonewar---> expect " + EXPECTED_COUNT);
    }

    private static void goGet(String host, int port,
                              String result, String contextPath)
         throws Exception {
        long time = System.currentTimeMillis();
        Socket s = new Socket(host, port);
        OutputStream os = s.getOutputStream();
        s.setSoTimeout(10000);

        System.out.println(("GET " + contextPath + " HTTP/1.1\n"));
        os.write(("GET " + contextPath + " HTTP/1.1\n").getBytes());
        os.write(("Host: localhost\n").getBytes());
        os.write("\n".getBytes());
        
        InputStream is = s.getInputStream();
        System.out.println("Time: " + (System.currentTimeMillis() - time));
        BufferedReader bis = new BufferedReader(new InputStreamReader(is));
        String line = null;

        try{
            int index = 0;
            while ((line = bis.readLine()) != null) {
                index = line.indexOf(result);
                System.out.println(line);
                if (index != -1) {
                   stat.addStatus("multiProcessorConfig", stat.FAIL); 
                   break;
                } 
            }

            if ( index == -1 ){
                stat.addStatus("multiProcessorConfig", stat.PASS); 
            }
        } catch( Exception ex){
        }
   }
  
}
