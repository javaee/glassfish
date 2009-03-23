package com.sun.s1asdev.ejb.ejb30.hello.session.client;

import java.io.*;
import java.util.*;
// import javax.ejb.EJB;
import javax.naming.InitialContext;
import com.sun.s1asdev.ejb.ejb30.hello.session.*;
import com.sun.ejte.ccl.reporter.SimpleReporterAdapter;

public class Client {

    private static SimpleReporterAdapter stat = 
        new SimpleReporterAdapter("appserv-tests");

    public static void main (String[] args) {

        stat.addDescription("ejb-ejb30-hello-session");
        Client client = new Client(args);
        client.doTest();
        stat.printSummary("ejb-ejb30-hello-sessionID");
    }  
    
    public Client (String[] args) {
    }

    /**    
    private static @EJB Sful sful;
    private static @EJB(mappedName="com.sun.s1asdev.ejb.ejb30.hello.session.Sless") Sless sless;
    */

    public void doTest() {

        try {

            System.out.println("Creating InitialContext()");
	    InitialContext ic = new InitialContext();

	    Sful sful = (Sful) ic.lookup("com.sun.s1asdev.ejb.ejb30.hello.session.Sful");
	    Sless sless = (Sless) ic.lookup("com.sun.s1asdev.ejb.ejb30.hello.session.Sless");

            System.out.println("invoking stateful");
            sful.hello();

            System.out.println("invoking stateless");
            sless.hello();

            System.out.println("test complete");

            stat.addStatus("local main", stat.PASS);

        } catch(Exception e) {
            e.printStackTrace();
            stat.addStatus("local main" , stat.FAIL);
        }
        
    	return;
    }

}

