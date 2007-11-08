
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
      
        stat.addDescription("Double content-length header");

        String host = args[0];
        String portS = args[1];
        String contextRoot = args[2];

        int port = new Integer(portS).intValue();
        String name;
        
        try {
            goGet(host, port, "EXPIRED", contextRoot + "/ServletTest" );
            
        } catch (Throwable t) {
        } finally{
            if (count != EXPECTED_COUNT){
                stat.addStatus("web-doubleContentLength", stat.FAIL);
            }           
        }

        stat.printSummary("web/doubleContentLength---> expect 2 PASS");
    }

    private static void goGet(String host, int port,
                              String result, String contextPath)
         throws Exception {
        Socket s = new Socket(host, port);
        OutputStream os = s.getOutputStream();

        System.out.println(("POST " + contextPath + " HTTP/1.1\n"));
        try {
            os.write(("POST " + contextPath + " HTTP/1.1\n").getBytes());
            os.write(("Host: localhost\r").getBytes());
            os.write("content-length: 0\r".getBytes());
            os.write("content-length: 10\r".getBytes());
            os.write("content-type: application/x-www-form-urlencoded\r".getBytes());
            os.write("\n\r\n".getBytes());
            os.write("a\r\n".getBytes());
        } catch (Exception e) {
            // Web server sometimes sends the response with 400 status
            // code right after the second content-length header in which
            // case next os.write will cause an exception. 
        }

        InputStream is = s.getInputStream();
        BufferedReader bis = new BufferedReader(new InputStreamReader(is));
        String line = null;

        try{
            int index, lineNum=0;
            while ((line = bis.readLine()) != null) {
                index = line.indexOf("400");
                System.out.println(lineNum + ":  " + line);
                if (index != -1) {
                    stat.addStatus("web-doubleContentLength", stat.PASS);
                    count++;
                    break;
                } 
                lineNum++;
            }
        } catch( Exception ex){
         }
   }
  
}
