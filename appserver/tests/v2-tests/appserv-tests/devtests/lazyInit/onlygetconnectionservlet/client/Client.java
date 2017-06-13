package com.sun.s1asdev.jdbc.onlygetconnectionservlet.client;

import javax.naming.*;
import java.rmi.*;
import java.util.*;
import java.net.*;
import java.io.*;

import com.sun.s1asdev.jdbc.onlygetconnectionservlet.servlet.*;
import com.sun.ejte.ccl.reporter.SimpleReporterAdapter;

public class Client {

    public static void main(String[] args)
        throws Exception {
        
 	SimpleReporterAdapter stat = new SimpleReporterAdapter();
	String testSuite = "jdbc-onlygetconnection ";
        
        HttpURLConnection conn =(HttpURLConnection) new URL("http://localhost:8080/" +
            "onlygetconnectionservlet/onlygetconnectionservlet").openConnection();

        Client c = new Client();
        if (c.doTest()) {
	    stat.addStatus(testSuite+" test1 : ", stat.PASS);
        } else {
	    stat.addStatus(testSuite+" test1 : ", stat.FAIL);
	}
        
	stat.printSummary();
        
    }

    public boolean doTest() throws Exception {

                boolean pass = false;
                boolean fail = false;

                HttpURLConnection conn = null;
                    try {
                    conn =(HttpURLConnection) new URL("http://localhost:8080/" +
                         "onlygetconnectionservlet/onlygetconnectionservlet").openConnection();

                    InputStream sis = conn.getInputStream(); 
                    BufferedReader in = new BufferedReader( new InputStreamReader(sis) );
                    System.out.println( conn.getResponseMessage() );
                    String op = null;
                    System.out.println(" Reading response"); 
                    while( (op = in.readLine()) != null ) {
                        System.out.println(op);
                        if (op.indexOf("PASSED") != -1) {
                            pass = true;
                        }
                        if (op.indexOf("FAILED") != -1) {
                            fail = true;
                        }
                    }
                    } catch( Exception e ) {
                        e.printStackTrace();
                    } finally {
                        conn.disconnect();
                    }
               return pass && !fail;

    }
}
