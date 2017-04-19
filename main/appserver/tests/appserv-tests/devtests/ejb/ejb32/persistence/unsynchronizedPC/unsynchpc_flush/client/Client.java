package com.sun.s1asdev.ejb.ejb32.persistence.unsynchronizedPC.unsynchpc_flush.client;

import javax.ejb.EJB;

import com.sun.ejte.ccl.reporter.SimpleReporterAdapter;
import com.sun.s1asdev.ejb.ejb32.persistence.unsynchronizedPC.unsynchpc_flush.ejb.Tester;

public class Client {

    private static SimpleReporterAdapter stat = new SimpleReporterAdapter(
            "appserv-tests");
    @EJB(beanName = "SLSBWithUnsynchPC")
    private static Tester sLSBWithUnsynchPC;

    public static void main(String[] args) {
        stat.addDescription("ejb32-persistence-unsynchronizedPC-unsynchpc-flush");
        Client client = new Client();
        client.doTest();
        stat.printSummary("ejb32-persistence-unsynchronizedPC-unsynchpc-flush");
    }

    public void doTest() {
        try {
            System.out.println("I am in client");

            System.out.println("Calling SLSBWithUnsynchPC.flushBeforeJoin");
            stat.addStatus("FlushUnsyncPCBeforeJoin", sLSBWithUnsynchPC.flushBeforeJoin() ? 
                    stat.PASS : stat.FAIL);
            
            System.out.println("Calling SLSBWithUnsynchPC.flushAfterJoin");
            stat.addStatus("FlushUnsyncPCAfterJoin", sLSBWithUnsynchPC.flushAfterJoin() ? 
                  stat.PASS : stat.FAIL);
            
            /* SPEC: If the flush method is not explicitly invoked, the persistence provider 
             * may defer flushing until commit time depending on the operations invoked and 
             * the flush mode setting in effect.
             */
            System.out.println("Calling SLSBWithUnsynchPC.AutoFlushByProvider");
            sLSBWithUnsynchPC.autoFlushByProvider("Tom3");
            stat.addStatus("AutoFlushByProvider", sLSBWithUnsynchPC.isPersonFound("Tom3") ? 
                    stat.PASS : stat.FAIL);
            
            System.out.println("DoTest method ends");
        } catch (Exception e) {
            e.printStackTrace();
            stat.addStatus("local main", stat.FAIL);
        }
    }

}
