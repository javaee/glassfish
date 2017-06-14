/*
 * Copyright 2002 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.sun.s1asdev.ejb.ejb30.hello.session3;


import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

import com.sun.ejte.ccl.reporter.SimpleReporterAdapter;

public class Client {

    private static SimpleReporterAdapter stat =  new SimpleReporterAdapter("appserv-tests");
    private String host;
    private String port;

    public Client(String[] args) {
        host = (args.length > 0) ? args[0] : "localhost";
        port = (args.length > 1) ? args[1] : "4848";
    }

    public static void main(String[] args) throws Exception {
        stat.addDescription("administered-object-definitionclient");
        Client client = new Client(args);
        client.doTestDD();
        stat.printSummary("administered-object-definitionclient");
    }

    private void doTestDD() throws Exception{
        String tcName = "administered-object-definition-web-ejb-test";
        InputStream ddIS=null;
        try{
            String url = "http://" + host + ":" + port + "/administered-object-definition/servlet";
            System.out.println("invoking administered-object-definition test servlet at " + url);
            int code = invokeServlet(url);


            if (code != 200) {
                System.out.println("Incorrect return code: " + code);
                stat.addStatus(tcName, stat.FAIL);
            } else {
                stat.addStatus(tcName, stat.PASS);
            }
            
        }catch(Exception e){
            stat.addStatus(tcName, stat.FAIL);
            throw e;
        }finally{
            if(ddIS != null){
                ddIS.close();
            }
        }
        return;
    }

    private int invokeServlet(String url) throws Exception {

        URL u = new URL(url);

        HttpURLConnection c1 = (HttpURLConnection) u.openConnection();
        int code = c1.getResponseCode();
        InputStream is = c1.getInputStream();
        BufferedReader input = new BufferedReader(new InputStreamReader(is));
        String line = null;
        
        while ((line = input.readLine()) != null)
            System.out.println(line);
        if (code != 200) {
            System.out.println("Incorrect return code: " + code);
        }
        return code;
    }

}

