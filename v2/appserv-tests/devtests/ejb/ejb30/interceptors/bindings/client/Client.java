package com.sun.s1asdev.ejb.ejb30.interceptors.bindings.client;

import java.io.*;
import java.util.*;
import javax.ejb.EJB;
import com.sun.s1asdev.ejb.ejb30.interceptors.bindings.*;
import com.sun.ejte.ccl.reporter.SimpleReporterAdapter;

public class Client {

    private static SimpleReporterAdapter stat = 
        new SimpleReporterAdapter("appserv-tests");

    public static void main (String[] args) {

        stat.addDescription("ejb-ejb30-interceptors-bindings");
        Client client = new Client(args);
        client.doTest();
        stat.printSummary("ejb-ejb30-interceptors-bindingsID");
    }  
    
    public Client (String[] args) {
    }
    
    private static @EJB Sful sful;
    private static @EJB Sful2 sful2;
    private static @EJB Sless3 sless3;
    private static @EJB Sless4 sless4;
    private static @EJB Sless5 sless5;
    private static @EJB Sless6 sless6;

    public void doTest() {

        try {

            System.out.println("running Sful tests");
            sful.cef();
            sful.cefa();
            sful.cd();
            sful.ab();
            sful.abcd();
            sful.abef();
            sful.ef();
            sful.cdef();
            sful.abcdef();
            sful.acbdfe();
            sful.nothing();

            System.out.println("running Sful2 tests");

            sful2.abef(1);
            sful2.cd();
            sful2.ef();
            sful2.cdef();
            sful2.nothing();

            System.out.println("running Sless3 tests");

            sless3.dc();
            sless3.ba();
            sless3.dcba();
            sless3.baef();
            sless3.ef();
            sless3.dcf();
            sless3.dcef();
            sless3.nothing();
            sless3.dcbaef();
            sless3.abcdef();

            System.out.println("running Sless4 tests");

            sless4.abef(1);
            sless4.cbd();
            sless4.ef();
            sless4.cbdef();
            sless4.nothing();

            System.out.println("running Sless5 tests");

            sless5.abdc();
            sless5.dcfe();
            sless5.nothing();

            System.out.println("running Sless6 tests");

            sless6.ag();
            sless6.ag(1);
            
            sless6.bg();
            sless6.bg(1);
            
            sless6.cg();
            sless6.cg(1);
            sless6.cg("foo", 1.0);

            sless6.dg();
            sless6.dg(1);

            sless6.eg();
            sless6.eg(1);


            System.out.println("test complete");

            stat.addStatus("local main", stat.PASS);

        } catch(Exception e) {
            e.printStackTrace();
            stat.addStatus("local main" , stat.FAIL);
        }
        
    	return;
    }

}

