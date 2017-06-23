/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2001-2017 Oracle and/or its affiliates. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License.  You can
 * obtain a copy of the License at
 * https://oss.oracle.com/licenses/CDDL+GPL-1.1
 * or LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 *
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at LICENSE.txt.
 *
 * GPL Classpath Exception:
 * Oracle designates this particular file as subject to the "Classpath"
 * exception as provided by Oracle in the GPL Version 2 section of the License
 * file that accompanied this code.
 *
 * Modifications:
 * If applicable, add the following below the License Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyright [year] [name of copyright owner]"
 *
 * Contributor(s):
 * If you wish your version of this file to be governed by only the CDDL or
 * only the GPL Version 2, indicate your decision by adding "[Contributor]
 * elects to include this software in this distribution under the [CDDL or GPL
 * Version 2] license."  If you don't indicate a single choice of license, a
 * recipient has the option to distribute your version of this file under
 * either the CDDL, the GPL Version 2 or to extend the choice of license to
 * its licensees as provided above.  However, if you add GPL Version 2 code
 * and therefore, elected the GPL Version 2 license, then the option applies
 * only if the new code is made subject to such option by the copyright
 * holder.
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
