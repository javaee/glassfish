package com.sun.ejb.devtest.client;

import java.io.*;
import java.util.*;
import javax.naming.*;
import javax.ejb.EJB;
import com.sun.ejb.devtest.Sless;
import com.sun.ejb.devtest.Sless30;
import com.sun.ejte.ccl.reporter.SimpleReporterAdapter;

@EJB(name="ejb/GG", beanInterface=Sless.class)
public class Client {

    private static SimpleReporterAdapter stat = 
        new SimpleReporterAdapter("appserv-tests");

    public static void main (String[] args) {

        stat.addDescription("ejb-ejb30-allowed-session");
        Client client = new Client(args);
        client.doTest();
        stat.printSummary("ejb-ejb30-allowed-sessionID");
    }  
    
    public Client (String[] args) {
    }
    
    public void doTest() {
        try {
            (new MyThread()).start();
            (new MyThread()).start();
            (new MyThread()).start();
            (new MyThread()).start();
            (new MyThread()).start();
            (new MyThread()).start();
            stat.addStatus("intro sayHello", stat.PASS);
	} catch (Exception ex) {
            stat.addStatus("intro sayHello", stat.FAIL);
        }

    }


    private static class MyThread extends Thread {
        MyThread() {}
        
        public void run() {
            try {
                (new InitialContext()).lookup("java:comp/env/ejb/GG");
            } catch (Exception ex) {
                throw new RuntimeException(ex);
            }
        }
    }

}

