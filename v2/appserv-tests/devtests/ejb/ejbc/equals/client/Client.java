package com.sun.s1asdev.ejb.ejbc.equals.client;

import java.io.*;
import java.util.*;
import javax.ejb.EJBHome;
import javax.naming.*;
import javax.rmi.PortableRemoteObject;
import org.omg.CORBA.ORB;
import com.sun.s1asdev.ejb.ejbc.equals.FooHome;
import com.sun.s1asdev.ejb.ejbc.equals.FooHomeSuper;
import com.sun.s1asdev.ejb.ejbc.equals.Foo;
import com.sun.s1asdev.ejb.ejbc.equals.FooSuper;
import com.sun.s1asdev.ejb.ejbc.equals.HelloHome;
import com.sun.s1asdev.ejb.ejbc.equals.Hello;
import com.sun.ejte.ccl.reporter.SimpleReporterAdapter;

public class Client {

    private static SimpleReporterAdapter stat = 
        new SimpleReporterAdapter("appserv-tests");

    public static void main (String[] args) {

        stat.addDescription("ejb-ejbc-equals");
        Client client = new Client(args);
        client.doTest();
        stat.printSummary("ejb-ejbc-equalsID");
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

            System.err.println("Narrowed FooHome!!");

            // make sure stubs for super class are available to client
            FooHomeSuper homeSuper = (FooHomeSuper)
                PortableRemoteObject.narrow(objref, FooHomeSuper.class);
                                                                     
            System.err.println("Narrowed FooHomeSuper!!");
                
                
            Foo f = home.create();
            System.err.println("Got the EJB!!");
            f.callHello();            

            FooSuper f2 = home.create();
            f2.louie();

            stat.addStatus("ejbclient main", stat.PASS);
        } catch(Exception e) {
            e.printStackTrace();
            stat.addStatus("ejbclient main" , stat.FAIL);
        }

	try {
            Context ic = new InitialContext();

            // create EJB using factory from container 
            Object objref = ic.lookup("java:comp/env/ejb/hello");
            System.out.println("objref = " + objref);
            System.err.println("Looked up home!!");


            HelloHome  home = (HelloHome)PortableRemoteObject.narrow
                (objref, HelloHome.class);
                                                                     
            System.err.println("Narrowed home!!");
                
            Hello hr = home.create();
            System.err.println("Got the EJB!!");
                
            System.out.println("invoking hello ejb");

            String said = hr.sayHello();

            String result = hr.assertValidRemoteObject();
            if (result == null) {
		stat.addStatus("ejbclient assertValidRemoteObject", stat.PASS);
	    } else {
		System.out.println("assertValidRemoteObject: " + result);
		stat.addStatus("ejbclient assertValidRemoteObject", stat.FAIL);
	    }

        } catch(Exception e) {
            e.printStackTrace();
            stat.addStatus("ejbclient assertValidRemoteObject" , stat.FAIL);
        }
        
    	return;
    }

}

