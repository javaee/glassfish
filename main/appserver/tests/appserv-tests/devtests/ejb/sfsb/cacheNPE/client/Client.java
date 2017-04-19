package com.sun.s1asdev.ejb.sfsb.cacheNPE.client;

import java.util.ArrayList;

import java.io.Serializable;
import javax.naming.*;
import javax.jms.*;
import javax.ejb.*;
import javax.rmi.PortableRemoteObject;
import java.rmi.NoSuchObjectException;

import com.sun.s1asdev.ejb.sfsb.cacheNPE.ejb.SFSBHome;
import com.sun.s1asdev.ejb.sfsb.cacheNPE.ejb.SFSB;

import com.sun.ejte.ccl.reporter.SimpleReporterAdapter;

public class Client {

    // consts
    public static String kTestNotRun    = "TEST NOT RUN";
    public static String kTestPassed    = "TEST PASSED";
    public static String kTestFailed    = "TEST FAILED";
    
    private static SimpleReporterAdapter stat = 
        new SimpleReporterAdapter("appserv-tests");

    private ArrayList sfsbList = new ArrayList();

    private SFSBHome home;

    public static void main (String[] args) {

        stat.addDescription("cacheNPE");
        Client client = new Client(args);
        System.out.println("[cacheNPEClient] doTest()...");
        client.doTest();
        System.out.println("[cacheNPEClient] DONE doTest()...");
        stat.printSummary("cacheNPE");
    }  
    
    public Client (String[] args) {
    }
    
    public void doTest() {
        initSFSBList(0,4);     //create SFSBs 

        for( int j = 0; j < 400; j++) {
            accessSFSB(0,3,5);
        }

        try {
            java.lang.Thread.sleep( 30 * 1000 );
        } catch (Exception ex ) {
            System.out.println ( "Exception caught : " + ex );
            ex.printStackTrace();
        } 

        removeSFSB(3);
        removeSFSB(2);
        accessSFSB(1,4,5);

        initSFSBList(5,20);     //create SFSBs 

    }

    private void initSFSBList(int i, int MAXSFSBS) {
        System.out.println("[cacheNPEClient] Inside init....");
        try {
            Context ic = new InitialContext();
            Object objref = ic.lookup("java:comp/env/ejb/SFSB");
            home = (SFSBHome)PortableRemoteObject.narrow
                (objref, SFSBHome.class);
            for (; i < MAXSFSBS; i++) {

                //Creating these many SFSBs will cause passivation
                SFSB sfsb = (SFSB) home.create("SFSB_"+i);

                sfsbList.add(sfsb);
            }
            System.out.println("[cacheNPE] Initalization done");
            stat.addStatus("ejbclient initSFSBs", stat.PASS);
        } catch(Exception e) {
            e.printStackTrace();
            System.out.println("[cacheNPEClient] Exception in init....");
            e.printStackTrace();
            stat.addStatus("ejbclient initSFSBs", stat.FAIL);
        }
    }

    public void accessSFSB(int i, int step, int MAXSFSBS) {
        try {
            boolean passed = true;
            int j=0;

            for (; i < MAXSFSBS; i=i+step) {
                SFSB sfsb = (SFSB) sfsbList.get(i);
                String sfsbName = sfsb.getName();
                
                boolean sessionCtxTest = sfsb.checkSessionContext();
                boolean initialCtxTest = sfsb.checkInitialContext();
                boolean entityHomeTest = sfsb.checkEntityHome();
                boolean entityLocalHomeTest = sfsb.checkEntityLocalHome();
                boolean entityRemoteTest = sfsb.checkEntityRemoteRef();
                boolean entityLocalTest = sfsb.checkEntityLocalRef();
                boolean homeHandleTest = sfsb.checkHomeHandle();
                boolean handleTest = sfsb.checkHandle();
                boolean utTest = sfsb.checkUserTransaction();

                System.out.println("In accessSFSB: for bean -> " + sfsbName 
                    + "; " + sessionCtxTest + "; " + initialCtxTest
                    + "; " + entityHomeTest + "; " + entityLocalHomeTest
                    + "; " + entityRemoteTest + "; " + entityLocalTest
                    + "; " + homeHandleTest + "; " + handleTest 
                    + "; " + utTest);

                passed = sessionCtxTest && initialCtxTest
                    && entityHomeTest && entityLocalHomeTest
                    && entityRemoteTest && entityLocalTest
                    && homeHandleTest && handleTest && utTest;

                if (! passed) {
                    break;
                }
            }
            

            if (passed) {
                stat.addStatus("ejbclient accessSFSBs", stat.PASS);
            } else {
                stat.addStatus("ejbclient accessSFSBs", stat.FAIL);
            }
        } catch (Exception ex) {
            stat.addStatus("ejbclient accessSFSB", stat.FAIL);

        }
    }

    public void removeSFSB(int i) {
        SFSB sfsb = (SFSB) sfsbList.get(i);
        try {
            String sfsbName = sfsb.getName();
            System.out.println("In removeSFSB for bean=" + sfsbName);
            sfsb.remove();
        } catch (Exception ex) {
            ex.printStackTrace();
            stat.addStatus("ejbclient removeSFSBs", stat.FAIL);
            return;
        } 

        stat.addStatus("ejbclient removeSFSBs", stat.PASS);

    }

} //Client{}
