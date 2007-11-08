import java.io.*;
import java.net.*;

import com.sun.ejte.ccl.reporter.*;
/**
 * Bugtraq 5047700 Installation Path Disclosure
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
            stat.addDescription("Basic Host/Context mapping");
            System.out.println("Running test");
            url = new URL("http://" + host  + ":" + port + "///BREAK");
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
               System.out.println("installationPathDisclosure: " + responseCode + " Expected code: 40X"); 
               if (urlConnection.getResponseCode() >= 400 && urlConnection.getResponseCode() < 500){
                    stat.addStatus("Test installationPathDisclosure", stat.PASS);
               } else {
                    stat.addStatus("Test installationPathDisclosure", stat.FAIL);
               }
            }
            url = new URL("http://" + host  + ":" + port + "/BREAK////");
            originalLoc = url.toString();
            System.out.println("\n Invoking url: " + url.toString());
            conn = url.openConnection();
            if (conn instanceof HttpURLConnection) {
                HttpURLConnection urlConnection = (HttpURLConnection)conn;
                urlConnection.setDoOutput(true);

                DataOutputStream out = 
                   new DataOutputStream(urlConnection.getOutputStream());
                                    out.writeByte(1);

               int responseCode=  urlConnection.getResponseCode();
               System.out.println("installationPathDisclosure: " + responseCode + " Expected code: 40X"); 
               if (urlConnection.getResponseCode() >= 400 && urlConnection.getResponseCode() < 500){
                    stat.addStatus("Test installationPathDisclosure-wrongUrl", stat.PASS);
               } else {
                    stat.addStatus("Test installationPathDisclosure-wrongUrl", stat.FAIL);
               }
            }
            stat.printSummary("web/installationPathDisclosure");
        }catch(Exception ex){
            ex.printStackTrace();
        }
    }

}
