package client.timer31.methodintf;

import javax.ejb.*;
import javax.naming.*;

import ejb31.timer.methodintf.Stles;

import com.sun.ejte.ccl.reporter.SimpleReporterAdapter;

public class Client {

    private static SimpleReporterAdapter stat = 
        new SimpleReporterAdapter("appserv-tests");

    InitialContext context;

    public static void main(String args[]) { 
        stat.addDescription("ejb31-timer-methodintf");

        try {
            Stles bean = (Stles) new InitialContext().lookup("java:global/ejb-timer-methodintf-ejb/StlesEJB");
            bean.createTimer();
            System.out.println("Waiting timers to expire for schedule_ann timer test");
            Thread.sleep(8000);
            System.out.println("Verifying timers transaction status");
            boolean pass = bean.verifyTimers(); 
            stat.addStatus("methodintf: ", ((pass)? stat.PASS : stat.FAIL) );

        } catch(Exception e) {
            stat.addStatus("methodintf: ", stat.FAIL);
            e.printStackTrace();
        }

        stat.printSummary("ejb31-timer-methodintf");
    }

}
