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
