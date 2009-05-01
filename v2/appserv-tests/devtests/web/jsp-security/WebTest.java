
import java.lang.*;
import java.io.*;
import java.net.*;

import com.sun.ejte.ccl.reporter.*;
/**
 * Fix for 4966609: [PE 8.0] isUserInRole returns true when jsp(s) i
 * is not defined in web.xml
 */
public class WebTest {
    
    private static int count = 0;
    private static int EXPECTED_COUNT = 1;
    
    static SimpleReporterAdapter stat=
        new SimpleReporterAdapter("appserv-tests");

    public static void main(String args[]) {

        stat.addDescription("JSR 115 Review 1 JSP security test");

        String host = args[0];
        String portS = args[1];
        String contextRoot = args[2];

        int port = new Integer(portS).intValue();
        String name;
        
        try {
            goGet(host, port, contextRoot + "/test.jsp" );
            
            if (count != EXPECTED_COUNT){
                stat.addStatus("Test UNPREDICTED-FAILURE", stat.FAIL);
            }           
        } catch (Throwable t) {
            System.out.println(t.getMessage());
            stat.addStatus("Test UNPREDICTED-FAILURE", stat.FAIL);
        }

        stat.printSummary("web/jsp-security---> expect " + EXPECTED_COUNT + " PASS");
    }

    private static void goGet(String host, int port,
                              String contextPath)
         throws Exception {
        Socket s = null;
        OutputStream os = null;
        InputStream is = null;
        BufferedReader bis = null;

        try{
            s = new Socket(host, port);
            os = s.getOutputStream();
            System.out.println(("GET " + contextPath + " HTTP/1.0\n"));
            os.write(("GET " + contextPath + " HTTP/1.0\n").getBytes());
            os.write("Authorization: Basic ajJlZTpqMmVl\n".getBytes());
            os.write("\n".getBytes());

            is = s.getInputStream();
            bis = new BufferedReader(new InputStreamReader(is));
            String line = null;

            int index, lineNum=0;
            while ((line = bis.readLine()) != null) {
                index = line.indexOf("::");
                System.out.println(lineNum + ":  " + line);
                if (index != -1) {
                    String status = line.substring(index+2);
                    
                    if (status.equalsIgnoreCase("PASS")){
                        stat.addStatus("web-jsp-security: " + line.substring(0,index), stat.PASS);
                    } else {
                        stat.addStatus("web-jsp-security: " + line.substring(0,index), stat.FAIL);                       
                    }
                    count++;
                } 
                lineNum++;
            }
        } catch( Exception ex){
            ex.printStackTrace();   
            throw new Exception("Test UNPREDICTED-FAILURE");
         } finally {
            try {
                if (os != null) os.close();
            } catch (IOException ex) {}
            try {
                if (s != null) s.close();
            } catch (IOException ex) {}
            try {
                if (is != null) is.close();
            } catch (IOException ex) {}
            try {
                if (bis != null) bis.close();
            } catch (IOException ex) {}
        }
   }
  
}
