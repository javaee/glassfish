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
            stat.addDescription("Basic Host/Context mapping");
            System.out.println("Running test");
            url = new URL("http://" + host  + ":" + port + "/");
            String originalLoc = url.toString();
            System.out.println("\n Invoking url: " + url.toString());
            conn = url.openConnection();
            if (conn instanceof HttpURLConnection) {
                HttpURLConnection urlConnection = (HttpURLConnection)conn;
                urlConnection.setDoOutput(true);

                DataOutputStream out = 
                   new DataOutputStream(urlConnection.getOutputStream());
                                    out.writeByte(1);

               int responseCode=  urlConnection.getResponseCode();
               System.out.println("Response code: " + responseCode + " Expected code: 200"); 
               if (urlConnection.getResponseCode() != 200){
                    stat.addStatus("index", stat.FAIL);
               } else {
                    stat.addStatus("index", stat.PASS);
               }
            }
            stat.printSummary("web/index");
        }catch(Exception ex){
            stat.addStatus("index", stat.FAIL);
            ex.printStackTrace();
        }
    }

}
