/*
 * Copyright 2002 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.sun.s1asdev.ejb.ejb30.hello.session3;

import java.net.*;
import java.io.*;
import java.util.*;
import javax.naming.*;

import com.sun.ejte.ccl.reporter.SimpleReporterAdapter;

import javax.persistence.EntityManagerFactory;
import javax.persistence.PersistenceUnit;

import javax.persistence.PersistenceContext;
import javax.persistence.PersistenceContextType;
import javax.persistence.EntityManager;

public class Client {
    
    private String host;
    private String port;

    private static SimpleReporterAdapter stat =
        new SimpleReporterAdapter("appserv-tests");

    @PersistenceUnit 
        private static EntityManagerFactory emf1;

    @PersistenceUnit(name="myemf", unitName="foo") 
        private static EntityManagerFactory emf2;

    public Client (String[] args) {
        host = ( args.length > 0) ? args[0] : "localhost";
        port = ( args.length > 1) ? args[1] : "4848";
    }
    
    public static void main(String[] args) {
        stat.addDescription("ejb-ejb30-hello-session3client");
        Client client = new Client(args);
        client.doTest();
        stat.printSummary("ejb-ejb30-hello-session3ID");
    }
    
    public void doTest() {
        
        String env = null;
        try {

            InitialContext ic = new InitialContext();

            if( (emf1 != null) && (emf2 != null) ) {

                emf1.isOpen();
                emf2.isOpen();

                EntityManagerFactory lookupemf1 = (EntityManagerFactory)
                    ic.lookup("java:comp/env/com.sun.s1asdev.ejb.ejb30.hello.session3.Client/emf1");

                EntityManagerFactory lookupemf2 = (EntityManagerFactory)
                    ic.lookup("java:comp/env/myemf");

                System.out.println("AppClient successful injection of EMF references!");
            } else {
                throw new Exception("One or more EMF/EM references" +
                                    " was not injected in AppClient");
            }


            String url = "http://" + host + ":" + port + 
                "/ejb-ejb30-hello-session3/servlet";
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

