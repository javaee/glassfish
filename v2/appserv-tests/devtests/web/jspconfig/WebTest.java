
import java.lang.*;
import java.io.*;
import java.net.*;

import com.sun.ejte.ccl.reporter.*;

public class WebTest
{
    
    private static int count = 0;
    private static int EXPECTED_COUNT = 5;
    private static boolean fail = false;
    
    static SimpleReporterAdapter stat=
        new SimpleReporterAdapter("appserv-tests");

    public static void main(String args[])
    {

        // The stat reporter writes out the test info and results
        // into the top-level quicklook directory during a run.
      
        stat.addDescription("Standalone jsp-config war test");

        String host = args[0];
        String portS = args[1];
        String contextRoot = args[2];

        int port = new Integer(portS).intValue();
        String name;
        
        try {
            goGet(host, port, "JSP-CONFIG", contextRoot + "/test.jsp" );
            goGet2(host, port, "ELIgnored", contextRoot + "/foo/test.jsp" );
            goGet2(host, port, "ELIgnored", contextRoot + "/foo/bar/test.jsp" );
            goGet2(host, port, "ELIgnored", contextRoot + "/foo/bar/baz/test.jsp" );
            goGet2(host, port, "ELIgnored", contextRoot + "/foo/bar/baz/test2.jsp" );
            
            if (count != EXPECTED_COUNT){
                stat.addStatus("Test UNPREDICTED-FAILURE", stat.FAIL);
            }           
        } catch (Throwable t) {
            System.out.println(t.getMessage());
            stat.addStatus("Test UNPREDICTED-FAILURE", stat.FAIL);
        }

        if (! fail) {
            stat.addStatus("web-jspconfig PASS", stat.PASS);
        }

        stat.printSummary("web/jsp-config---> expect 1 PASS");
    }

    private static void goGet(String host, int port,
                              String result, String contextPath)
         throws Exception
    {
        Socket s = null;
        OutputStream os = null;
        InputStream is = null;
        BufferedReader bis = null;
        try {
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
                    
                    if (! status.equalsIgnoreCase("PASS")){
                        fail = true;
                        stat.addStatus("web-jspconfig: Test " + count, stat.FAIL);                       
                    }
                    count++;
                } 
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
  
    private static void goGet2(String host, int port,
                              String result, String contextPath)
         throws Exception
    {
        Socket s = new Socket(host, port);
        OutputStream os = s.getOutputStream();

        System.out.println(("GET " + contextPath + " HTTP/1.0\n"));
        os.write(("GET " + contextPath + " HTTP/1.0\n").getBytes());
        os.write("\n".getBytes());
        
        InputStream is = s.getInputStream();
        BufferedReader bis = new BufferedReader(new InputStreamReader(is));
        String line = null;

        try{
            int index;
            while ((line = bis.readLine()) != null) {
                index = line.indexOf(result);
                System.out.println(line);
                if (index != -1) {
                    index = line.indexOf(":");
                    boolean ELAllowed = line.charAt(index+1) == 't';
                    boolean ELSeen = line.indexOf("${") >= 0;
                    if ((ELSeen && !ELAllowed) || (!ELSeen && ELAllowed)) {
                        fail = true;
                        stat.addStatus("web-jspconfig: Test "+count, stat.FAIL);
                    }
                    count++;
                }
            }
        } catch( Exception ex){
            ex.printStackTrace();   
            throw new Exception("Test UNPREDICTED-FAILURE");
        }
    }
}
