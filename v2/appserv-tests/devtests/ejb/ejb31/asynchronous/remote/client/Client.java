package com.acme;


import javax.ejb.*;
import javax.annotation.*;

import javax.naming.InitialContext;

import java.util.concurrent.*;


import com.sun.ejte.ccl.reporter.SimpleReporterAdapter;

public class Client {

    private static SimpleReporterAdapter stat = 
        new SimpleReporterAdapter("appserv-tests");

    private static String appName;

    private RemoteAsync remoteAsync;

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
