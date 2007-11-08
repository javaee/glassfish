package com.sun.s1asdev.ejb.classload.lifecycle.client;

import java.io.*;
import java.util.*;
import javax.ejb.EJBHome;
import javax.naming.*;
import javax.rmi.PortableRemoteObject;
import org.omg.CORBA.ORB;
import com.sun.s1asdev.ejb.classload.lifecycle.FooHome;
import com.sun.s1asdev.ejb.classload.lifecycle.Foo;
import com.sun.ejte.ccl.reporter.SimpleReporterAdapter;

public class Client {

    private static SimpleReporterAdapter stat = 
        new SimpleReporterAdapter("appserv-tests");

    public static void main (String[] args) {

        stat.addDescription("ejb-classload-lifecycle");
        Client client = new Client(args);
        client.doTest();
        stat.printSummary("ejb-classload-lifecycleID");
    }  
    
    public Client (String[] args) {
    }
    
    public void doTest() {

        try {
            Context ic = new InitialContext();
                
            System.out.println("Looking up ejb ref ");
            // create EJB using factory from container 
            Object objref = ic.lookup("java:comp/env/ejb/foo");
            System.out.println("objref = " + objref);
            System.err.println("Looked up home!!");
                
            FooHome  home = (FooHome)PortableRemoteObject.narrow
                (objref, FooHome.class);
                                                                     
            System.err.println("Narrowed home!!");
                
                
            Foo f = home.create();
            System.err.println("Got the EJB!!");
                
            // invoke method on the EJB
            System.out.println("invoking ejb");
            f.callHello();
            System.out.println("successfully invoked ejb");

            System.err.println("Removing bean");
            f.remove();

            stat.addStatus("ejbclient main", stat.PASS);

        } catch(Exception e) {
            e.printStackTrace();
            stat.addStatus("ejbclient main" , stat.FAIL);
        }
        
    	return;
    }

}

