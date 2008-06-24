
import java.lang.*;
import java.io.*;
import java.net.*;

import com.sun.ejte.ccl.reporter.*;

public class WebTest2 {
    
    private static int count = 0;
    private static int EXPECTED_COUNT = 1;
    
    static SimpleReporterAdapter stat=
        new SimpleReporterAdapter("appserv-tests");
    private static URLConnection conn = null;
    private static URL url;
    private static ObjectOutputStream objectWriter = null;
    private static ObjectInputStream objectReader = null;  

    public static void main(String args[]) {

        // The stat reporter writes out the test info and results
        // into the top-level quicklook directory during a run.
        stat.addDescription("Dynamic virtual-server/listener creation");
        String host = args[0];
        String port = args[1];
        String contextRoot = args[2];

        try{
            System.out.println("Running test");
            url = new URL("http://" + host  + ":" + port + "/" + contextRoot + "/ServletTest");
            String originalLoc = url.toString();
            System.out.println("\n Invoking url: " + url.toString());
            conn = url.openConnection();
            if (conn instanceof HttpURLConnection) {
               HttpURLConnection urlConnection = (HttpURLConnection)conn;
               urlConnection.setDoOutput(true);
               int responseCode=  urlConnection.getResponseCode();
               System.out.println("Response code: " + responseCode + " Expected code: 200"); 
               if (urlConnection.getResponseCode() == 200){
                    stat.addStatus("v3HttpListenerDynamicCongif-afterReconf", stat.PASS);
               } else {
                    stat.addStatus("v3HttpListenerDynamicCongif-afterReconf", stat.FAIL);
               }
            }
        }catch(Exception ex){
            stat.addStatus("v3HttpListenerDynamicCongif-afterReconf", stat.FAIL);
        }
        stat.printSummary("web/v3HttpListenerDynamicCongif-afterReconf");
    }
}
