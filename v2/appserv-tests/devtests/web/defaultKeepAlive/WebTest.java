
import java.lang.*;
import java.io.*;
import java.net.*;

import com.sun.ejte.ccl.reporter.*;

public class WebTest extends Thread{
    
    private static int count = 0;
    private static int EXPECTED_COUNT = 1;
    private static boolean executed = false;
    
    static SimpleReporterAdapter stat=
        new SimpleReporterAdapter("appserv-tests");

    public static void main(String args[]) {
        stat.addDescription("Default keep-alive test");

        String host = args[0];
        String portS = args[1];
        String contextRoot = args[2];

        int port = new Integer(portS).intValue();
        String name;

        new WebTest().start();

        try {
            goGet(host, port, "/" );
        } catch (Throwable t) {
            System.out.println(t.getMessage());
        }

        if (count != EXPECTED_COUNT){
            stat.addStatus("defaultKeepAlive", stat.FAIL);
        }           
        stat.printSummary("web/defaultKeepAlive---> expect " + EXPECTED_COUNT);
    }

    private static void goGet(String host, int port,
                              String contextPath) throws Exception {
        Socket s = new Socket(host, port);
        OutputStream os = s.getOutputStream();

        System.out.println(("GET " + contextPath + " HTTP/1.1\n"));
        os.write(("GET " + contextPath + " HTTP/1.1\n").getBytes());
        os.write(("Host: localhost\n").getBytes());
        os.write("\n".getBytes());
        
        InputStream is = s.getInputStream();
        BufferedReader bis = new BufferedReader(new InputStreamReader(is));
        String line = null;

        try{
            int index;
            while ((line = bis.readLine()) != null) {
                System.out.println(line);
            }
            if (executed){
                stat.addStatus("defaultKeepAlive", stat.PASS);
            }
        } catch( Exception ex){
            ex.printStackTrace();   
        }
   }
  
   public void run(){
       try{
           Thread.sleep(15000);
           System.out.println("OK keep-alive");
           executed = true;
       } catch (Exception ex){
           ex.printStackTrace();
       }
   }

}
