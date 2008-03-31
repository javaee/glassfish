package test.jdbc.jdbcusertx;
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
public class JdbcUserTxTestNG {

    private static final String TEST_NAME =
        "jdbc-jdbcusertx";
   
    private String strContextRoot="jdbcusertx";

    static String result = "";
    String host=System.getProperty("http.host");
    String port=System.getProperty("http.port");
           
    /*
     *If two asserts are mentioned in one method, then last assert is taken in
     *to account.
     *Each method can act as one test within one test suite
     */


    @Test(groups ={ "pulse"} ) // test method
    public void testUserTx() throws Exception{
        
        try{

          String testurl = "http://" + host  + ":" + port + "/"+ 
	    strContextRoot + "/MyServlet?testcase=usertx";
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
	  String EXPECTED_RESPONSE ="user-tx-commit:true";
	  String EXPECTED_RESPONSE2 ="user-tx-rollback:true";
	  while ((line = input.readLine()) != null) {
	    // echo(line);
            if(line.indexOf(EXPECTED_RESPONSE)!=-1 &&
               line.indexOf(EXPECTED_RESPONSE2)!=-1){
	      testLine = line;
	      echo(testLine);
	      result=true;
	      break;
            }
	  }        
                
	  Assert.assertEquals(result, true,"Unexpected Results");
        
        }catch(Exception e){
	  e.printStackTrace();
	  throw new Exception(e);
        }

    }

    @Test(groups ={ "pulse"} ) // test method
    public void testNoLeak() throws Exception{
        
        try{

          String testurl = "http://" + host  + ":" + port + "/"+ 
	    strContextRoot + "/MyServlet?testcase=noleak";
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
	  String EXPECTED_RESPONSE ="no-leak-test:true";
	  while ((line = input.readLine()) != null) {
	    // echo(line);
   	    if(line.indexOf(EXPECTED_RESPONSE)!=-1){
	      testLine = line;
	      echo(testLine);
	      result=true;
	      break;
            }
	  }        
                
	  Assert.assertEquals(result, true,"Unexpected Results");
               
        }catch(Exception e){
            e.printStackTrace();
            throw new Exception(e);
        }

    }

    public static void echo(String msg) {
        System.out.println(msg);
    }

}
