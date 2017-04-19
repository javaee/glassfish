package com.sun.s1asdev.timer31.reschedule_after_failures.client;

import java.io.Serializable;
import java.rmi.NoSuchObjectException;
import java.util.Date;
import java.util.Properties;
import java.util.Set;

import javax.ejb.*;
//import javax.jms.*;
import javax.naming.*;
import javax.rmi.PortableRemoteObject;

import com.sun.s1asdev.ejb31.timer.reschedule_after_failures.Stles;

import com.sun.ejte.ccl.reporter.SimpleReporterAdapter;

public class Client {

    private static SimpleReporterAdapter stat = 
        new SimpleReporterAdapter("appserv-tests");

    InitialContext context;

    @EJB private static Stles bean;

    public static void main(String args[]) { 

        stat.addDescription("ejb31-timer-reschedule_after_failures");


        try {
            System.out.println("Creating timers for reschedule_after_failures timer test");
            bean.createTimers(); 
            System.out.println("Waiting some time for timers to fail and be rescheduled for reschedule_after_failures timer test");
            Thread.sleep(24000);
            System.out.println("Verifying timers for reschedule_after_failures timer test");
            bean.verifyTimers(); 
            stat.addStatus("reschedule_after_failures: ", stat.PASS );

        } catch(Exception e) {
            stat.addStatus("reschedule_after_failures: ", stat.FAIL);
            e.printStackTrace();
        }

        stat.printSummary("ejb31-timer-reschedule_after_failures");
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
