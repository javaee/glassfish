package com.sun.s1asdev.ejb.ejb31.aroundtimeout.client;

import java.io.*;
import java.util.*;
import javax.ejb.EJB;
import javax.ejb.EJBException;
import com.sun.s1asdev.ejb.ejb31.aroundtimeout.*;
import com.sun.ejte.ccl.reporter.SimpleReporterAdapter;

public class Client {

    private static SimpleReporterAdapter stat = 
        new SimpleReporterAdapter("appserv-tests");

    public static void main (String[] args) throws Exception {

        stat.addDescription("ejb-ejb31-aroundtimeout");
        Client client = new Client(args);
        System.out.println("Sleeping for 7 seconds before checking results...");
        Thread.sleep(7000);
        client.doTest();
        stat.printSummary("ejb-ejb31-aroundtimeoutID");
    }  
    
    public Client (String[] args) {
    }
    
    private static @EJB Sless3 sless3;
    private static @EJB Sless4 sless4;
    private static @EJB Sless5 sless5;
    private static @EJB Sless6 sless6;

    public void doTest() {

        try {

            System.out.println("verifying Sless3 tests");

            sless3.noaroundtimeout();
            sless3.verify();

            System.out.println("verifying Sless4 tests");

            sless4.verify();
            try {
                sless4.cbd();
                throw new Exception("Sless4:cbd AroundTimeout called when invoked through interface");
            } catch(EJBException e) {}

            System.out.println("verifying Sless5 tests");

            sless5.verify();
            sless5.abdc();

            System.out.println("verifying Sless6 & SlessEJB7 tests");

            sless6.noaroundtimeout();
            sless6.verify();

            System.out.println("test complete");

            stat.addStatus("local main", stat.PASS);

        } catch(Exception e) {
            e.printStackTrace();
            stat.addStatus("local main" , stat.FAIL);
        }
        
    	return;
    }

}

