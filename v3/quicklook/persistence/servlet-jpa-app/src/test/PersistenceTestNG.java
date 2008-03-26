/*
 * PersistenceTestNG.java
 *
 * Created on March 18, 2008, 8:36 AM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package test;

import org.testng.annotations.Configuration;
import org.testng.annotations.ExpectedExceptions;
import org.testng.annotations.Test;
import org.testng.annotations.*;
import org.testng.Assert;

import org.apache.commons.httpclient.*;
import org.apache.commons.httpclient.methods.*;
import org.apache.commons.httpclient.params.*;
import org.apache.commons.httpclient.cookie.*;

import java.io.*;
import java.net.*;
import java.util.*;

/*
 *Author Deepa Singh
*/

/**
 *
 * @author Deepa Singh
 */
public class PersistenceTestNG {
    
   private String strContextRoot="persistence";

    static String result = "";
    String m_host=System.getProperty("http.host");
    String m_port=System.getProperty("http.port"); 
    HttpClient httpclient = new HttpClient();
    
    
    //@Parameters({"host","port"})
    @BeforeMethod
    public void beforeTest(){
        m_host=System.getProperty("http.host");
        m_port=System.getProperty("http.port");
        
    }
    
    @Test(groups={"pulse"})
    public void testIndexPage() throws Exception{
        GetMethod method=null;
        System.out.println("Testing Index Page..");
        String comparedString="Hello-Java Persistence API Finder Test";
        try{
            System.out.println("Running method testRequestResponse");
            String testurl = "http://" + m_host  + ":" + m_port +
                    "/"+ strContextRoot + "/index.jsp";    
            
            System.out.println("running URL-->"+testurl);
            
            
            method = new GetMethod(testurl);
           
            
            int statusCode = httpclient.executeMethod(method);
            if (statusCode != HttpStatus.SC_OK) {
                System.err.println("Method failed: " + method.getStatusLine());
            } else
                System.out.println("print status ok "+statusCode);
            
            // Read the response body.
            byte[] responseBody = method.getResponseBody();
            String responseHTML=new String(responseBody);
            System.out.println(responseHTML);
            
            boolean result=false;
            
            if(responseHTML.indexOf(comparedString)!=-1){
                result=true;                
            }
                        
            System.out.println("testIndexPage result :"+result);
            Assert.assertEquals(result,true);
            
        }catch (HttpException e) {
            System.err.println("Fatal protocol violation: " + e.getMessage());
            e.printStackTrace();
        } catch (IOException e) {
            System.err.println("Fatal transport error: " + e.getMessage());
            e.printStackTrace();
        } 
        finally {
            // Release the connection.
            method.releaseConnection();
        }
    }
    
    @Test(groups={"pulse"})
    public void testServletFormMethod() throws Exception{
        
        GetMethod method=null;
        System.out.println("Testing Customer Details Page..");
        String comparedString="Search Customer Information";
        try{
        
        String testurl = "http://" + m_host  + ":" + m_port +
                    "/"+ strContextRoot + "/details";
        
        System.out.println("URL is:"+testurl);
        method = new GetMethod(testurl);
           
            
            int statusCode = httpclient.executeMethod(method);
            if (statusCode != HttpStatus.SC_OK) {
                System.err.println("Method failed: " + method.getStatusLine());
            } else
                System.out.println("print status ok "+statusCode);
        
        byte[] responseBody = method.getResponseBody();
            String responseHTML=new String(responseBody);
            //System.out.println(responseHTML);
            
            boolean result=false;
            
            if(responseHTML.indexOf(comparedString)!=-1){
                result=true;
                System.out.println(result);
            }
            System.out.println("testServletForm result :"+result);
            Assert.assertEquals(result,true);
        }catch (HttpException e) {
            System.err.println("Fatal protocol violation: " + e.getMessage());
            e.printStackTrace();
        } catch (IOException e) {
            System.err.println("Fatal transport error: " + e.getMessage());
            e.printStackTrace();
        } 
        finally {
            // Release the connection.
            method.releaseConnection();
        }
    }
    
    @Test(groups={"pulse"})
    public void testFinderMethod() throws Exception{
        
        GetMethod method=null;
        String comparedString="Alice_1";
        System.out.println("Testing Finder JPA method..");
        try{
            
            String testurl = "http://" + m_host  + ":" + m_port +
                    "/"+ strContextRoot + "/details?customer_nr=1";
            System.out.println("finding details of customer 1..");
            System.out.println("URL is:"+testurl);
            System.out.println("Response should be Alice_1");
            
            method = new GetMethod(testurl);
           
            
            int statusCode = httpclient.executeMethod(method);
            if (statusCode != HttpStatus.SC_OK) {
                System.err.println("Method failed: " + method.getStatusLine());
            } else
                System.out.println("print status ok "+statusCode);  
            
            byte[] responseBody = method.getResponseBody();
            String responseHTML=new String(responseBody);
            System.out.println(responseHTML);
            
            boolean result=false;
            
            if(responseHTML.indexOf(comparedString)!=-1){
                result=true;
                System.out.println(result);
            }
            System.out.println("testFinderMethod result :"+result);
            Assert.assertEquals(result,true);
            
        }catch (HttpException e) {
            System.err.println("Fatal protocol violation: " + e.getMessage());
            e.printStackTrace();
        } catch (IOException e) {
            System.err.println("Fatal transport error: " + e.getMessage());
            e.printStackTrace();
        } 
        finally {
            // Release the connection.
            method.releaseConnection();
        }
    }
}
            
 /*           String name="testuser";
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

}
    
    
    
}*/
