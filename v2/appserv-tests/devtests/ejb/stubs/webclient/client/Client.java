/*
 * Copyright 2002 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.sun.s1asdev.ejb.stubs.webclient.client;

import java.net.*;
import java.io.*;
import java.util.*;
import javax.naming.*;

import com.sun.ejte.ccl.reporter.SimpleReporterAdapter;

public class Client {
    
    private String host;
    private String port;

    private static SimpleReporterAdapter stat =
        new SimpleReporterAdapter("appserv-tests");

    public Client (String[] args) {
        host = ( args.length > 0) ? args[0] : "localhost";
        port = ( args.length > 1) ? args[1] : "4848";
    }
    
    public static void main(String[] args) {
        stat.addDescription("ejb-stubs-webclient");
        Client client = new Client(args);
        client.doTest();
        stat.printSummary("ejb-stubs-webclientID");
    }
    
    public void doTest() {
        
        String env = null;
        try {
            String url = "http://" + host + ":" + port + 
                "/ejb-stubs-webclient/servlet";
            System.out.println("invoking webclient servlet at " + url);
            int code = invokeServlet(url);
            
            if(code != 200) {
                System.out.println("Incorrect return code: " + code);
                stat.addStatus("webclient main", stat.FAIL);
            } else {
                stat.addStatus("webclient main", stat.PASS);
            }
        } catch (Exception ex) {
            System.out.println("Jms web test failed.");
            stat.addStatus("webclient main", stat.FAIL);
            ex.printStackTrace();
        }
        
        return;
        
    }

    private int invokeServlet(String url) throws Exception {
            
        URL u = new URL(url);
        
        HttpURLConnection c1 = (HttpURLConnection)u.openConnection();
        int code = c1.getResponseCode();
        InputStream is = c1.getInputStream();
        BufferedReader input = new BufferedReader (new InputStreamReader(is));
        String line = null;
        while((line = input.readLine()) != null)
            System.out.println(line);
        if(code != 200) {
            System.out.println("Incorrect return code: " + code);
        }
        return code;
    }
    
}

