
import java.lang.*;
import java.io.*;
import java.net.*;

import com.sun.ejte.ccl.reporter.*;

public class WebTest {
    
    private static int count = 0;
    private static int EXPECTED_COUNT = 1;
    
    static SimpleReporterAdapter stat=
        new SimpleReporterAdapter("appserv-tests");

    public static void main(String args[]) {

        // The stat reporter writes out the test info and results
        // into the top-level quicklook directory during a run.
        stat.addDescription("Badly query string character ';'");

        String host = args[0];
        String portS = args[1];
        String contextRoot = args[2];

        int port = new Integer(portS).intValue();
        String name;
        
        try {
            goGet(host, port, contextRoot + "/ServletTest" );
            
            if (count != EXPECTED_COUNT){
                stat.addStatus("malformedQueryString", stat.FAIL);
            }           
        } catch (Throwable t) {
            System.out.println(t.getMessage());
            stat.addStatus("malformedQueryString", stat.FAIL);
        }

        stat.printSummary("web/malformedQueryString---> expect 1 PASS");
    }

    private static void goGet(String host, 
                              int port, String contextPath) throws Exception {
        Socket s = new Socket(host, port);
        OutputStream os = s.getOutputStream();

        System.out.println(("GET " + contextPath + " HTTP/1.0\n"));
        os.write(("GET " + contextPath + " HTTP/1.0\n").getBytes());
        os.write("\n".getBytes());
        
        InputStream is = s.getInputStream();
        BufferedReader bis = new BufferedReader(new InputStreamReader(is));
        String line = null;

        try{
            int index, lineNum=0;
            while ((line = bis.readLine()) != null) {
                index = line.indexOf("::");
                System.out.println(lineNum + ":  " + line);
                if (index != -1) {
                    String status = line.substring(index+2);
                    
                    if (status.equalsIgnoreCase("PASS")){
                        stat.addStatus("web-malformedQueryString", stat.PASS);
                    } else {
                        stat.addStatus("web-malformedQueryString", stat.FAIL);                      }
                    count++;
                } 
                lineNum++;
            }
        } catch( Exception ex){
            ex.printStackTrace();   
            throw new Exception("Test UNPREDICTED-FAILURE");
        }
   }
  
}
