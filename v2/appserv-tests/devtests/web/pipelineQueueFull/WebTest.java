
import java.lang.*;
import java.io.*;
import java.net.*;

import com.sun.ejte.ccl.reporter.*;

// Issue 25071
public class WebTest {
    
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
        stat.addDescription("Queue full exception");
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
               if (responseCode != 503){
                    stat.addStatus("pipelineQueueFull", stat.FAIL);
               } else {
                    stat.addStatus("pipelineQueueFull", stat.PASS);
               }
            }
        }catch(Exception ex){
            stat.addStatus("pipelineQueueFull", stat.PASS);
        }
        stat.printSummary("web/pipelineQueueFull");
    }
}
