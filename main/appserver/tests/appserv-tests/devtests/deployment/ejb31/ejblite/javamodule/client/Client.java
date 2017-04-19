package com.acme;


import java.net.*;
import java.io.*;
import java.util.*;

import javax.naming.InitialContext;

public class Client {

    private static String appName;
    private String host;
    private String port;

    public static void main(String args[]) {
        System.out.println("ejb31-ejblite-javamodule");
	appName = args[0];
	Client client = new Client(args);       
        client.doTest();	
    }

    public Client(String[] args) {
	host = args[1];
        port = args[2];
    }

    public void doTest() {

	try {

	    String url = "http://" + host + ":" + port + 
                "/" + appName + "/HelloServlet";

            System.out.println("invoking webclient servlet at " + url);

	    URL u = new URL(url);
        
	    HttpURLConnection c1 = (HttpURLConnection)u.openConnection();
	    int code = c1.getResponseCode();
	    InputStream is = c1.getInputStream();
	    BufferedReader input = new BufferedReader (new InputStreamReader(is));
	    String line = null;
	    while((line = input.readLine()) != null)
		System.out.println(line);
	    if(code != 200) {
		throw new RuntimeException("Incorrect return code: " + code);
	    }
            System.out.println("test complete");
	    
	} catch(Exception e) {
	    e.printStackTrace();
            System.exit(-1);
	}
        return;
    }
}
