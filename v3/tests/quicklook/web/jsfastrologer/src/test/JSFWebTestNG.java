package test.jsf.astrologer;

import org.testng.annotations.Configuration;
import org.testng.annotations.ExpectedExceptions;
import org.testng.annotations.Test;
import org.testng.annotations.*;
import org.testng.Assert;

// import org.apache.commons.httpclient.*;
// import org.apache.commons.httpclient.methods.*;
// import org.apache.commons.httpclient.params.*;
// import org.apache.commons.httpclient.cookie.*;

import java.io.*;
import java.net.*;
import java.util.*;

/*
 *Author Deepa Singh
 */


public class JSFWebTestNG {

    private static final String TEST_NAME =
        "jsf-webapp";

    private static final String EXPECTED_RESPONSE =
        "JSP Page Test";
    
    private String strContextRoot="jsfastrologer";

    static String result = "";
    String m_host="";
    String m_port="";    
    //HttpClient httpclient = new HttpClient();
    
    //@Parameters({"host","port"})
    @BeforeMethod
    public void beforeTest(){
        m_host=System.getProperty("http.host");
        m_port=System.getProperty("http.port");
    }
            
    /*
     *If tw
     o asserts are mentioned in one method, then last assert is taken in
     *to account.
     *Each method can act as one test within one test suite
     */


    @Test(groups ={ "pulse"} ) // test method
    //public void webtest(String host, String port, String contextroot) throws Exception{
    public void jsfAppDeployedFirstPagetest() throws Exception{
        
        try{
        System.out.println("Running TestMethod webtest");

        String testurl = "http://" + m_host  + ":" + m_port + "/"+ strContextRoot + "/faces/greetings.jsp";
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
            if(line.indexOf("Welcome to jAstrologer")!=-1){
                result=true;            
                testLine = line;
            
            }
            
        }     
                
        Assert.assertEquals(result, true,"Unexpected HTML");
               
        
        }catch(Exception e){
            e.printStackTrace();
            throw new Exception(e);
        }

    }
    
    
    @Test(groups ={ "pulse"} ) // test method
    public void jsfIndexPageBasicTest() throws Exception{
         try{
             
             System.out.println("Running TestMethod SimpleHTMLTest");
         

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
            if(line.indexOf("JavaServer Faces Greetings Page")!=-1){
                result=true;
             testLine = line;
           System.out.println(testLine);
            }
          
        }        
                
        Assert.assertEquals(result, true);
        
        }catch(Exception e){
            e.printStackTrace();
            throw new Exception(e);
        }
        
    }

    public static void echo(String msg) {
        System.out.println(msg);
    }
    
    
/*
    @Test(groups={"pulse"})
    public void testRequestResponse() throws Exception{
        try{
            System.out.println("Running method testRequestResponse");
            String testurl = "http://" + m_host  + ":" + m_port +
                    "/"+ strContextRoot + "/index.jsp";
            String name="testuser";
            String birthday="121212";
            System.out.println("URL is: "+testurl);
            GetMethod httpget=null;
            PostMethod post=null;
            httpget = new GetMethod(testurl);
            post=new PostMethod("http://localhost:8080/jsfastrologer/faces/greetings.jsp");

            
            NameValuePair[] mydata = {
                // new NameValuePair("loginID", itUser),
                // new NameValuePair("password", itPwd), Not working for editing of bug
                
                new NameValuePair("name",name),
                new NameValuePair("birthday",birthday)
            };
            
            post.setRequestBody(mydata);
            int statusCode = httpclient.executeMethod(post);
            System.out.println("print status ok "+statusCode);
             Assert.assertEquals(statusCode, 200);
            
            if (statusCode != HttpStatus.SC_OK) {
                System.err.println("Method failed: " + post.getStatusLine());
            }
            post.getStatusLine();
        
        String response=post.getResponseBodyAsString();
        System.out.println(response);
            
            
        }catch(Exception e){
            e.printStackTrace();
            throw new Exception(e);
        }
        
    }
*/

}
