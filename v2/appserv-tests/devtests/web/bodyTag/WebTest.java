import java.lang.*;
import java.io.*;
import java.net.*;

import com.sun.ejte.ccl.reporter.*;

public class WebTest
{
    
    private static boolean pass = false;
    
    static SimpleReporterAdapter stat=
        new SimpleReporterAdapter("appserv-tests");

    public static void main(String args[])
    {

        // The stat reporter writes out the test info and results
        // into the top-level quicklook directory during a run.
      
        stat.addDescription("Test BodyTag behavior when tag is reused");

        String host = args[0];
        String portS = args[1];
        String contextRoot = args[2];
        int port = new Integer(portS).intValue();
        
        try {
            goGet(host, port, contextRoot + "/test.jsp" );
            stat.addStatus("BodyTag test", pass? stat.PASS: stat.FAIL);
        } catch (Throwable t) {
            System.out.println(t.getMessage());
            stat.addStatus("Test UNPREDICTED-FAILURE", stat.FAIL);
        }

        stat.printSummary("BodyTag Reuse");
    }

    private static void goGet(String host, int port, String contextPath)
         throws Exception
    {
        Socket s = new Socket(host, port);
        OutputStream os = s.getOutputStream();

        System.out.println("GET " + contextPath + " HTTP/1.0");
        os.write(("GET " + contextPath + " HTTP/1.0\n").getBytes());
        os.write("\n".getBytes());
        
        InputStream is = s.getInputStream();
        BufferedReader bis = new BufferedReader(new InputStreamReader(is));
        String line = null;

        int count = 0;
        try {
            while ((line = bis.readLine()) != null) {
                if (line.trim().length() > 0)
                    System.out.println(line);
                if (line.indexOf("xxBodyTagxx") >= 0)
                    count++;
            }
        } catch( Exception ex){
            ex.printStackTrace();   
            throw new Exception("Test UNPREDICTED-FAILURE");
        }
        if (count == 1)
             pass = true;
    }
}
