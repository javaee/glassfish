/*
 *
 * Copyright 2001 Sun Microsystems, Inc. All Rights Reserved.
 * 
 * This software is the proprietary information of Sun Microsystems, Inc.  
 * Use is subject to license terms.
 * 
 */

package com.sun.s1peqe.ejb.bmp.enroller.client;

import java.util.Iterator;
import java.util.ArrayList;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.rmi.PortableRemoteObject;
import com.sun.s1peqe.ejb.bmp.enroller.ejb.*;
import com.sun.ejte.ccl.reporter.SimpleReporterAdapter;

//Stress Client
public class EnrollerClient {
	private static final int MAXTHREADS= 1;
	private static final int MAXCYCLESPERTHREAD=1;
	private static ArrayList threadsAL = new ArrayList();
	private static Object lock = new Object();
	
	private static SimpleReporterAdapter status =
        new SimpleReporterAdapter("appserv-tests");

    public static void main(String[] args) {
        try {
            long timeStart = System.currentTimeMillis();
            System.out.println("Starting the test");
	    status.addDescription("Testing stress cmp roster app.");
	    for(int i = 0 ; i < MAXTHREADS; i++){
		    final int cnt = i;
		    //create Thrread to execute doTest
		    Thread t = new Thread(new Runnable(){
			    public void run() {
				    try { 
					    
					    String threadId = "Thr-" + cnt;
                                            System.out.print(""+cnt);
					    for(int j=0; j < MAXCYCLESPERTHREAD; j++ ){
						    System.out.print(".");
						    doTest(threadId);
					    }
				    } catch (Exception e) {
					    System.err.println("Caught an exception in Thread: " + e.toString());
					    e.printStackTrace();
				    }
			    }
		    });
		    threadsAL.add(t);
		    t.start();
	    }
	    
	    //finally join all threads
	    for(int i=0; i < threadsAL.size(); i++){
		    ((Thread)(threadsAL.get(i))).join();
	    }
	    long timeStop = System.currentTimeMillis();
	    //Approximately 65 transactions to myRoster per cycle per thread
	    System.out.println(" " + (double)(MAXTHREADS*MAXCYCLESPERTHREAD*65) + " transactions COMPLETED in " + (long)(timeStop - timeStart) + "ms" );

            if ( checkIfPassed()) {
	        status.addStatus("Looks it is passed " , status.PASS);
            } else {
	        status.addStatus("Looks it is passed " , status.FAIL);
            }

            status.printSummary("rosterAppID");
            System.exit(0);
	    
        } catch (Exception ex) {
            System.err.println("Caught an exception in main: " + ex.toString());
            ex.printStackTrace();
        }
    }
    
    private static void doTest(String threadId) throws Exception{
            Context initial = new InitialContext();
            Object objref = initial.lookup("java:comp/env/ejb/SimpleEnroller");

            EnrollerHome home = 
                (EnrollerHome)PortableRemoteObject.narrow(objref, 
                                                        EnrollerHome.class);

            Enroller e = home.create();

            for (int i=0; i <MAXCYCLESPERTHREAD; i++) {
                System.out.println("Result: " + e.doTest(threadId));
            }       

	    e.remove();

    }

    private static boolean checkIfPassed() throws Exception{
            Context initial = new InitialContext();
            Object objref = initial.lookup("java:comp/env/ejb/SimpleEnroller");

            EnrollerHome home = 
                (EnrollerHome)PortableRemoteObject.narrow(objref, 
                                                        EnrollerHome.class);

            Enroller e = home.create();

            int result = e.verifyTest();
            System.out.println("Expected Result : 0"  );
            System.out.println("Actual Result : " + result);

	    e.remove();

            if (result == 0) {
               return true;
            } else {
               return false;
            }
    }
}
