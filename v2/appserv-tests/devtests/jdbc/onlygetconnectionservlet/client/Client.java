package com.sun.s1asdev.jdbc.onlygetconnectionservlet.client;

import javax.naming.*;
import java.rmi.*;
import java.util.*;
import java.net.*;
import java.io.*;

import com.sun.s1asdev.jdbc.onlygetconnectionservlet.servlet.*;
//import com.sun.ejte.ccl.reporter.SimpleReporterAdapter;

public class Client {

    public static void main(String[] args)
        throws Exception {
        
 	//SimpleReporterAdapter stat = new SimpleReporterAdapter();
	String testSuite = "jdbc-onlygetconnection ";
        
        HttpURLConnection conn =(HttpURLConnection) new URL("http://localhost:8080/" +
            "onlygetconnectionservlet/onlygetconnectionservlet").openConnection();

        /*
        InputStream sis = conn.getInputStream(); 
        BufferedReader in = new BufferedReader( new InputStreamReader(sis) );
        System.out.println( conn.getResponseMessage() );
        String op = null;
        System.out.println(" Reading response"); 
        while( (op = in.readLine()) != null ) {
            System.out.println(op);
        }
        */

        Client c = new Client();
        c.multiThreadedTest( 40 );
       
        /*
        if ( bean.test1() ) {
	    stat.addStatus(testSuite+" test1 : ", stat.PASS);
	} else {
	    stat.addStatus(testSuite+" test1 : ", stat.FAIL);
	}
        
	stat.printSummary();
        */
        
    }

    public void multiThreadedTest( int numThreads ) throws Exception {

        for( int i = 0; i < numThreads; i++ ) {
            Thread t = new Thread( new Runnable() {
                HttpURLConnection conn = null;
                public void run() {
                    try {
                    conn =(HttpURLConnection) new URL("http://localhost:8080/" +
                         "onlygetconnectionservlet/onlygetconnectionservlet").openConnection();

                    InputStream sis = conn.getInputStream(); 
                    BufferedReader in = new BufferedReader( new InputStreamReader(sis) );
                    System.out.println( conn.getResponseMessage() );
                    String op = null;
                    System.out.println(" Reading response"); 
                    while( (op = in.readLine()) != null ) {
                        //System.out.println(op);
                    }
                    } catch( Exception e ) {
                        e.printStackTrace();
                    } finally {
                        conn.disconnect();
                    }
                }
            });

            t.start();
        }
    }
}
