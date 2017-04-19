package com.sun.s1asdev.timer31.schedule_ann.client;

import java.io.Serializable;
import java.rmi.NoSuchObjectException;
import java.util.Date;
import java.util.Properties;
import java.util.Set;

import javax.ejb.*;
//import javax.jms.*;
import javax.naming.*;
import javax.rmi.PortableRemoteObject;

import com.sun.s1asdev.ejb31.timer.schedule_ann.Stles;

import com.sun.ejte.ccl.reporter.SimpleReporterAdapter;

public class Client {

    private static SimpleReporterAdapter stat = 
        new SimpleReporterAdapter("appserv-tests");

    InitialContext context;

    @EJB private static Stles bean;

    public static void main(String args[]) { 
        boolean doJms = false; // TODO (args.length == 1) && (args[0].equalsIgnoreCase("jms"));

        stat.addDescription("ejb31-timer-schedule_ann");


        try {
            System.out.println("Waiting timers to expire for schedule_ann timer test");
            Thread.sleep(12000);
            System.out.println("Verifying timers for schedule_ann timer test");
            bean.verifyTimers(); 
            stat.addStatus("schedule_ann: ", stat.PASS );

        } catch(Exception e) {
            stat.addStatus("schedule_ann: ", stat.FAIL);
            e.printStackTrace();
        }

        stat.printSummary("ejb31-timer-schedule_ann");
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
