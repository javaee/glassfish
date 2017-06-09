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
import javax.rmi.PortableRemoteObject;

import org.omg.CORBA.ORB;

import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

@Singleton
@Startup
public class SingletonBean implements SingletonRemote {
    @EJB(name="helloref", mappedName="HH#com.acme.Hello") Hello hello;
    @EJB(name="hellohomeref", mappedName="HH") HelloHome helloHome;

    @EJB(name="helloref2", lookup="HH#com.acme.Hello") Hello hello2;
    @EJB(name="hellohomeref2", lookup="HH") HelloHome helloHome2; 

    @EJB RemoteAsync statefulBean;

    @Resource
    private SessionContext sesCtx;

    @PostConstruct
    public void init() {
        System.out.println("In Remote SingletonBean::init()");
    }

    public void doTest(String targetAppName) {

	try {

	    Hello hGlobal = (Hello) new InitialContext().lookup("java:global/HBGlobal");
	    System.out.println("hGlobal = " + hGlobal);
	    System.out.println("hGlobal hello = " + hGlobal.hello());

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

	    String targetAppJndiPrefix = "java:global/" + targetAppName + "/";

	    HelloHome helloHomePGlobal = (HelloHome) new InitialContext().lookup(targetAppJndiPrefix + "HelloBean!com.acme.HelloHome");


	    ORB orb = (ORB) sesCtx.lookup("java:comp/ORB");
	    System.out.println("orb = " + orb);
	    ORB orb2 = (ORB) new InitialContext().lookup("java:comp/ORB");
	    System.out.println("orb2 = " + orb2);


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

	    // injected
	    callHome(helloHome);
	    callBusHome(hello);


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
	    HelloHome helloHome2 = (HelloHome) new InitialContext().lookup(targetAppJndiPrefix + "HelloBean!com.acme.HelloHome");
	    callHome(helloHome2);

	    Hello hello2 = (Hello) new InitialContext().lookup(targetAppJndiPrefix + "HelloBean!com.acme.Hello");
	    callBusHome(hello2);

	    // Fully-qualified portable global via sesion context
	    HelloHome helloHome3 = (HelloHome) sesCtx.lookup(targetAppJndiPrefix + "HelloBean!com.acme.HelloHome");
	    callHome(helloHome3);

	    Hello hello3 = (Hello) sesCtx.lookup(targetAppJndiPrefix + "HelloBean!com.acme.Hello");
	    callBusHome(hello3);

	    // ejb-ref
	    HelloHome helloHome4 = (HelloHome) sesCtx.lookup("java:comp/env/hellohomeref");
	    callHome(helloHome4);

	    Hello hello4 = (Hello) sesCtx.lookup("java:comp/env/helloref");
	    callBusHome(hello4);
	    
	    // non-portable global
	    HelloHome helloHome5 = (HelloHome) new InitialContext().lookup("HH");
	    callHome(helloHome5);

	    Hello hello5 = (Hello) new InitialContext().lookup("HH#com.acme.Hello");
	    callBusHome(hello5);


	} catch(Exception e) {
	    e.printStackTrace();
	    throw new EJBException("test failed", e);
	}

    }

    private void callHome(HelloHome home) throws Exception {
	//	HelloHome home = (HelloHome) PortableRemoteObject.narrow(obj, HelloHome.class);
	HelloRemote hr = home.create();
	System.out.println("2.x HelloRemote.hello() says " + hr.hello());
    }

    private void callBusHome(Hello h) {
	System.out.println("Hello.hello() says " + h.hello());
    }

    @PreDestroy
    public void destroy() {
        System.out.println("In SingletonBean::destroy()");
    }



}
