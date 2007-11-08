
import java.lang.*;
import java.io.*;
import java.net.*;

import com.sun.ejte.ccl.reporter.*;

public class WebTest
{
    
    private static int count = 0;
    private static int EXPECTED_COUNT = 3;
    
    static SimpleReporterAdapter stat=
        new SimpleReporterAdapter("appserv-tests");

    public static void main(String args[])
    {

        // The stat reporter writes out the test info and results
        // into the top-level quicklook directory during a run.
      
        stat.addDescription("Standalone Servlet/Filter war test");

        String host = args[0];
        String portS = args[1];
        String contextRoot = args[2];

        int port = new Integer(portS).intValue();
        String name;
        
        try {
            goGet(host, port, "FILTER", contextRoot + "/ServletTest" );
        } catch (Throwable t) {
            t.printStackTrace();
        }

        if (count != EXPECTED_COUNT){
            stat.addStatus("web-requestdispatcher", stat.FAIL);
        }           
        stat.printSummary("web-requestdispatcher");
    }

    private static void goGet(String host, int port,
                              String result, String contextPath) throws Exception {
        try{
            long time = System.currentTimeMillis();
            Socket s = new Socket(host, port);
            s.setSoTimeout(5000);
            OutputStream os = s.getOutputStream();

            System.out.println(("GET " + contextPath + " HTTP/1.1\n"));
            os.write(("GET " + contextPath + " HTTP/1.1\n").getBytes());
            os.write(("Host: localhost\n").getBytes());
            os.write("\n".getBytes());
            
            InputStream is = s.getInputStream();
            System.out.println("Time: " + (System.currentTimeMillis() - time));
            BufferedReader bis = new BufferedReader(new InputStreamReader(is));
            String line = null;

            int index;
            while ((line = bis.readLine()) != null) {
                index = line.indexOf(result);
                System.out.println(line);
                if (index != -1) {
                    index = line.indexOf(":");
                    String status = line.substring(index+1);
                    
                    if (status.equalsIgnoreCase("PASS")){
                        stat.addStatus("web-requestdispatcher: " + line.substring(0,index), stat.PASS);
                    } else {
                        stat.addStatus("web-requestdispatcher: " + line.substring(0,index), stat.FAIL);                       
                    }
                    count++;
                } 
            }
        } catch( Exception ex){
            ex.printStackTrace();
        }
   }
  
}
