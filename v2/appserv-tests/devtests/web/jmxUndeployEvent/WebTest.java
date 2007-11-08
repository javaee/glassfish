import java.io.*;
import java.net.*;

import com.sun.ejte.ccl.reporter.*;

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
            stat.addDescription("JMX undeployment event test.");
            
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
                
               if (urlConnection.getResponseCode() != 404){
                    stat.addStatus("jmxUndeployEvent", stat.FAIL);
               } else {
                    stat.addStatus("jmxUndeployEvent", stat.PASS);
               }
            }

            stat.printSummary("web/jmxUndeployEvent");
        }catch(Exception ex){
            ex.printStackTrace();
        }
    }

}
