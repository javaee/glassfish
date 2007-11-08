
import java.lang.*;
import java.io.*;
import java.net.*;

import com.sun.ejte.ccl.reporter.*;

public class WebTest extends Thread{
    
    private static int EXPECTED_COUNT = 1;

    public static int count;
    
    static SimpleReporterAdapter stat=
        new SimpleReporterAdapter("appserv-tests");

    public static void main(String args[]) {

        // The stat reporter writes out the test info and results
        // into the top-level quicklook directory during a run.
      
        stat.addDescription("Slow client bytes write");

        String host = args[0];
        int port = Integer.parseInt(args[1]);
        int num = Integer.parseInt(args[2]);
        
        try {
           for (int i=0; i < num; i++){
               new SlowClient(host,port,new WebTest()); 
           }
           Thread.sleep(60 * 3000);
        } catch (Throwable t) {
            t.printStackTrace();
        } 

        System.out.println("count: " + count);
        if ( count != num ) {
            stat.addStatus("dosSlowClient", stat.FAIL);
        } else {
            stat.addStatus("dosSlowClient", stat.PASS);
        }
        stat.printSummary("web/dosSlowClient");
    }

}
