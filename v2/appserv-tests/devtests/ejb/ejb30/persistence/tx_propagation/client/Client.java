package com.sun.s1asdev.ejb.ejb30.persistence.tx_propagation.client;

import java.io.*;
import java.util.*;
import javax.ejb.EJB;
import javax.transaction.UserTransaction;
import com.sun.s1asdev.ejb.ejb30.persistence.tx_propagation.*;

import com.sun.ejte.ccl.reporter.SimpleReporterAdapter;

public class Client {

    private static SimpleReporterAdapter stat =
        new SimpleReporterAdapter("appserv-tests");

    private String personName;

    public static void main (String[] args) {

        stat.addDescription("ejb-ejb30-persistence-tx_propagation");
        Client client = new Client(args);
        client.doTest(false);
        client.doTest(true);
        stat.printSummary("ejb-ejb30-persistence-tx_propagationID");
    }

    public Client (String[] args) {

        personName = "duke";

        if( args.length > 0 ) {
            personName = args[0];
        }

    }

    private static @EJB Sful sful;

    public void doTest(boolean commit) {

        try {
            
            System.err.println("I am in client");
            System.err.println("calling createPerson(" + personName + ")");
            sful.setName(personName);
            System.err.println("created ");

            UserTransaction utx = (UserTransaction)(new javax.naming.InitialContext()).lookup("java:comp/UserTransaction");
            utx.begin();
            System.err.println("utx.begin called ");
            
            Map<String, Boolean> map = sful.doTests();
            if (commit) {
                System.err.println("calling utx.commit ");
                utx.commit();
                System.err.println("utx.commit called ");
            } else {
                System.err.println("calling utx.rollback ");
                utx.rollback();
                System.err.println("utx.rollback called ");
            }
            
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

