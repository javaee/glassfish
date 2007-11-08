
import java.lang.*;
import java.io.*;
import java.net.*;

import com.sun.ejte.ccl.reporter.*;

public class WebTest
{
    
    private static URLConnection conn = null;
    private static URL url;
    private static ObjectOutputStream objectWriter = null;
    private static ObjectInputStream objectReader = null;  
    private static int count = 0;
    private static int EXPECTED_COUNT = 3;
    
    static SimpleReporterAdapter stat=
        new SimpleReporterAdapter("appserv-tests");

    public static void main(String args[])
    {

        // The stat reporter writes out the test info and results
        // into the top-level quicklook directory during a run.
      
        stat.addDescription("Invoking HttpServlet.doPost");

        String host = args[0];
        String portS = args[1];
        String contextRoot = args[2];

        int port = new Integer(portS).intValue();
        String name;
        
        try {
            goGet(host, port, contextRoot + "/ServletTest" );
        } catch (Throwable t) {
            System.out.println(t.getMessage());
        }

        if (count != EXPECTED_COUNT){
            stat.addStatus("httpServletDoPost", stat.FAIL);
        }           
        stat.printSummary("web/httpServletDoPost---> expect " + EXPECTED_COUNT);
    }

    private static void goGet(String host, int port,
                              String contextPath)
         throws Exception
    {
        url = new URL("http://" + host  + ":" + port + contextPath);
        System.out.println("\n Invoking url: " + url.toString());
        conn = url.openConnection();
        if (conn instanceof HttpURLConnection) {
            HttpURLConnection urlConnection = (HttpURLConnection)conn;
            urlConnection.setDoOutput(true);

            DataOutputStream out = 
               new DataOutputStream(urlConnection.getOutputStream());
                                out.writeByte(1);

           int responseCode=  urlConnection.getResponseCode();
           System.out.println("responseCode: " + responseCode);
            
           if (urlConnection.getResponseCode() != 405){
                stat.addStatus("httpServletDoPost", stat.FAIL);
           } else {
                stat.addStatus("httpServletDoPost", stat.PASS);
           }
        }

        boolean mark = false;
        int i = 0;
        try{
            long time = System.currentTimeMillis();
            Socket s = new Socket(host, port);
            s.setSoTimeout(10000);
            OutputStream os = s.getOutputStream();

            System.out.println(("POST " + contextPath + " HTTP/1.1\n"));
            os.write(("POST " + contextPath + " HTTP/1.1\n").getBytes());
            os.write(("Host: localhost\r\n").getBytes());
            os.write("content-length: 0\r\n".getBytes());
            os.write("\r\n".getBytes());
            
            InputStream is = s.getInputStream();
            BufferedReader bis = new BufferedReader(new InputStreamReader(is));
            String line = null;

            int index = 0;
            while ((line = bis.readLine()) != null) {
                index = line.indexOf("httpServletDoPost");
                System.out.println(i++ + ": " + line);
                if (index != -1) {
                    index = line.indexOf("::");
                    String status = line.substring(index+1);
                    
                    if (status.equalsIgnoreCase("FAIL")){
                        stat.addStatus("httpServletDoPost-noCL", stat.FAIL);
                        mark = true;
                    }
                    count++;
                } 
            }
        } catch( Exception ex){
        } finally {
            if (!mark && i > 0)
               stat.addStatus("httpServletDoPost-noCL", stat.PASS);
        }

        try{
            long time = System.currentTimeMillis();
            Socket s = new Socket(host, port);
            s.setSoTimeout(10000);
            OutputStream os = s.getOutputStream();

            System.out.println(("POST " + contextPath + " HTTP/1.0\n"));
            os.write(("POST " + contextPath + " HTTP/1.0\n").getBytes());
            os.write(("Host: localhost\r\n").getBytes());
            os.write("content-length: 0\r\n".getBytes());
            os.write("\r\n".getBytes());
            
            InputStream is = s.getInputStream();
            BufferedReader bis = new BufferedReader(new InputStreamReader(is));
            String line = null;

            int index = 0;
            while ((line = bis.readLine()) != null) {
                index = line.indexOf("httpServletDoPost");
                System.out.println(i++ + ": " + line);
                if (index != -1) {
                    index = line.indexOf("::");
                    String status = line.substring(index+1);
                    
                    if (status.equalsIgnoreCase("FAIL")){
                        stat.addStatus("httpServletDoPost-noCL-http10", stat.FAIL);
                        mark = true;
                    }
                    count++;
                } 
            }
        } catch( Exception ex){
        } finally {
            if (!mark && i > 0)
               stat.addStatus("httpServletDoPost-noCL-http10", stat.PASS);
        }
         
   }
  
}
