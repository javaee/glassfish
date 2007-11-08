
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
      
        stat.addDescription("Multi Servlet Requests");

        String host = args[0];
        String portS = args[1];
        String contextRoot = args[2];

        int port = new Integer(portS).intValue();
        String name;
        
        try {
            goGet(host, port, "multiServletRequests", contextRoot + "/ServletTest" );
        } catch (Throwable t) {
            System.out.println(t.getMessage());
        }

        if (count != EXPECTED_COUNT){
            stat.addStatus("web-multiServletRequests", stat.FAIL);
        }           
        stat.printSummary("web/multiServletRequests---> expect " + EXPECTED_COUNT);
    }

    private static void goGet(String host, int port,
                              String result, String contextPath)
         throws Exception
    {
        long time = System.currentTimeMillis();
        Socket s = new Socket(host, port);
        OutputStream os = s.getOutputStream();

        contextPath += "?host=" + host + "&port=" + port + "&contextRoot=" + contextPath;
        System.out.println(("GET " + contextPath + " HTTP/1.1\n"));
        os.write(("GET " + contextPath + " HTTP/1.1\n").getBytes());
        os.write(("Host: localhost\n").getBytes());
        os.write("\n".getBytes());
        
        InputStream is = s.getInputStream();
        System.out.println("Time: " + (System.currentTimeMillis() - time));
        BufferedReader bis = new BufferedReader(new InputStreamReader(is));
        String line = null;

        try{
            int index;
            int j=0;
            while ((line = bis.readLine()) != null) {
                index = line.indexOf(result);
                System.out.println(j + ":" + line);
                if (index != -1) {
                    index = line.indexOf("::");
                    String status = line.substring(index+2);
                    
                    System.out.println("=== status: " + status);
                    if (status.equalsIgnoreCase("PASS")){
                        stat.addStatus("web-multiServletRequests: " + line.substring(0,index), stat.PASS);
                    } else {
                        stat.addStatus("web-multiServletRequests: " + line.substring(0,index), stat.FAIL);                       
                    }
                    count++;
                } 
                j++;
            }
        } catch( Exception ex){
        }
   }
  
}
