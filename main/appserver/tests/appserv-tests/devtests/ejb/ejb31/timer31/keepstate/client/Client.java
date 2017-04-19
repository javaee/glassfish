package com.sun.s1asdev.timer31.keepstate.client;

import javax.ejb.*;
import com.sun.s1asdev.ejb31.timer.keepstate.KeepStateIF;
import com.sun.ejte.ccl.reporter.SimpleReporterAdapter;

public class Client {

    private static SimpleReporterAdapter stat = new SimpleReporterAdapter("appserv-tests");

    @EJB private static KeepStateIF bean;

    public static void main(String args[]) { 
        boolean keepState = Boolean.valueOf(args[0]);
        String testName = "ejb31-timer-keepstate-" + keepState;
        stat.addDescription(testName);
        try {
            testKeepState(keepState);
            stat.addStatus("testKeepState " + keepState + ": ", stat.PASS );
        } catch(Exception e) {
            stat.addStatus("testKeepState " + keepState + ": ", stat.FAIL );
            e.printStackTrace();
        }
        stat.printSummary(testName);
    }

    public static void testKeepState(boolean keepState) throws Exception {
        String result = bean.verifyTimers(keepState);        
        System.out.println("testKeepStateTrue result: " + result);
    }

}
