package com.sun.s1asdev.ejb.txprop.cmttimeout.client;

import java.io.*;
import java.util.*;
import javax.ejb.EJBHome;
import javax.naming.*;
import javax.rmi.PortableRemoteObject;
import org.omg.CORBA.ORB;

import com.sun.s1asdev.ejb.slsb.SimpleSLSBHome;
import com.sun.s1asdev.ejb.slsb.SimpleSLSB;
import com.sun.s1asdev.ejb.slsb.SimpleSLSBBean;

import com.sun.ejte.ccl.reporter.SimpleReporterAdapter;

public class Client {

    private static SimpleReporterAdapter stat = 
        new SimpleReporterAdapter("appserv-tests");

    public static void main (String[] args) {

        stat.addDescription("ejb-txprop-cmttimeout");
        Client client = new Client(args);
        client.doTest();
        stat.printSummary("ejb-txprop-cmttimeout");
    }  
    
    public Client (String[] args) {
    }
    
    public void doTest() {

        try {
            Context ic = new InitialContext();
                
            System.out.println("Looking up ejb ref ");
            // create EJB using factory from container 
            Object objref = ic.lookup("java:comp/env/ejb/SimpleSLSBHome");
            System.out.println("objref = " + objref);
                
            SimpleSLSBHome  home = (SimpleSLSBHome)PortableRemoteObject.narrow
                (objref, SimpleSLSBHome.class);
                                                                     
            System.err.println("Narrowed home!!");
                
                
            SimpleSLSB f = home.create();
            System.err.println("Got the EJB!!");

            doTest1(f);
            doTest2(f);

        } catch(Exception e) {
            e.printStackTrace();
            stat.addStatus("ejbclient main" , stat.FAIL);
        }
    }
        
    private void doTest1(SimpleSLSB ref) {
        try {
            // invoke method on the EJB
            System.out.println("invoking ejb");
            boolean result = ref.doSomething(8);

            System.out.println("successfully invoked ejb");
            stat.addStatus("ejbclient test1",
                    (result ? stat.PASS : stat.FAIL));
        } catch(Exception e) {
            e.printStackTrace();
            stat.addStatus("ejbclient test1" , stat.FAIL);
        }
    }

    private void doTest2(SimpleSLSB ref) {
        try {
            System.out.println("invoking ejb");
            boolean result = ref.doSomethingAndRollback();

            System.out.println("successfully invoked ejb");
            stat.addStatus("ejbclient test2",
                    (result ? stat.PASS : stat.FAIL));
        } catch(Exception e) {
            e.printStackTrace();
            stat.addStatus("ejbclient test2" , stat.FAIL);
        }
    }

}

