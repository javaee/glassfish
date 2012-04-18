package com.sun.s1asdev.timer31.schedule_on_ejb_timeout.client;

import java.io.Serializable;
import java.rmi.NoSuchObjectException;
import java.util.Date;
import java.util.Properties;
import java.util.Set;

import javax.ejb.*;
import javax.naming.*;
import javax.rmi.PortableRemoteObject;

import com.sun.s1asdev.ejb31.timer.schedule_on_ejb_timeout.Stles;

import com.sun.ejte.ccl.reporter.SimpleReporterAdapter;

public class Client {

    private static SimpleReporterAdapter stat = 
        new SimpleReporterAdapter("appserv-tests");

    InitialContext context;

    @EJB private static Stles bean;

    public static void main(String args[]) { 
        stat.addDescription("ejb31-timer-schedule_on_ejb_timeout");


        try {
            bean.createTimer();
            System.out.println("Waiting timers to expire for schedule_on_ejb_timeout timer test");
            Thread.sleep(2000);
            System.out.println("Verifying timers for schedule_on_ejb_timeout timer test");
            bean.verifyTimers(); 
            stat.addStatus("schedule_on_ejb_timeout: ", stat.PASS );

        } catch(Exception e) {
            stat.addStatus("schedule_on_ejb_timeout: ", stat.FAIL);
            e.printStackTrace();
        }

        stat.printSummary("ejb31-timer-schedule_on_ejb_timeout");
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
