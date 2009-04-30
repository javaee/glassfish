
import java.lang.*;
import java.io.*;
import java.net.*;

import com.sun.ejte.ccl.reporter.*;
/**
 * BugId 4862098: ProgrammaticLogin fails for webcontainer - regression on AS8
 */
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
      
        stat.addDescription("Standalone Servlet/Filter war test");

        String host = args[0];
        String portS = args[1];
        String contextRoot = args[2];
        String user = args[3];
        String password = args[4];

        int port = new Integer(portS).intValue();
        String name;
        
        try {
            goGet(host, port, "WEB-Programmatic-Login", contextRoot + "/ServletTest?user=testuser3&password=secret" );
            
            if (count != EXPECTED_COUNT){
                stat.addStatus("Test UNPREDICTED-FAILURE", stat.FAIL);
            }           
        } catch (Throwable t) {
            System.out.println(t.getMessage());
            stat.addStatus("Test UNPREDICTED-FAILURE", stat.FAIL);
        }

        stat.printSummary("web/standalonewar---> expect 3 PASS");
    }

    private static void goGet(String host, int port,
                              String result, String contextPath)
         throws Exception
    {
        Socket s = null;
        OutputStream os = null;
        InputStream is = null;
        BufferedReader bis = null;
        try{
            s = new Socket(host, port);
            os = s.getOutputStream();

            System.out.println(("GET " + contextPath + " HTTP/1.0\n"));
            os.write(("GET " + contextPath + " HTTP/1.0\n").getBytes());
            os.write("\n".getBytes());
        
            is = s.getInputStream();
            bis = new BufferedReader(new InputStreamReader(is));
            String line = null;

            int index;
            while ((line = bis.readLine()) != null) {
                index = line.indexOf(result);
                System.out.println(line);
                if (index != -1) {
                    index = line.indexOf(":");
                    String status = line.substring(index+1);
                    
                    if (status.equalsIgnoreCase("PASS")){
                        stat.addStatus("web-programmatic-login: " + line.substring(0,index), stat.PASS);
                    } else {
                        stat.addStatus("web-programmatic-login: " + line.substring(0,index), stat.FAIL);                       
                    }
                    count++;
                } 
            }
        } catch( Exception ex){
            ex.printStackTrace();   
            throw new Exception("Test UNPREDICTED-FAILURE");
        } finally {
            try {
                if (s != null) {
                    s.close();
                }
            } catch(IOException ioe) {
                // ignore
            }
            try {
                if (os != null) {
                    os.close();
                }
            } catch(IOException ioe) {
                // ignore
            }
            try {
                if (is != null) {
                    is.close();
                }
            } catch(IOException ioe) {
                // ignore
            }
            try {
                if (bis != null) {
                    bis.close();
                }
            } catch(IOException ioe) {
                // ignore
            }
        }
   }
  
}
