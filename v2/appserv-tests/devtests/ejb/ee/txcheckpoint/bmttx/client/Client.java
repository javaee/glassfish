package com.sun.s1asdev.ejb.ee.client;

import java.util.ArrayList;

import java.io.Serializable;
import javax.naming.*;
import javax.jms.*;
import javax.ejb.*;
import javax.rmi.PortableRemoteObject;
import java.rmi.NoSuchObjectException;

import com.sun.s1asdev.ejb.ee.ejb.BMTSessionHome;
import com.sun.s1asdev.ejb.ee.ejb.BMTSession;

import com.sun.ejte.ccl.reporter.SimpleReporterAdapter;

public class Client {

    private static SimpleReporterAdapter stat = 
        new SimpleReporterAdapter("appserv-tests");

    private BMTSessionHome home;
    private BMTSession sfsb;

    private String _sfsbPrefix = "SFSB_" + System.currentTimeMillis() + "_";

    public static void main (String[] args) {

        stat.addDescription("bmttx");
        Client client = new Client(args);
        System.out.println("[bmttx] doTest()...");
        client.doTest();
        System.out.println("[bmttx] DONE doTest()...");
        stat.printSummary("bmttx");
    }  
    
    public Client (String[] args) {
    }
    
    public void doTest() {
        initSFSB();     //create SFSBs 
	checkPersistedFirstTime();
        txAccessCheck();       //access the SFBS
    }

    private void initSFSB() {
        System.out.println("[bmttx] Inside init....");
        try {
            Context ic = new InitialContext();
            Object objref = ic.lookup("java:comp/env/ejb/BMTSessionHome");
            home = (BMTSessionHome)PortableRemoteObject.narrow
                (objref, BMTSessionHome.class);
            sfsb = (BMTSession) home.create(_sfsbPrefix);
            System.out.println("[bmttx] Initalization done");
            stat.addStatus("ejbclient initSFSBList", stat.PASS);
        } catch(Exception e) {
            e.printStackTrace();
            //stat.addStatus("ejbclient localEntityGetEJBObject(-)" , stat.PASS);
            System.out.println("[bmttx] Exception in init....");
            e.printStackTrace();
            stat.addStatus("ejbclient initSFSBList", stat.FAIL);
        }
    }

    public void checkPersistedFirstTime() {
        try {
	    int prevActCount =  sfsb.getActivateCount();
	    int nowActCount =  sfsb.getActivateCount();

	    sfsb.startTx();
	    sfsb.getTxName();
	    sfsb.commitTx();

	    sfsb.startTx();
	    sfsb.getTxName();
	    sfsb.commitTx();

	    stat.addStatus("ejbclient checkPersistedFirstTime"
		 + "(" + prevActCount + " : " + nowActCount + " : "
		 + sfsb.getActivateCount() + ")", stat.PASS);
	} catch (Exception ex) {
	    stat.addStatus("ejbclient checkPersistedFirstTime", stat.FAIL);
	}
    }

    public void txAccessCheck() {
        try {
	    int prevActCount = 0;
	    int nowActCount = 0;
	    
	    prevActCount =  sfsb.getActivateCount();
	    sfsb.getName();
	    nowActCount =  sfsb.getActivateCount();
	    stat.addStatus("ejbclient NonTxNonCheckpointedMethod"
		+ " (" + prevActCount + " == " + nowActCount + ")",
		((prevActCount == nowActCount) ? stat.PASS : stat.FAIL));

	    prevActCount =  sfsb.getActivateCount();
	    sfsb.checkpoint();
	    nowActCount =  sfsb.getActivateCount();
	    stat.addStatus("ejbclient NonTxCheckpointedMethod"
		+ " (" + prevActCount + " != " + nowActCount + ")",
		((prevActCount != nowActCount) ? stat.PASS : stat.FAIL));

	    prevActCount = nowActCount;
	    sfsb.startTx();
	    nowActCount =  sfsb.getActivateCount();
	    stat.addStatus("ejbclient utBeginCheck"
		+ " (" + prevActCount + " == " + nowActCount + ")",
		((prevActCount == nowActCount) ? stat.PASS : stat.FAIL));

	    sfsb.incrementCount();
	    nowActCount =  sfsb.getActivateCount();
	    stat.addStatus("ejbclient TxBusinessMethodInsideTx"
		+ " (" + prevActCount + " == " + nowActCount + ")",
		((prevActCount == nowActCount) ? stat.PASS : stat.FAIL));

	    sfsb.getTxName();
	    nowActCount =  sfsb.getActivateCount();
	    stat.addStatus("ejbclient TxMethodInsideTx"
		+ " (" + prevActCount + " == " + nowActCount + ")",
		((prevActCount == nowActCount) ? stat.PASS : stat.FAIL));

	    sfsb.getName();
	    nowActCount =  sfsb.getActivateCount();
	    stat.addStatus("ejbclient NonTxMethodInsideTx"
		+ " (" + prevActCount + " == " + nowActCount + ")",
		((prevActCount == nowActCount) ? stat.PASS : stat.FAIL));

	    sfsb.checkpoint();
	    nowActCount =  sfsb.getActivateCount();
	    stat.addStatus("ejbclient checkpointedMethodInsideTx"
		+ " (" + prevActCount + " == " + nowActCount + ")",
		((prevActCount == nowActCount) ? stat.PASS : stat.FAIL));

	    sfsb.commitTx();
	    nowActCount =  sfsb.getActivateCount();
	    stat.addStatus("ejbclient commitTxCheck"
		+ " (" + prevActCount + " == " + nowActCount + ")",
		(((prevActCount+1) == nowActCount) ? stat.PASS : stat.FAIL));

        } catch (Exception ex) {
	    ex.printStackTrace();
            stat.addStatus("ejbclient txAccessCheck", stat.FAIL);
        }
    }

    private void sleepFor(int seconds) {
	System.out.println("Waiting for " + seconds + " seconds before accessing...");
	for (int i=0; i<seconds; i++) {
	    System.out.println("" + (seconds - i) + " seconds left...");
	    try {
		Thread.currentThread().sleep(1*1000);
	    } catch (Exception ex) {
	    }
	}
    }

} //Client{}
