package com.sun.s1asdev.ejb.sfsb.stress.client;

import java.util.ArrayList;

import java.io.Serializable;
import javax.naming.*;
import javax.jms.*;
import javax.ejb.*;
import javax.rmi.PortableRemoteObject;
import java.rmi.NoSuchObjectException;

import com.sun.s1asdev.ejb.sfsb.stress.ejb.StressSFSBHome;
import com.sun.s1asdev.ejb.sfsb.stress.ejb.StressSFSB;

import com.sun.ejte.ccl.reporter.SimpleReporterAdapter;

public class Client {

    // consts
    public static String kTestNotRun    = "TEST NOT RUN";
    public static String kTestPassed    = "TEST PASSED";
    public static String kTestFailed    = "TEST FAILED";
    
    private static SimpleReporterAdapter stat = 
        new SimpleReporterAdapter("appserv-tests");

    private static final int    MAX_SFSBS = 40;

    private ArrayList sfsbList = new ArrayList();

    public static void main (String[] args) {

        stat.addDescription("stress");
        Client client = new Client(args);
        System.out.println("[stressClient] doTest()...");
        client.doTest();
        System.out.println("[stressClient] DONE doTest()...");
        stat.printSummary("stress");
    }  
    
    public Client (String[] args) {
    }
    
    public void doTest() {
        initSFSBList();     //create SFSBs 
        accessSFSB();       //access the SFBS

    }

    private void initSFSBList() {
        System.out.println("[stressClient] Inside init....");
        try {
            Context ic = new InitialContext();
            Object objref = ic.lookup("java:comp/env/ejb/StressSFSBHome");
            StressSFSBHome home = (StressSFSBHome)PortableRemoteObject.narrow
                (objref, StressSFSBHome.class);
            for (int i=0; i < MAX_SFSBS; i++) {

                //Creating these many SFSBs will cause passivation
                StressSFSB sfsb = (StressSFSB) home.create("SFSB_"+i);

                sfsbList.add(sfsb);
            }
            System.out.println("[stress] Initalization done");
            stat.addStatus("ejbclient initSFSBList", stat.PASS);
        } catch(Exception e) {
            e.printStackTrace();
            //stat.addStatus("ejbclient localEntityGetEJBObject(-)" , stat.PASS);
            System.out.println("[stressClient] Exception in init....");
            e.printStackTrace();
            stat.addStatus("ejbclient initSFSBList", stat.FAIL);
        }
    }

    public void accessSFSB() {
        try {
            boolean passed = true;
            for (int i=0; i < MAX_SFSBS; i++) {
                StressSFSB sfsb = (StressSFSB) sfsbList.get(i);
                String sfsbName = "SFSB_"+i;
                

                passed = sfsb.doWork((long) 2000);

                System.out.println("Bean #: " + i + " ==> " + passed);

                if (! passed) {
                    break;
                }
            }

            if (passed) {
                stat.addStatus("ejbclient accessSFSB", stat.PASS);
            } else {
                stat.addStatus("ejbclient accessSFSB", stat.FAIL);
            }
        } catch (Exception ex) {
            stat.addStatus("ejbclient accessSFSB", stat.FAIL);

        }
    }

} //Client{}
