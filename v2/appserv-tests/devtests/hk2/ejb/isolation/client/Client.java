package com.oracle.s1asdev.hk2.simple.client;

import java.io.*;
import java.util.*;
import javax.ejb.EJB;
import javax.ejb.EJBHome;
import javax.naming.*;
import javax.rmi.PortableRemoteObject;
import com.sun.ejte.ccl.reporter.SimpleReporterAdapter;

import com.oracle.hk2devtest.isolation1.Isolation1;

public class Client {
    private static SimpleReporterAdapter stat = 
        new SimpleReporterAdapter("appserv-tests");

    @EJB(lookup="java:app/env/forappclient")
    private static Isolation1 isolation1;

    public static void main (String[] args) {

        stat.addDescription("hk2-ejb-isolation");
        Client client = new Client(args);
        client.doTest();
        stat.printSummary("hk2-ejb-isolationID");
    }  
    
    public Client (String[] args) {
    }
    
    public void doTest() {

        try {
            Context ic = new InitialContext();
                
            System.out.println("Looking up ejb ref " + isolation1);
            // create EJB using factory from container 
            // Object objref = ic.lookup("java:comp/env/ejb/foo");

            stat.addStatus("ejbclient main", stat.PASS);

        } catch(Exception e) {
            e.printStackTrace();
            stat.addStatus("ejbclient main" , stat.FAIL);
        }
        
    	return;
    }

}

