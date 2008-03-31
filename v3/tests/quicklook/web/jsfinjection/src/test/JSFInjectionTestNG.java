package test.web.jsfinjection;
import org.testng.annotations.Configuration;
import org.testng.annotations.ExpectedExceptions;
import org.testng.annotations.Test;
import org.testng.annotations.*;
import org.testng.Assert;

import java.io.*;
import java.net.*;
import java.util.*;

/**
 * Simple TestNG client for basic WAR containing JSF page with injectited values.
 * Client checks for two values: injected string and injected number.
 * If both values are as expected test passes.
 *
 * @author Lidia Marchioni <lidia.marchioni@sun.com>
 */
public class JSFInjectionTestNG {

    private static final String TEST_NAME =
        "simple-webapp-jsf-injection";
   
    private String strContextRoot="jsfinjection";

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
    public void injectedValuesTestPage() throws Exception {
        
      try {

        String errorText = "";
        boolean testPass = false;

        String testUrl = "http://" + host  + ":" + port + "/"+ strContextRoot + "/jsfHello.jsf";
        echo("URL is: " + testUrl);
        URL url = new URL(testUrl);
        echo("Connecting to: " + url.toString());
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.connect();
        int responseCode = conn.getResponseCode();
        if ( responseCode != 200 ) {
          echo("ERROR: http response code is " + responseCode);
          errorText = errorText + "ERROR: http response code is " + responseCode + ".\n";
        } else {
          echo("Connected: " + responseCode);
        }

        InputStream is = conn.getInputStream();
        BufferedReader input = new BufferedReader(new InputStreamReader(is));

        String line = null;
        String line2 = null;
        boolean result=false;
	String EXPECTED_RESPONSE = "Injected entry";
	String DIVIDER = "===";
        while ((line = input.readLine()) != null) {
          //echo (line);
          if (line.indexOf(EXPECTED_RESPONSE)!= -1) {
            testPass = true;
            echo("Received: " + line);
          }
          if (line.indexOf(DIVIDER)!= -1) {
            line2 = line;
            echo("Received: " + line2);
          }
        }

        if (! testPass) {
          echo("ERROR: injection 1 not found");
          errorText = errorText + "ERROR: injection 1 not found\n";
        }

        if (line2 != null) {
          String [] injection2Array = line2.split(DIVIDER);
          String injectedNumber = injection2Array[1].trim();
          echo("injectedNumber = " + injectedNumber);
          int num = Integer.parseInt(injectedNumber);
  
          if ( num < 0 ) {
            echo("ERROR: injection 2 is less than zero.");
            errorText = errorText + "ERROR: injection 2 is less than zero";
            testPass = false;
          } else {
            echo("Injection2 matched.");
          }
        } else {
          echo("ERROR: line with " + DIVIDER + " not found.");
          errorText = errorText + "ERROR: line with " + DIVIDER + " not found";
          testPass = false;
        }

        Assert.assertEquals(testPass, true, errorText);
        
      }catch(Exception e){
        echo("ERROR: caught exception!");
        e.printStackTrace();
        throw new Exception(e);
      }
    }

    public static void echo(String msg) {
      System.out.println(msg);
    }
}
