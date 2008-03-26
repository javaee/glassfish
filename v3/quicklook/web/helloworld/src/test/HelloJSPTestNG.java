package test.web.jsp.hello;
import org.testng.annotations.Configuration;
import org.testng.annotations.ExpectedExceptions;
import org.testng.annotations.Test;
import org.testng.annotations.*;
import org.testng.Assert;

import java.io.*;
import java.net.*;
import java.util.*;

/**
 * Simple TestNG client for basic WAR containing one JSP,one Servlet and one static
 *HTML resource.Each resources (HTML,JSP,Servlet) is invoked as a separate test.
 *
 * @author Deepa Singh <deepa.singh@sun.com>
 */
public class HelloJSPTestNG {

    private static final String TEST_NAME =
        "simple-webapp-jspservlet-noresource";
   
    private String strContextRoot="hellojsp";

    static String result = "";
    String host=System.getProperty("http.host");
    String port=System.getProperty("http.port");
           
    /*
     *If two asserts are mentioned in one method, then last assert is taken in
     *to account.
     *Each method can act as one test within one test suite
     */


    //@Parameters({ "host", "port", "contextroot" })
    @Test(groups ={ "pulse"} ) // test method
    //public void webtest(String host, String port, String contextroot) throws Exception{
    public void simpleJSPTestPage() throws Exception{
        
        try{
         

        String testurl = "http://" + host  + ":" + port + "/"+ strContextRoot + "/hello.jsp";
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
	String EXPECTED_RESPONSE ="JSP Test Page";
        while ((line = input.readLine()) != null) {
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

    @Test(groups={"pulse"}) //test method for server
    public void testServerRunning() throws Exception{
	    //Your server is up and running!
	    //
	String testurl = "http://" + host  + ":" + port;
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
        while ((line = input.readLine()) != null) {
            if(line.indexOf("Your server is up and running!")!=-1){
                result=true;
             testLine = line;
           echo(testLine);
            }
          
        }        
                
        Assert.assertEquals(result, true,"Unexpected HTML");
    }
    
    
    @Test(groups ={ "pulse"} ) // test method
    public void staticHTMLPageTest() throws Exception{
         try{
         

        String testurl = "http://" + host  + ":" + port + "/"+ strContextRoot + "/first.html";
        System.out.println("URL is: "+testurl);
        URL url = new URL(testurl);
        echo("Connecting to: " + url.toString());
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.connect();
        int responseCode = conn.getResponseCode();

        //Assert.assertEquals(responseCode, 200);

        InputStream is = conn.getInputStream();
        BufferedReader input = new BufferedReader(new InputStreamReader(is));

        String line = null;
        boolean result=false;
        String testLine = null;        
        while ((line = input.readLine()) != null) {
            if(line.indexOf("Welcome to HTML Test Program")!=-1){
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
    
    @Test(groups ={ "pulse"} ) // test method
    public void simpleServletTest() throws Exception{
         try{
         

        String testurl = "http://" + host  + ":" + port + "/"+ strContextRoot + "/simpleservlet";
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
        while ((line = input.readLine()) != null) {
            if(line.indexOf("Sample Application Servlet")!=-1){
                result=true;
             testLine = line;
           echo(testLine);
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
