package com.sun.s1asdev.ejb.stress.sfsbcache.client;

import java.util.ArrayList;

import java.io.Serializable;
import javax.naming.*;
import javax.jms.*;
import javax.ejb.*;
import javax.rmi.PortableRemoteObject;
import java.rmi.NoSuchObjectException;

import com.sun.s1asdev.ejb.stress.sfsbcache.ejb.SFSBHome;
import com.sun.s1asdev.ejb.stress.sfsbcache.ejb.SFSB;

import com.sun.ejte.ccl.reporter.SimpleReporterAdapter;

public class Client {

    // consts
    public static String kTestNotRun    = "TEST NOT RUN";
    public static String kTestPassed    = "TEST PASSED";
    public static String kTestFailed    = "TEST FAILED";
    
    private static SimpleReporterAdapter stat = 
        new SimpleReporterAdapter("appserv-tests");

    private static final int    MAX_SFSBS = 600;

    private ArrayList sfsbList = new ArrayList();

    public static void main (String[] args) {

        stat.addDescription("sfsbcache");
        Client client = new Client(args);
        System.out.println("[sfsbcacheClient] doTest()...");
        client.doTest();
        System.out.println("[sfsbcacheClient] DONE doTest()...");
        stat.printSummary("sfsbcache");
    }  
    
    public Client (String[] args) {
    }
    
    public void doTest() {
       
        System.out.println("[sfsbcacheClient] Inside init....");
        try {
            Context ic = new InitialContext();
            Object objref = ic.lookup("java:comp/env/ejb/SFSB");
            SFSBHome home = (SFSBHome)PortableRemoteObject.narrow
                (objref, SFSBHome.class);

            //Creating these many SFSBs will cause passivation
            SFSB sfsb = (SFSB) home.create("SFSB_");
   
            sfsb.getName();

            sfsb.remove();

            try {
                System.out.println("Calling getName() after removing bean");
                sfsb.getName();
                stat.addStatus("ejbclient accessSFSB", stat.FAIL);
            } catch(Exception e) {
                System.out.println("Successfully got exception after " +
                                   " calling business method on removed bean");
            }

            try {
                System.out.println("Calling getName() AGAIN after removing bean");
                sfsb.getName();
                stat.addStatus("ejbclient accessSFSB", stat.FAIL);
            } catch(Exception e) {
                System.out.println("Successfully got exception after " +
                                   " calling business method on removed bean");
                stat.addStatus("ejbclient accessSFSB", stat.PASS);
            }

        } catch(Exception e) {
            e.printStackTrace();
            //stat.addStatus("ejbclient localEntityGetEJBObject(-)" , stat.PASS);
            System.out.println("[sfsbcacheClient] Exception in init....");
            e.printStackTrace();
            stat.addStatus("ejbclient accessSFSB", stat.FAIL);
        }
    }                 

} //Client{}
