import java.io.*;
import java.net.*;

import com.sun.ejte.ccl.reporter.*;
/**
 * Unit test for 4929994 Admin virtual server should be treated as special case.
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
        int adminPort = Integer.parseInt(args[3]);

        try{
            stat.addDescription("Single Engine Test");
            
            System.out.println("Running test");
            url = new URL("http://" + host  + ":" + port + contextRoot + "/ServletTest");
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
                    stat.addStatus("singleEngine_responseCode", stat.FAIL);
               } else {
                    stat.addStatus("singleEngine_responseCode", stat.PASS);
               }
            }


            url = new URL("http://" + host  + ":" + adminPort + contextRoot + "/ServletTest");
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
                
               if (urlConnection.getResponseCode() != 404){
                    stat.addStatus("singleEngine-__asadamin", stat.FAIL);
               } else {
                    stat.addStatus("singleEngine-__asadmin", stat.PASS);
               }
            }


            stat.printSummary("web/singleEngine");
        }catch(Exception ex){
            ex.printStackTrace();
        }
    }

}
