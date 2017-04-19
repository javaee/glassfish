package com.sun.s1asdev.ejb.ejb32.persistence.unsynchronizedPC.txscope_synctype_mismatch.client;

import javax.ejb.EJB;

import com.sun.ejte.ccl.reporter.SimpleReporterAdapter;
import com.sun.s1asdev.ejb.ejb32.persistence.unsynchronizedPC.txscope_synctype_mismatch.ejb.Tester;

public class Client {

    private static SimpleReporterAdapter stat = new SimpleReporterAdapter(
            "appserv-tests");
    @EJB(beanName = "TC1SfsbWithUnsynchPC")
    private static Tester tC1SfsbWithUnsynchPC;
    
    @EJB(beanName = "TC2SlsbWithUnsynchPC")
    private static Tester tC2SlsbWithUnsynchPC;
    
    @EJB(beanName = "TC3SlsbWithSynchPC")
    private static Tester tC3SlsbWithSynchPC;
    
    @EJB(beanName = "TC4SlsbWithSynchPC")
    private static Tester tC4SlsbWithSynchPC;

    public static void main(String[] args) {
        stat.addDescription("ejb32-persistence-unsynchronizedPC-txscope-synctype-mismatch");
        Client client = new Client();
        client.doTest();
        stat.printSummary("ejb32-persistence-unsynchronizedPC-txscope-synctype-mismatch");
    }

    public void doTest() {
        try {
            System.out.println("I am in client");
            
            System.out.println("Calling tC1_SFSBWithUnsynchPC.doTest");
            stat.addStatus("TestCase1_sfsbWithUnsyncPCInvokeSlsbWithSynchPC", tC1SfsbWithUnsynchPC.doTest() ? stat.PASS : stat.FAIL);
            
            System.out.println("Calling tC2_SLSBWithUnsynchPC.doTest");
            stat.addStatus("TestCase2_slsbWithUnsyncPCInvokeSlsbWithSynchPC", tC2SlsbWithUnsynchPC.doTest() ? stat.PASS : stat.FAIL);
            
            System.out.println("Calling tC3_SLSBWithSynchPC.doTest");
            stat.addStatus("TestCase3_slsbWithSyncPCInvokeSlsbWithUnsynchPC", tC3SlsbWithSynchPC.doTest() ? stat.PASS : stat.FAIL);
            
            System.out.println("Calling tC4_SLSBWithSynchPC.doTest");
            stat.addStatus("TestCase4_slsbWithSyncPCInvokeSfsbWithUnsynchPC", tC4SlsbWithSynchPC.doTest() ? stat.PASS : stat.FAIL);
            
            System.out.println("DoTest method ends");
        } catch (Exception e) {
            e.printStackTrace();
            stat.addStatus("local main", stat.FAIL);
        }
    }

}
