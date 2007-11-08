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
            stat.addDescription("Standalone Servlet/Filter war test");
            
            System.out.println("Running test");
            url = new URL("http://" + host  + ":" + port + contextRoot + "/ServletTest");
            String originalLoc = url.toString();
            for (int k=0; k < 3; k++){
                System.out.println("\n Invoking url: " + url.toString());
                conn = url.openConnection();
                if (conn instanceof HttpURLConnection) {
                    HttpURLConnection urlConnection = (HttpURLConnection)conn;
                    urlConnection.setDoOutput(true);

                    DataOutputStream out = 
                       new DataOutputStream(urlConnection.getOutputStream());
                                        out.writeByte(1);

                   int responseCode=  urlConnection.getResponseCode();
                   String encodedURL = urlConnection.getHeaderField ("Location");
                   System.out.println("responseCode: " + responseCode);
                   System.err.println("encodedURL : " + encodedURL);
                    
                   if (urlConnection.getResponseCode() != 201){
                        stat.addStatus("contentLength-responseCode", stat.FAIL);
                   } else {
                        stat.addStatus("contentLength-responseCode", stat.PASS);
                   }
                
                   if (encodedURL != null && !encodedURL.startsWith(originalLoc)){
                        stat.addStatus("contentLength-header", stat.FAIL);
                   } else {
                        stat.addStatus("contentLength-header", stat.PASS);
                   }
                }
            }
            stat.printSummary("web/contentLength");
        }catch(Exception ex){
            ex.printStackTrace();
        }
    }

}
