
import java.lang.*;
import java.io.*;
import java.net.*;

import com.sun.ejte.ccl.reporter.*;

public class WebTest
{
    
    private static int count = 0;
    private static int EXPECTED_COUNT = 1;
    
    static SimpleReporterAdapter stat=
        new SimpleReporterAdapter("appserv-tests");

    public static void main(String args[])
    {

        // The stat reporter writes out the test info and results
        // into the top-level quicklook directory during a run.
      
        stat.addDescription("Test for getLocalName() getLocalPort Servlet 2.4 new Features");


        String host = args[0];
        String portS = args[1];
        String contextRoot = args[2];

        int port = new Integer(portS).intValue();
        String name;
        
        try {
            goGet(host, port, "getLocalPort", contextRoot + "/ServletTest" );
            
            if (count != EXPECTED_COUNT){
                stat.addStatus("localPort getLocalPort UNPREDICTED-FAILURE", stat.FAIL);
            }           
        } catch (Throwable t) {
            System.out.println(t.getMessage());
            stat.addStatus("localPort getLocalPort UNPREDICTED-FAILURE", stat.FAIL);
        }

        stat.printSummary("web/localPort---> expect 3 PASS");
    }

    private static void goGet(String host, int port,
                              String result, String contextPath)
         throws Exception
    {
        System.out.println("*** This test will fail when using a proxy. ****");
        long time = System.currentTimeMillis();
        Socket s = new Socket(host, port);
        OutputStream os = s.getOutputStream();

        System.out.println(("GET " + contextPath + " HTTP/1.1\n"));
        os.write(("GET " + contextPath + " HTTP/1.1\n").getBytes());
        os.write("Host: localhost\n".getBytes());
        os.write("\n".getBytes());
        
        InputStream is = s.getInputStream();
        System.out.println("Time: " + (System.currentTimeMillis() - time));
        BufferedReader bis = new BufferedReader(new InputStreamReader(is));
        String line = null;

        try{
            int index;
            int i = 0;
            while ((line = bis.readLine()) != null) {
                index = line.indexOf(result);
                System.out.println(i++ + ": " + line);
                if (index != -1) {
                    index = line.indexOf(":");
                    String status = line.substring(index+1);
                    
                    if (status.equalsIgnoreCase(String.valueOf(port))){
                        stat.addStatus("web-localPort: " + line.substring(0,index), stat.PASS);
                    } else {
                        stat.addStatus("web-localPort: " + line.substring(0,index), stat.FAIL);                       
                    }
                    count++;
                } 
            }
        } catch( Exception ex){
            ex.printStackTrace();   
            throw new Exception("localPort getLocalPort UNPREDICTED-FAILURE");
         }
   }
  
}
