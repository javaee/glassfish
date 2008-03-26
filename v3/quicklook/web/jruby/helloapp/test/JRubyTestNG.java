package test.web.jruby.hello;
import org.testng.annotations.Configuration;
import org.testng.annotations.ExpectedExceptions;
import org.testng.annotations.Test;
import org.testng.annotations.Parameters;
import org.testng.Assert;

import java.io.*;
import java.net.*;
import java.util.*;

public class JRubyTestNG {

    private static final String TEST_NAME =
        "simple-jruby-webapp";

    private static final String EXPECTED_RESPONSE =
        "riding the Rails!";
    
    private String strContextRoot="helloapp";

    static String result = "";
    /*
     *If two asserts are mentioned in one method, then last assert is taken in
     *to account.
     *Each method can act as one test within one test suite
     */


    //@Parameters({ "host", "port", "contextroot" })
    @Test(groups ={ "pulse"} ) // test method
    //public void webtest(String host, String port, String contextroot) throws Exception{
    public void webtest() throws Exception{
        
        try{
            String host=System.getProperty("host");
            String port=System.getProperty("port");
            String contextRoot=System.getProperty("contextroot");

        String testurl = "http://" + host  + ":" + port + "/"+ contextRoot+"/";
        System.out.println("URL is: "+testurl);
        URL url = new URL(testurl);
        echo("Connecting to: " + url.toString());
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.connect();
        int responseCode = conn.getResponseCode();
        

        InputStream is = conn.getInputStream();
        BufferedReader input = new BufferedReader(new InputStreamReader(is));

        String line = null;
        boolean result=false;
        String testLine = null;        
        System.out.println("Reading HTML output..");
        while ((line = input.readLine()) != null) {
              //System.out.println(line);
            if(line.indexOf(EXPECTED_RESPONSE)!=-1){
                result=true;
             testLine = line;
           System.out.println(testLine);
            }
          
        }        
                
        Assert.assertEquals(result, true,"Unexpected HTML");
               
        
        }catch(Exception e){
            e.printStackTrace();
            throw new Exception(e);
        }

    }
    
    @Test(groups ={ "functional"} ) // test method
    public void controllerTest() throws Exception{
        try{
            String host=System.getProperty("host");
            String port=System.getProperty("port");
            String contextRoot=System.getProperty("contextroot");

        String testurl = "http://" + host  + ":" + port + "/"+ contextRoot
                +"/mycontroller/testview";
        System.out.println("URL is: "+testurl);
        URL url = new URL(testurl);
        echo("Connecting to: " + url.toString());
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.connect();
        int responseCode = conn.getResponseCode();
        

        InputStream is = conn.getInputStream();
        BufferedReader input = new BufferedReader(new InputStreamReader(is));

        String line = null;
        boolean result=false;
        String testLine = null;        
        System.out.println("Reading HTML output..");
        while ((line = input.readLine()) != null) {
              //System.out.println(line);
            if(line.indexOf("Mycontroller#testview")!=-1){
                result=true;
             testLine = line;
           System.out.println(testLine);
            }
          
        }        
                
        Assert.assertEquals(result, true,"Unexpected HTML");
               
        
        }catch(Exception e){
            e.printStackTrace();
            throw new Exception(e);
        }
        
    }
    
    public static void echo(String msg) {
        System.out.println(msg);
    }

}
