import java.lang.*;
import java.io.*;
import java.net.*;

import com.sun.ejte.ccl.reporter.*;

/**
 * Bug 6193728: Multibyte value not processed correctly when request is
 * forwarded to another servlet
 */
public class WebTest {
    
    private static int count = 0;
    private static int EXPECTED_COUNT = 2;
    
    static SimpleReporterAdapter stat=
        new SimpleReporterAdapter("appserv-tests");

    public static void main(String args[]) {
        stat.addDescription("i18 multi byte value test");

        String host = args[0];
        String portS = args[1];
        String contextRoot = args[2];

        int port = new Integer(portS).intValue();
        String name;
        
        try {
            run(host, port, contextRoot + "/ServletTest" );
            
            if (count != EXPECTED_COUNT){
                stat.addStatus("multiByteValue POST", stat.FAIL);
            }           
        } catch (Throwable t) {
            stat.addStatus("multiByteValue", stat.FAIL);
        }

        stat.printSummary("web/multiByteValue---> expect 2 PASS");
    }

    private static void run(String host, int port, String contextPath)
         throws Exception {

        /*
         * Send a GET request
         */
        Socket s = new Socket(host, port);
        OutputStream os = s.getOutputStream();
        System.out.println(("GET " + contextPath + "?j_encoding=Shift_JIS" 
                                                            + " HTTP/1.0\n"));
        os.write(("GET " + contextPath + "?j_encoding=Shift_JIS" 
                                                + " HTTP/1.0\n").getBytes());
        os.write("\n".getBytes());
        
        InputStream is = s.getInputStream();
        BufferedReader bis = new BufferedReader(new InputStreamReader(is));
        String line = null;

        int index, lineNum=0;
        while ((line = bis.readLine()) != null) {
            index = line.indexOf("::");
            System.out.println(lineNum + ":  " + line);
            if (index != -1) {
                String status = line.substring(index+2);
                
                if (status.equalsIgnoreCase("PASS")){
                    stat.addStatus("web-multibyteValue GET: " 
                                        + line.substring(0,index), stat.PASS);
                } else {
                    stat.addStatus("web-multibyteValue GET: " 
                                        + line.substring(0,index), stat.FAIL);                       
                }
                count++;
            } 
            lineNum++;
        }

        /*
         * Send a POST request
         */
        // Construct body
        String body = URLEncoder.encode("j_encoding", "UTF-8")
            + "=" + URLEncoder.encode("Shift_JIS", "UTF-8");
    
        // Create a socket to the host
        s = new Socket(host, port);
        os = s.getOutputStream();
    
        // Send header
        BufferedWriter wr = new BufferedWriter(new OutputStreamWriter(
            s.getOutputStream(), "UTF8"));
        wr.write("POST " + contextPath + " HTTP/1.0\r\n");
        wr.write("Content-Length: " + body.length() + "\r\n");
        wr.write("Content-Type: application/x-www-form-urlencoded\r\n");
        wr.write("\r\n");
    
        // Send body
        wr.write(body);
        wr.flush();

        // Read response
        is = s.getInputStream();
        bis = new BufferedReader(new InputStreamReader(is));
        line = null;

        index=lineNum=0;
        while ((line = bis.readLine()) != null) {
            index = line.indexOf("::");
            System.out.println(lineNum + ":  " + line);
            if (index != -1) {
                String status = line.substring(index+2);
                
                if (status.equalsIgnoreCase("PASS")){
                    stat.addStatus("web-multibyteValue POST: " 
                                        + line.substring(0,index), stat.PASS);
                } else {
                    stat.addStatus("web-multibyteValue POST: " 
                                        + line.substring(0,index), stat.FAIL);                       
                }
                count++;
            } 
            lineNum++;
        }
   }
  
}
