/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2017 Oracle and/or its affiliates. All rights reserved.
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

package com.sun.s1asdev.ejb.ejb30.clientview.exceptions.client;

import java.io.*;
import java.util.*;
import javax.ejb.*;
import java.rmi.RemoteException;
import javax.transaction.TransactionRequiredException;
import com.sun.s1asdev.ejb.ejb30.clientview.exceptions.*;
import com.sun.ejte.ccl.reporter.SimpleReporterAdapter;
import javax.naming.InitialContext;

public class Client {

    private static SimpleReporterAdapter stat = 
        new SimpleReporterAdapter("appserv-tests");

    @EJB static private Hello hr;

    @EJB static protected SlessRemoteBusiness2 slessRemoteBusiness2;    
    @EJB static public SfulRemoteBusiness2 sfulRemoteBusiness2;

    @EJB static public SlessRemoteBusiness slessRemoteBusiness;
    @EJB static public SfulRemoteBusiness sfulRemoteBusiness;

    public static void main (String[] args) {

        stat.addDescription("ejb-ejb30-clientview-exceptions");
        Client client = new Client(args);
        client.doTest();
        stat.printSummary("ejb-ejb30-clientview-exceptionsID");
    }  
    
    public Client (String[] args) {
    }
    
    public void doTest() {

        try {

	    if( hr == null ) {

		System.out.println("In stand-alone mode");
		InitialContext ic = new InitialContext();
		hr = (Hello) ic.lookup("ejb/ejb_ejb30_clientview_exceptions_Hello");
		sfulRemoteBusiness2 = (SfulRemoteBusiness2) ic.lookup("ejb/ejb_ejb30_clientview_exceptions_Sful#com.sun.s1asdev.ejb.ejb30.clientview.exceptions.SfulRemoteBusiness2");
		slessRemoteBusiness2 = (SlessRemoteBusiness2) ic.lookup("ejb/ejb_ejb30_clientview_exceptions_Sless#com.sun.s1asdev.ejb.ejb30.clientview.exceptions.SlessRemoteBusiness2");
		sfulRemoteBusiness = (SfulRemoteBusiness) ic.lookup("ejb/ejb_ejb30_clientview_exceptions_Sful#com.sun.s1asdev.ejb.ejb30.clientview.exceptions.SfulRemoteBusiness");
		slessRemoteBusiness = (SlessRemoteBusiness) ic.lookup("ejb/ejb_ejb30_clientview_exceptions_Sless#com.sun.s1asdev.ejb.ejb30.clientview.exceptions.SlessRemoteBusiness");

	    }

	    // TODO enable when             hr.runAccessDeniedExceptionTest();

            hr.runTxRequiredTest();

            hr.runTxRolledbackTest();

            hr.runNoSuchObjectTest();

            hr.runRollbackAppExceptionTest();

            hr.runAppExceptionTest();

            runTxRequiredTest();

            stat.addStatus("local main", stat.PASS);

        } catch(Exception e) {
            e.printStackTrace();
            stat.addStatus("local main" , stat.FAIL);
        }

        runConcurrentAccessTest();

    	return;
    }

    private void runTxRequiredTest() {

        int numEx = 0;

        try {
            sfulRemoteBusiness.forceTransactionRequiredException();
        } catch(EJBTransactionRequiredException txre) {
            numEx++;
        } 
        
        try {
            slessRemoteBusiness.forceTransactionRequiredException();
        } catch(EJBTransactionRequiredException txre) {
            numEx++;            
        } 

        try {
            sfulRemoteBusiness2.forceTransactionRequiredException();
        } catch(TransactionRequiredException txre) {
            numEx++;
        } catch(RemoteException e) {}
        
        try {
            slessRemoteBusiness2.forceTransactionRequiredException();
        } catch(TransactionRequiredException txre) {
            numEx++;
        } catch(RemoteException e) {}
        
        if( numEx != 4 ) {
            throw new RuntimeException("Didn't receive all expected " +
                                       "exceptions : " + numEx);
                                       
        } else {
            System.out.println("PASS : runTxRequiredTest");
        }


    }

    public void runConcurrentAccessTest() {
	    try {
		MyThread thread = new MyThread(sfulRemoteBusiness);
		thread.start();

		sleepFor(5);
		sfulRemoteBusiness.ping();
		stat.addStatus("local concurrentAccess[1.1]", stat.FAIL);
	    } catch (javax.ejb.ConcurrentAccessException conEx) {
		stat.addStatus("local concurrentAccess[1.1]", stat.PASS);
	    } catch (Throwable th) {
		System.out.println("Argggggg: Got: " + th);
		stat.addStatus("local concurrentAccess[1.2]", stat.FAIL);
	    }

	    try {
		MyThread2 thread = new MyThread2(sfulRemoteBusiness2);
		thread.start();

		sleepFor(5);
		sfulRemoteBusiness2.pingRemote();
		stat.addStatus("local concurrentAccess[2.1]", stat.FAIL);
	    } catch (java.rmi.RemoteException remEx) {
		stat.addStatus("local concurrentAccess[2.1]", stat.PASS);
	    } catch (Throwable th) {
		stat.addStatus("local concurrentAccess[2.2] Got: " + th, stat.FAIL);
	    }
    }

    class MyThread extends Thread {
	SfulRemoteBusiness sfulBusiness;

	MyThread(SfulRemoteBusiness sfulBusiness) {
	    this.sfulBusiness = sfulBusiness;
	}

	public void run() {
	    try {
		sfulBusiness.sleepFor(20);
	    } catch (Throwable th) {
		throw new RuntimeException("Could not invoke waitfor() method");
	    }
	}
    }


    class MyThread2 extends Thread {
	SfulRemoteBusiness2 sfulBusiness2;

	MyThread2(SfulRemoteBusiness2 sfulBusiness2) {
	    this.sfulBusiness2 = sfulBusiness2;
	}

	public void run() {
	    try {
		sfulBusiness2.sleepFor(20);
	    } catch (Throwable th) {
		throw new RuntimeException("Could not invoke waitfor() method");
	    }
	}
    }

    private void sleepFor(int sec) {
	try {
	    for (int i=0 ; i<sec; i++) {
		Thread.currentThread().sleep(1000);
		System.out.println("[" + i + "/" + sec + "]: Sleeping....");
	    }
	} catch (Exception ex) {
	}
    }

}

