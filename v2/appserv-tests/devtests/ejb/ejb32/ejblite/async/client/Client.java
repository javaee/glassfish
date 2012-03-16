package com.acme;


import java.net.*;
import java.io.*;
import java.util.*;

import javax.naming.InitialContext;

import com.sun.ejte.ccl.reporter.SimpleReporterAdapter;

public class Client {

    private static SimpleReporterAdapter stat = 
        new SimpleReporterAdapter("appserv-tests");

    private static String appName;
    private String host;
    private String port;

    public static void main(String args[]) {
	appName = args[0];
	stat.addDescription(appName);
	Client client = new Client(args);       
        client.doTest();	
        stat.printSummary(appName + "ID");
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
	    
	    stat.addStatus("local main", stat.PASS);

	} catch(Exception e) {
	    stat.addStatus("local main", stat.FAIL);
	    e.printStackTrace();
	}
    }


}
