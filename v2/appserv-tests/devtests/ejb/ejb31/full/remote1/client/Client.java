package com.acme;


import javax.ejb.*;
import javax.annotation.*;

import javax.naming.InitialContext;

import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

import com.sun.ejte.ccl.reporter.SimpleReporterAdapter;


public class Client {

    private static SimpleReporterAdapter stat = 
        new SimpleReporterAdapter("appserv-tests");

    private static String appName;

    public static void main(String args[]) {

	appName = args[0]; 
	stat.addDescription(appName);
	Client client = new Client(args);       
        client.doTest();	
        stat.printSummary(appName + "ID");
    }

    public Client(String[] args) {}

    public void doTest() {

	try {

	    /**
	    Object o= (HelloHome) new InitialContext().lookup("corbaname:iiop:localhost:3700#HH");
	    HelloHome helloHomeCorbaName = (HelloHome)
		javax.rmi.PortableRemoteObject.narrow(o, HelloHome.class);
	    callHome(helloHomeCorbaName);
	    */

	    HelloHome helloHome = (HelloHome) new InitialContext().lookup("java:global/" + appName + "/HelloBean!com.acme.HelloHome");
	    callHome(helloHome);

	    Hello hello = (Hello) new InitialContext().lookup("HH#com.acme.Hello");

	    // @@@ TODO System.out.println("Lookup via app-defined global name");
	    // @@@ Not implemented yet @@@ Hello hellog = (Hello) new InitialContext().lookup("java:global/HBGlobal");

	    Future<String> future = hello.helloAsync();	   
	    
	    // "memory leak" don't call get on async that returns a future
	    // At the least should be cleaned up at container shutdown but
	    // but possibly sooner
	    hello.helloAsync();
	    hello.helloAsync();
	    hello.helloAsync();

	    hello.fireAndForget();	   
	   
	    try {
		String result = future.get();
		System.out.println("helloAsync() says : " + result);
	    } catch(ExecutionException e) {
		System.out.println("Got async ExecutionException. Cause is " +
				   e.getCause().getMessage());
		e.getCause().printStackTrace();
	    }

	    Future<String> futureTO = hello.asyncBlock(5);	    
	    // Test timeout
	    try {

		String result = futureTO.get(1, TimeUnit.MILLISECONDS);
		throw new EJBException("Should have timed out");
	    } catch(TimeoutException toe) {
		try {
		    String result = futureTO.get();
		    System.out.println("asyncBlock() says : " + result);
		} catch(Exception e) {
		    throw new EJBException(e);
		}
	    } catch(ExecutionException e) {
		System.out.println("Got async ExecutionException. Cause is " +
				   e.getCause().getMessage());
		e.getCause().printStackTrace();
	    }
	 
	    Future<String> futureException = 
		hello.asyncThrowException("javax.ejb.ConcurrentAccessTimeoutException");
	    try {
		String result = futureException.get();
		throw new EJBException("Should have gotten exception");
	    } catch(ExecutionException e) {
		if( e.getCause() instanceof javax.ejb.ConcurrentAccessTimeoutException) {
		    System.out.println("Got correct async exception. Cause is " +
				       e.getCause().getMessage());
		} else {
		    throw new EJBException("wrong exception", e);
		}
	    }

	    Future<String> futureCancel = null;
	    try {
		futureCancel = hello.asyncCancel(5);
		futureCancel.cancel(true);
		futureCancel.get();	
		throw new EJBException("Should have gotten exception");
	    } catch(ExecutionException e) {
		if( e.getCause().getClass().getName().equals("java.lang.Exception")) {
		    System.out.println("Got correct async exception. Cause is " +
				       e.getCause().getMessage());
		} else {
		    throw new EJBException("wrong exception", e);
		}
	    }

	    try {
		hello.throwException("javax.ejb.ConcurrentAccessException");
		throw new EJBException("expected an exception");
	    } catch(ConcurrentAccessException cae) {
		System.out.println("Got expected " + cae);
	    }

	    try {
		hello.throwException("javax.ejb.ConcurrentAccessTimeoutException");
		throw new EJBException("expected an exception");
	    } catch(ConcurrentAccessTimeoutException cat) {
		System.out.println("Got expected " + cat);
	    }

	    try {
		hello.throwException("javax.ejb.IllegalLoopbackException");
		throw new EJBException("expected an exception");
	    } catch(IllegalLoopbackException ile) {
		System.out.println("Got expected " + ile);
	    }


	    // Fully-qualified portable global
	    HelloHome helloHome2 = (HelloHome) new InitialContext().lookup("java:global/" + appName + "/HelloBean!com.acme.HelloHome");
	    callHome(helloHome2);

	    Hello hello2 = (Hello) new InitialContext().lookup("java:global/" + appName + "/HelloBean!com.acme.Hello");
	    callBusHome(hello2);

	    // non-portable global
	    HelloHome helloHome5 = (HelloHome) new InitialContext().lookup("HH");
	    callHome(helloHome5);

	    Hello hello5 = (Hello) new InitialContext().lookup("HH#com.acme.Hello");
	    callBusHome(hello5);

	    stat.addStatus("local main", stat.PASS);

	} catch(Exception e) {
	    stat.addStatus("local main", stat.FAIL);
	    e.printStackTrace();
	}

    }

    private static void callHome(HelloHome home) throws Exception {
	//	HelloHome home = (HelloHome) PortableRemoteObject.narrow(obj, HelloHome.class);
	HelloRemote hr = home.create();
	System.out.println("2.x HelloRemote.hello() says " + hr.hello());
    }

    private static void callBusHome(Hello h) {
	System.out.println("Hello.hello() says " + h.hello());
    }


}
