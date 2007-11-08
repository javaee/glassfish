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
      
        stat.addDescription("Standalone keepAlive war test");

        String host = args[0];
        String portS = args[1];
        String contextRoot = args[2];

        int port = new Integer(portS).intValue();
        String name;
        
        try {
            goGet(host, port, "KeepAlive", contextRoot + "/test.jsp" );
            
        } catch (Throwable t) {
        } finally {
            if (count != EXPECTED_COUNT){
                stat.addStatus("web-keepAlive", stat.FAIL);
            }           
        }

        stat.printSummary("web/keepAlive ---> expect 1 PASS");
    }

    private static void goGet(String host, int port,
                              String result, String contextPath)
         throws Exception
    {
        Socket s = new Socket(host, port);
        OutputStream os = s.getOutputStream();

        System.out.println("GET " + contextPath + " HTTP/1.0");
        System.out.println("Connection: keep-alive");
        os.write(("GET " + contextPath + " HTTP/1.0\n").getBytes());
        os.write("Connection: keep-alive\n".getBytes());
        os.write("\n".getBytes());
        
        InputStream is = s.getInputStream();
        BufferedReader bis = new BufferedReader(new InputStreamReader(is));
        String line = null;

        int tripCount = 0;
        try {
            while ((line = bis.readLine()) != null) {
                System.out.println(line);
                int index = line.indexOf("Connection:");
                if (index >= 0) {
                    index = line.indexOf(":");
                    String state = line.substring(index+1).trim();
                    if (state.equalsIgnoreCase("keep-alive")) {
                        stat.addStatus("web-keepalive ", stat.PASS);
                        count++;
                    }
                } 
                if (line.indexOf("KeepAlive:end") >= 0) {
                    if (++tripCount == 1) {
                        System.out.println("GET " + contextPath + " HTTP/1.0");
                        os.write(("GET " + contextPath + " HTTP/1.0\n\n").getBytes());
                    }
                }
            }
        } catch( Exception ex){
            ex.printStackTrace();   
            throw new Exception("Test UNPREDICTED-FAILURE");
        }
    }
}
