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

package com.acme;

import javax.ejb.*;
import javax.annotation.*;

import javax.naming.InitialContext;

import java.util.concurrent.*;

import java.rmi.RemoteException;
import java.rmi.NoSuchObjectException;


import com.sun.ejte.ccl.reporter.SimpleReporterAdapter;

public class Client {

    private static SimpleReporterAdapter stat = 
        new SimpleReporterAdapter("appserv-tests");

    private static String appName;

    private RemoteAsync remoteAsync;

    private RemoteAsync2 statefulBean;
    private RemoteAsync3 statefulBeanLegacyRemote;
    private RemoteAsync3 statefulBeanLegacyRemote2;

    private int num;

    public static void main(String args[]) {

	appName = args[0]; 
	stat.addDescription(appName);
	Client client = new Client(args);       
        client.doTest();	
        stat.printSummary(appName + "ID");
    }

    public Client(String[] args) {
	num = Integer.valueOf(args[1]);
    }

    public void doTest() {

	try {

	    statefulBean = (RemoteAsync2) new InitialContext().lookup("java:global/" + appName + "/StatefulBean!com.acme.RemoteAsync2");
	    statefulBeanLegacyRemote = (RemoteAsync3) new InitialContext().lookup("java:global/" + appName + "/StatefulBean!com.acme.RemoteAsync3");
	    statefulBeanLegacyRemote2 = (RemoteAsync3) new InitialContext().lookup("java:global/" + appName + "/StatefulBean!com.acme.RemoteAsync3");

	    Future<String> futureSful = statefulBean.helloAsync();
	    System.out.println("Stateful bean says " + futureSful.get());

	    futureSful = statefulBean.removeAfterCalling();
	    System.out.println("Stateful bean removed status = " + futureSful.get());

	    boolean gotSfulException = false;
	    try {
		futureSful = statefulBean.helloAsync();    
	    } catch(NoSuchEJBException nsee) {
		System.out.println("Got nsee from helloAsync");
		gotSfulException = true;
	    }

	    try {
		if( !gotSfulException ) {
		    System.out.println("return value = " + futureSful.get());
		    throw new EJBException("Should have gotten exception");
		}
	    } catch(ExecutionException ee) {
		if( ee.getCause() instanceof NoSuchEJBException ) {
		    System.out.println("Successfully caught NoSuchEJBException when " +
				       "accessing sful bean asynchronously after removal");
		} else {
		    throw new EJBException("wrong exception during sfsb access after removal",
					   ee);
		}
	    }
	    
	    try {
		Future<String> f = statefulBeanLegacyRemote.throwException("javax.ejb.CreateException");
		String result = f.get();
		throw new EJBException("Didn't get CreateException");
	    } catch(ExecutionException ee) {
		if( ee.getCause() instanceof CreateException ) {
		    System.out.println("Successfully received CreateException");
		} else {
		    throw new EJBException("wrong exception received",
					   ee);
		}
	    }

	    try {
		Future<String> f = statefulBeanLegacyRemote.throwException("javax.ejb.EJBException");
		String result = f.get();
		throw new EJBException("Didn't get EJBException");
	    } catch(ExecutionException ee) {
		if( ee.getCause() instanceof RemoteException ) {
		    System.out.println("Successfully received RemoteException");
		} else {
		    throw new EJBException("wrong exception received",
					   ee);
		}
	    }

	    
	    try {
		Future<String> f = statefulBeanLegacyRemote2.removeAfterCalling();
		String result = f.get();
		System.out.println("result of removeAfterCalling = " + result);
	    } catch(ExecutionException ee) {
		throw new EJBException("got unexpected exception", ee);
	    }

	    try {
		Future<String> f = statefulBeanLegacyRemote2.helloAsync();
		String result = f.get();
		throw new EJBException("Didn't get RemoteException");
	    } catch(ExecutionException ee) {
		if( ee.getCause() instanceof NoSuchObjectException ) {
		    System.out.println("Successfully received RemoteException");
		} else {
		    throw new EJBException("wrong exception received",
					   ee);
		}
	    } catch(NoSuchObjectException nsoe) {
		System.out.println("Successfully received NoSuchObjectException");
	    }
	  
	    remoteAsync = (RemoteAsync) new InitialContext().lookup("java:global/" + appName + "/SingletonBean");
	    remoteAsync.startTest();

	    ExecutorService executor = Executors.newCachedThreadPool();

	    int numFireAndForgets = num;
	    for(int i = 0; i < numFireAndForgets; i++) {
		executor.execute( new FireAndForget() );
	    }

	    CancelAfterAlreadyDone cad = new CancelAfterAlreadyDone();
	    executor.execute( cad );

	    ProcessAsync pAsyncs[] = new ProcessAsync[num];
	    for(int i = 0; i < pAsyncs.length; i++) {

		if( ( i % 2 ) == 0 ) {
		    pAsyncs[i] = new ProcessAsync(i, 1, 3, false);
		} else {
		    pAsyncs[i] = new ProcessAsync(i, 1, 3, true);
		}

		executor.execute( pAsyncs[i] );
	    }

	    executor.shutdown();

	    executor.awaitTermination(15, TimeUnit.SECONDS);

	    if( !cad.success ) {
		throw new Exception("Cancel after already done failed");
	    }

	    for(int i = 0; i < pAsyncs.length; i++) {
		ProcessAsync pa = pAsyncs[i];
		if( !pa.success() ) {
		    throw new Exception(pa.failureMsg);
		}
	    }

	    int ffCount =  remoteAsync.getFireAndForgetCount();
	    System.out.println("FireAndForget count = " + ffCount + " expected = " + 
			       numFireAndForgets);
	    if( ffCount != numFireAndForgets) {
		throw new Exception("numFireAndForget mismatch");
	    }

	    stat.addStatus("local main", stat.PASS);

	} catch(Exception e) {
	    stat.addStatus("local main", stat.FAIL);
	    e.printStackTrace();
	}
    }

    class FireAndForget implements Runnable {
	public void run() {
	    remoteAsync.fireAndForget();
	}
    }

    class CancelAfterAlreadyDone implements Runnable {
	boolean success = false;
	public void run() {
	    try {
		// asyc method that returns immediately.
		// Sleep for a bit so that it's likely already
		// done by the time we call cancel.  This should
		// exercise the path that the result piggy-backs
		// on the return of the cancel call.
		Future<String> future = remoteAsync.helloAsync();
		Thread.sleep(2000);
		future.cancel(true);
		String result = future.get();
		System.out.println("cancel after done = " + result);
		success = true;
	    } catch(Exception e) {
		e.printStackTrace();
	    }
	}

    }

    class ProcessAsync implements Runnable {
	int id;
	int interval;
	int numIntervals;
	boolean cancel;

	boolean cancelled;
	boolean completed;

	String failureMsg;
	Throwable exception;

	public ProcessAsync(int i, int interval, int numIntervals, boolean cancel) {
	    this.id = i;
	    this.interval = interval;
	    this.numIntervals = numIntervals;
	    this.cancel = cancel;
	}
	public void run() {
	    try {
		Future<Integer> future = 
		    remoteAsync.processAsync(interval, numIntervals);
		if( cancel ) {
		    future.cancel(true);
		} 

		Integer result = future.get();

		System.out.println("ProcessAsync result : " + result);
		completed = true;

	    } catch(ExecutionException ee) {
		exception = ee.getCause();
		if( exception.getClass().getName().equals("java.lang.Exception") ) {
		    System.out.println("ProcessAsync succesfully cancelled");
		    cancelled = true;
		}
	    } catch(Exception e) {
		exception = e; 
	    }
	}

	public boolean success() {
	    boolean succeeded = true;
	    if (cancel && !cancelled) {
		succeeded = false;
		failureMsg = "pasync " + id + " was not cancelled successfully";
	    } else if( !cancel && !completed ) {
		succeeded = false;
		failureMsg = "pasync " + id + " did not complete successfully";
	    }

	    return succeeded;
	}

    }


}
