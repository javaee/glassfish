package com.sun.s1asdev.ejb.ejb32.persistence.unsynchronizedPC.rollback_notclear_unsynchPC.client;

import javax.ejb.EJB;

import com.sun.ejte.ccl.reporter.SimpleReporterAdapter;
import com.sun.s1asdev.ejb.ejb32.persistence.unsynchronizedPC.rollback_notclear_unsynchPC.ejb.Tester;

public class Client {

    private static SimpleReporterAdapter stat = new SimpleReporterAdapter(
            "appserv-tests");
    @EJB(beanName = "SlessBean")
    private static Tester slessBean;

    public static void main(String[] args) {
        stat.addDescription("ejb32-persistence-unsynchronizedPC-rollback_notclear_unsynchPC");
        Client client = new Client();
        client.doTest();
        stat.printSummary("ejb32-persistence-unsynchronizedPC-rollback_notclear_unsynchPC");
    }

    public void doTest() {
        System.out.println("I am in client");
        
        try {
            stat.addStatus("TestUnsynchPCIsCLearedAfterRollback", slessBean.doTest() ? stat.PASS : stat.FAIL);
        } catch (Exception e) {
            e.printStackTrace();
            stat.addStatus("local main", stat.FAIL);
        }
    }

}
