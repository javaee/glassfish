package com.sun.s1asdev.ejb.ee.methodcheckpoint.simple.client;

import java.util.ArrayList;

import java.io.Serializable;
import javax.naming.*;
import javax.jms.*;
import javax.ejb.*;
import javax.rmi.PortableRemoteObject;
import java.rmi.NoSuchObjectException;

import com.sun.s1asdev.ejb.ee.ejb.SFSBHome;
import com.sun.s1asdev.ejb.ee.ejb.SFSB;

import com.sun.ejte.ccl.reporter.SimpleReporterAdapter;

public class Client {

    private static SimpleReporterAdapter stat = 
        new SimpleReporterAdapter("appserv-tests");

    private SFSBHome home;
    private SFSB sfsb;

    private String _sfsbPrefix = "SFSB_" + System.currentTimeMillis() + "_";

    public static void main (String[] args) {

        stat.addDescription("simpletx");
        Client client = new Client(args);
        System.out.println("[simpletxClient] doTest()...");
        client.doTest();
        System.out.println("[simpletxClient] DONE doTest()...");
        stat.printSummary("simpletx");
    }  
    
    public Client (String[] args) {
    }
    
    public void doTest() {
        initSFSBList();     //create SFSBs 
        accessSFSB();       //access the SFBS

    }

    private void initSFSBList() {
        System.out.println("[simpletxClient] Inside init....");
        try {
            Context ic = new InitialContext();
            Object objref = ic.lookup("java:comp/env/ejb/SFSBHome");
            home = (SFSBHome)PortableRemoteObject.narrow
                (objref, SFSBHome.class);
            sfsb = (SFSB) home.create(_sfsbPrefix);
            System.out.println("[simpletx] Initalization done");
            stat.addStatus("ejbclient initSFSBList", stat.PASS);
        } catch(Exception e) {
            e.printStackTrace();
            //stat.addStatus("ejbclient localEntityGetEJBObject(-)" , stat.PASS);
            System.out.println("[simpletxClient] Exception in init....");
            e.printStackTrace();
            stat.addStatus("ejbclient initSFSBList", stat.FAIL);
        }
    }

    public void accessSFSB() {
        try {
	    boolean actCountOK = true;
	    System.out.println("ActivateCount[0]: " + sfsb.getActivateCount());
	    actCountOK = sfsb.getActivateCount() == 0;

	    String retrievedName = sfsb.getName();
	    boolean nameOK = _sfsbPrefix.equalsIgnoreCase(retrievedName);
	    System.out.println("ActivateCount[1]: " + sfsb.getActivateCount());
	    actCountOK = (sfsb.getActivateCount() == 1);

	    System.out.println("ActivateCount[2]: " + sfsb.getActivateCount());
	    sfsb.getPassivateCount();
	    actCountOK = (sfsb.getActivateCount() == 1);

	    System.out.println("ActivateCount[3]: " + sfsb.getActivateCount());
	    sfsb.getPassivateCount();
	    actCountOK = (sfsb.getActivateCount() == 1);

	    System.out.println("ActivateCount[4]: " + sfsb.getActivateCount());
	    sfsb.getPassivateCount();
	    actCountOK = (sfsb.getActivateCount() == 1);

            if (nameOK && actCountOK) {
                stat.addStatus("ejbclient accessSFSB ", stat.PASS);
            } else {
                stat.addStatus("ejbclient accessSFSB ", stat.FAIL);
            }

        } catch (Exception ex) {
            stat.addStatus("ejbclient accessSFSB", stat.FAIL);
        }
    }

    private void sleepFor(int seconds) {
	System.out.println("Waiting for 10 seconds before accessing...");
	for (int i=0; i<seconds; i++) {
	    System.out.println("" + (10 - i) + " seconds left...");
	    try {
		Thread.currentThread().sleep(1*1000);
	    } catch (Exception ex) {
	    }
	}
    }

} //Client{}
