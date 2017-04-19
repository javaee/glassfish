package com.sun.s1asdev.ejb.stress.passivateactivate.client;

import java.util.ArrayList;

import java.io.Serializable;
import javax.naming.*;
import javax.jms.*;
import javax.ejb.*;
import javax.rmi.PortableRemoteObject;
import java.rmi.NoSuchObjectException;

import com.sun.s1asdev.ejb.stress.passivateactivate.ejb.SFSBHome;
import com.sun.s1asdev.ejb.stress.passivateactivate.ejb.SFSB;

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

        stat.addDescription("passivateactivate");
        Client client = new Client(args);
        System.out.println("[passivateactivateClient] doTest()...");
        client.doTest();
        System.out.println("[passivateactivateClient] DONE doTest()...");
        stat.printSummary("passivateactivate");
    }  
    
    public Client (String[] args) {
    }
    
    public void doTest() {
        initSFSBList();     //create SFSBs 
        accessSFSB();       //access the SFBS

    }

    private void initSFSBList() {
        System.out.println("[passivateactivateClient] Inside init....");
        try {
            Context ic = new InitialContext();
            Object objref = ic.lookup("java:comp/env/ejb/SFSB");
            SFSBHome home = (SFSBHome)PortableRemoteObject.narrow
                (objref, SFSBHome.class);
            for (int i=0; i < MAX_SFSBS; i++) {

                //Creating these many SFSBs will cause passivation
                SFSB sfsb = (SFSB) home.create("SFSB_"+i);

                sfsbList.add(sfsb);
            }
            System.out.println("[passivateactivate] Initalization done");
            stat.addStatus("ejbclient initSFSBList", stat.PASS);
        } catch(Exception e) {
            e.printStackTrace();
            //stat.addStatus("ejbclient localEntityGetEJBObject(-)" , stat.PASS);
            System.out.println("[passivateactivateClient] Exception in init....");
            e.printStackTrace();
            stat.addStatus("ejbclient initSFSBList", stat.FAIL);
        }
    }

    public void accessSFSB() {
        try {
            for (int i=0; i < MAX_SFSBS; i++) {
                SFSB sfsb = (SFSB) sfsbList.get(i);
                String sfsbName = sfsb.getName();
                System.out.println("Successfully accessed SFSB bean for: " + sfsbName);
    
                stat.addStatus("ejbclient accessSFSB", stat.PASS);
            }
        } catch (Exception ex) {
            stat.addStatus("ejbclient accessSFSB", stat.FAIL);

        }
    }

} //Client{}
