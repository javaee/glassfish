package com.sun.s1asdev.ejb.ejb30.clientview.adapted.client;

import java.io.*;
import java.util.*;
import javax.ejb.EJB;
import org.omg.CORBA.ORB;
import com.sun.s1asdev.ejb.ejb30.clientview.adapted.*;
import com.sun.ejte.ccl.reporter.SimpleReporterAdapter;
import javax.naming.InitialContext;

import java.rmi.Remote;

public class Client {

    private static SimpleReporterAdapter stat = 
        new SimpleReporterAdapter("appserv-tests");

    static final int NUM_EXTRA_HELLOS = 6;

    @EJB static private Hello hr;

    public static void main (String[] args) {

        stat.addDescription("ejb-ejb30-clientview-adapted");
        Client client = new Client(args);
        client.doTest();
        stat.printSummary("ejb-ejb30-clientview-adaptedID");
    }  
    
    public Client (String[] args) {

    }
    
    public void doTest() {

        Set<Hello> hellos = new HashSet<Hello>();

        try {

	    String lookupStr = 
		"java:comp/env/com.sun.s1asdev.ejb.ejb30.clientview.adapted.client.Client/hr";

	    if( hr == null ) {
		System.out.println("In stand-alone mode");
		lookupStr = "ejb/ejb_ejb30_clientview_adapted_Hello";
		hr = (Hello) new InitialContext().lookup(lookupStr);
	    }

            hellos.add(hr);
            InitialContext ic = new InitialContext();
            for(int i = 0; i < NUM_EXTRA_HELLOS; i++) {
                hellos.add( (Hello) ic.lookup(lookupStr));
            }

            System.out.println("Sleeping to wait for passivation...");
            // Wait enough time for some passivation to happen.
            Thread.sleep(10000);
            System.out.println("Waking up");

            int numPassivatedActivated = 0;

            for(Hello next : hellos) {
                
               
		   if( next.hasBeenPassivatedActivated() ) {
                    numPassivatedActivated++;
                }
	

                System.out.println("Running test for " + next);
                // do something right here
                System.out.println("\nStateful Session results (microsec): \twith tx \tno tx:");
                next.warmup(Common.STATEFUL);
                runTests(Common.STATEFUL, next);
                
                System.out.println("\nStateless Session results (microsec): \twith tx \tno tx:");
                next.warmup(Common.STATEFUL);
                runTests(Common.STATELESS, next);
                
                next.shutdown();
            }

            if ( numPassivatedActivated == 0 ) {
                throw new Exception("Passivation/Activation could not be tested.  No Passivation/Activation occurred");
            }

	    System.out.println("Hellos.size() : " + hellos.size() + " passivationCount: " + numPassivatedActivated);
	    
	    stat.addStatus("local main", stat.PASS);

	    boolean refResult1 = hr.checkSlessLocalReferences();
	    stat.addStatus("local checkSlessReferences",
		    (refResult1 == true) ? stat.PASS : stat.FAIL);

	    boolean refResult2 = hr.checkSfulLocalReferences();
	    stat.addStatus("local checkSfulLocalReferences",
		    (refResult2 == true) ? stat.PASS : stat.FAIL);
            
            boolean refSlessResult = hr.checkSlessRemoteReferences();
	    stat.addStatus("local checkSlessRemoteReferences",
		    (refSlessResult == true) ? stat.PASS : stat.FAIL);
                        
            boolean refResult3 = hr.checkSfulRemoteReferences();
	    stat.addStatus("local checkSfulRemoteReferences",
		    (refResult3 == true) ? stat.PASS : stat.FAIL);
            
            DummyRemote ref1 = hr.getSfulRemoteBusiness(1);
            DummyRemote ref2 = hr.getSfulRemoteBusiness(2);
            boolean refResult4 = ref1.equals(ref1);
            boolean refResult5 = ref2.equals(ref2);
            boolean refResult6 = ref1.equals(ref2);
            boolean refResult7 = hr.compareRemoteRefs(ref1, ref1);
            boolean refResult8 = hr.compareRemoteRefs(ref2, ref2);
            boolean refResult9 = hr.compareRemoteRefs(ref1, ref2);
            boolean refResult10 = hr.compareRemoteRefs(ref1, hr);
            boolean refResult11 = hr.compareRemoteRefs(ref1, null);
            
            boolean refResultSummary = (refResult4 == true)
                && (refResult5 == true)
                && (refResult6 == false)
                && (refResult7 == true)
                && (refResult8 == true)
                && (refResult9 == false)
                && (refResult10 == false)
                && (refResult11 == false);
                
            stat.addStatus("local compareRemoteReferences",
		    (refResult3 == true) ? stat.PASS : stat.FAIL);        
            
        } catch(Exception e) {
            e.printStackTrace();
            stat.addStatus("local main" , stat.FAIL);
        }
        
    	return;
    }

    

    private void runTests(int type, Hello hr)
	throws Exception
    {
      
        hr.notSupported(type, true);
        hr.notSupported(type, false);
        hr.supports(type, true);
	hr.supports(type, false);
        hr.required(type, true);
	hr.required(type, false);
        hr.requiresNew(type, true);
	hr.requiresNew(type, false);
        hr.mandatory(type, true);
	hr.never(type, false);
    }
}

