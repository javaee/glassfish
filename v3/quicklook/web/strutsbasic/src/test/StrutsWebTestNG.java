package test.web.strutsbasic;

import org.testng.annotations.Configuration;
import org.testng.annotations.ExpectedExceptions;
import org.testng.annotations.Test;
import org.testng.annotations.*;
import org.testng.Assert;

import java.io.*;
import java.net.*;
import java.util.*;

/*
 *Author Deepa Singh
 */

public class StrutsWebTestNG {

    private static final String TEST_NAME =
        "struts-webapp";

    private static final String EXPECTED_RESPONSE =
        "JSP Page Test";
    
    private String strContextRoot="strutsbasic";

    static String result = "";
    String m_host="";
    String m_port="";
    String host=System.getProperty("http.host");
    String port=System.getProperty("http.port");
    
    //@Parameters({"host","port"})
    @BeforeMethod
    //public void beforeTest(String httpHost,String httpPort){
    public void beforeTest(){
        m_host=System.getProperty("http.host");
        m_port=System.getProperty("http.port");
        System.out.println("Host is-->"+m_host);
        System.out.println("Port is-->"+m_port);
    }
            
    /*
     *If two asserts are mentioned in one method, then last assert is taken in
     *to account.
     *Each method can act as one test within one test suite
     */


    @Test(groups ={ "pulse"} ) // test method
    //public void webtest(String host, String port, String contextroot) throws Exception{
    public void strutsAppDeployedtest() throws Exception{
        
        try{
         

        String testurl = "http://" + m_host  + ":" + m_port + "/"+ strContextRoot + "/index.jsp";
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
            if(line.indexOf("Struts Welcome Page")!=-1){
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
    public void strutsBasicHTMLTest() throws Exception{
         try{
         

        String testurl = "http://" + m_host  + ":" + m_port + "/"+ strContextRoot + "/Welcome.do";
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
            if(line.indexOf("Struts Applications in Netbeans!")!=-1){
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
