import java.io.*;
import java.net.*;

import com.sun.ejte.ccl.reporter.*;
/**
 * Unit test for 29661: Host Names parsing is case sensitive
 */
public class WebTest{

    static SimpleReporterAdapter stat=
           new SimpleReporterAdapter("appserv-tests");
    private static URLConnection conn = null;
    private static URL url;
    private static ObjectOutputStream objectWriter = null;
    private static ObjectInputStream objectReader = null;  
    
    public static void main(String args[]) throws Exception{
        String host = args[0];
        String port = args[1];
        String contextRoot = args[2];

        try{
            stat.addDescription("Case sensitive host mapping");
            
            System.out.println("Running test");
            url = new URL("http://LocalHost:" + port + contextRoot + "/ServletTest");
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
                
               if (urlConnection.getResponseCode() != 200){
                    stat.addStatus("caseSensitiveHostMapping", stat.FAIL);
               } else {
                    stat.addStatus("caseSensitiveHostMapping", stat.PASS);
               }
            }
            stat.printSummary("web/caseSensitiveHostMapping");
        }catch(Exception ex){
            ex.printStackTrace();
        }
    }

}
