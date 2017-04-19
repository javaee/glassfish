package com.acme.ejb32.timer.getalltimers;

import javax.ejb.*;
import javax.naming.*;

import com.sun.ejte.ccl.reporter.SimpleReporterAdapter;

public class Client {

    private static SimpleReporterAdapter stat = 
        new SimpleReporterAdapter("appserv-tests");

    InitialContext context;

    @EJB(lookup = "java:app/ejb-timer-getalltimers-ejb1/StlesTimeoutEJB")
    private static StlesTimeout stlesTimeout;
    @EJB(lookup = "java:app/ejb-timer-getalltimers-ejb2/StlesNonTimeoutEJB")
    private static StlesNonTimeout stlesNonTimeout;

    public static void main(String args[]) { 
        stat.addDescription("ejb32-timer-getalltimers");


        try {
            System.out.println("Waiting timers to expire for getalltimers timer test");
            Thread.sleep(2000);
            System.out.println("Verifying getalltimers from non-timeout bean");
            stlesNonTimeout.verifyAllTimers();
            stat.addStatus("getalltimers nontimeout: ", stat.PASS );

            System.out.println("Verifying getalltimers from timeout bean");
            stlesTimeout.createProgrammaticTimers();
            stlesTimeout.verify();
            stat.addStatus("getalltimers timeout: ", stat.PASS );
        } catch(Exception e) {
            stat.addStatus("getalltimers: ", stat.FAIL);
            e.printStackTrace();
        }

        stat.printSummary("ejb32-timer-getalltimers");
    }

    // when running this class through the appclient infrastructure
    public Client() {
        try {
            context = new InitialContext();
        } catch(Exception e) {
            System.out.println("Client : new InitialContext() failed");
            e.printStackTrace();
            stat.addStatus("Client() ", stat.FAIL);
        }
    }

}
