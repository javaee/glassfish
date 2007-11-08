package com.sun.s1asdev.ejb.ejb30.persistence.eem_1slsb_2sfsbs.client;

import java.io.*;
import java.util.*;
import javax.ejb.EJB;
import com.sun.s1asdev.ejb.ejb30.persistence.eem_1slsb_2sfsbs.*;

import com.sun.ejte.ccl.reporter.SimpleReporterAdapter;

public class Client {

    private static SimpleReporterAdapter stat =
        new SimpleReporterAdapter("appserv-tests");

    private String personName;

    public static void main (String[] args) {

        stat.addDescription("ejb-ejb30-persistence-1slsb_2sfsbs");
        Client client = new Client(args);
        client.doTest();
        stat.printSummary("ejb-ejb30-persistence-1slsb_2sfsbsID");
    }

    public Client (String[] args) {

        personName = "duke";

        if( args.length > 0 ) {
            personName = args[0];
        }

    }

    private static @EJB Sful sful;

    public void doTest() {

        try {
            
            System.out.println("I am in client");
            System.out.println("calling createPerson(" + personName + ")");
            sful.setName(personName);
            System.out.println("created ");
            
            Map<String, Boolean> map = sful.doTests();
            
            Iterator<String> iter = map.keySet().iterator();
            while (iter.hasNext()) {
                String testName = iter.next();
                boolean result = map.get(testName);
                stat.addStatus("local " + testName,
                        (result) ? stat.PASS : stat.FAIL);
            }
        } catch(Exception e) {
            e.printStackTrace();
            stat.addStatus("local main" , stat.FAIL);
        }

    	return;
    }


}

