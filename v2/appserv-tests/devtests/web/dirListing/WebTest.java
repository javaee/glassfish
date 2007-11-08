
import java.lang.*;
import java.io.*;
import java.net.*;

import com.sun.ejte.ccl.reporter.*;

/**
 * Bug 6172839
 */
public class WebTest {
    
    private static int count = 0;
    private static int EXPECTED_COUNT = 1;
    
    static SimpleReporterAdapter stat=
        new SimpleReporterAdapter("appserv-tests");

    public static void main(String args[]) {

        // The stat reporter writes out the test info and results
        // into the top-level quicklook directory during a run.
      
        stat.addDescription("Standalone Session Invalid war test");

        String host = args[0];
        String portS = args[1];
        String contextRoot = args[2];

        int port = new Integer(portS).intValue();
        String name;
        
        try {
            goGet(host, port, "", contextRoot + "/" );
            
            if (count != EXPECTED_COUNT){
                stat.addStatus("web-directoryListing", stat.FAIL);
            }           
        } catch (Throwable t) {
            t.printStackTrace();
            stat.addStatus("web-directoryListing", stat.FAIL);
        }

        stat.printSummary("web/directoryListing---> expect 1 PASS");
    }

    private static void goGet(String host, int port,
                              String result, String contextPath)
         throws Exception {
        boolean ok = false;
        try{
            Socket s = new Socket(host, port);
            OutputStream os = s.getOutputStream();

            System.out.println(("GET " + contextPath + " HTTP/1.0\n"));
            os.write(("GET " + contextPath + " HTTP/1.0\n").getBytes());
            os.write("Authorization: Basic ajJlZTpqMmVl\n".getBytes());
            os.write("\n".getBytes());
            
            InputStream is = s.getInputStream();
            BufferedReader bis = new BufferedReader(new InputStreamReader(is));
            String line = null;

            int index=0, lineNum=0;
            String cookies = "";
            while ((line = bis.readLine()) != null) {
                System.out.println(lineNum + ": " + line);
                if (line.indexOf("Directory Listing") != -1){
                    stat.addStatus("web-directoryListing", stat.PASS);
                    ok = true;
                    count++;
                    break;
                }
                lineNum++;
            }
        } catch( Exception ex){
            ex.printStackTrace();   
            throw new Exception("Test UNPREDICTED-FAILURE");
        }

        if (!ok){
            count++;
            stat.addStatus("web-directoryListing", stat.FAIL);
        }
   }
  
}
