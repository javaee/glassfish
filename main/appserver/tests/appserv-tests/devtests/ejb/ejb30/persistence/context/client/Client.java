package com.sun.s1asdev.ejb.ejb30.persistence.context.client;

import java.io.*;
import java.util.*;
import javax.ejb.EJB;
import com.sun.s1asdev.ejb.ejb30.persistence.context.*;
import com.sun.ejte.ccl.reporter.SimpleReporterAdapter;

public class Client {

    private static SimpleReporterAdapter stat = 
        new SimpleReporterAdapter("appserv-tests");

    private String personName;

    public static void main (String[] args) {

        stat.addDescription("ejb-ejb30-persistence-context");
        Client client = new Client(args);
        client.doTest();
        stat.printSummary("ejb-ejb30-persistence-contextID");
    }  
    
    public Client (String[] args) {

        personName = "duke";

        if( args.length > 0 ) {
            personName = args[0];
        }

    }
    
    private static @EJB Sless sless;

    public void doTest() {

        try {

            System.out.println("I am in client");
            System.out.println("calling createPerson(" + personName + ")");
            sless.createPerson(personName);
            System.out.println("created ");
            System.out.println("calling findPerson(" + personName + ")");
            Person p = sless.findPerson(personName);
            if( p == null ) {
                throw new Exception("findPerson returned null");
            }
            System.out.println("found " + p);

            System.out.println("calling nonTxFindPerson(" + personName + ")");
            System.out.println("found " + sless.nonTxFindPerson(personName));

            System.out.println("calling nonTxFindPerson(" + personName + ")");
            System.out.println("found " + sless.nonTxFindPerson(personName));

            System.out.println("calling nonTxBlah(" + personName + ")");
            sless.createPerson("Bob");
            sless.nonTxTest2("Bob");

            System.out.println("removing Person(" + personName + ")");
            sless.removePerson(personName);
            System.out.println("removed Person(" + personName + ")");

            stat.addStatus("local main", stat.PASS);

        } catch(Exception e) {
            e.printStackTrace();
            stat.addStatus("local main" , stat.FAIL);
        }
        
    	return;
    }

}

