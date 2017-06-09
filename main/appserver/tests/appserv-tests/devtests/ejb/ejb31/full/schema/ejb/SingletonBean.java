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

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

import java.util.concurrent.TimeUnit;

import javax.annotation.*;

@Startup
@Singleton
@LocalBean
public class SingletonBean implements RemoteSingleton {


    @EJB 
    private StatefulBean sf;

    @Resource
    private SessionContext ctx;

    @EJB
    private SingletonBean me;

    private boolean gotTimeout;

    @PostConstruct
    private void init() {
	System.out.println("In SingletonBean::init()");

	// Call a method marked as async in .xml.  This would block
	// if it's not correctly identified as an async method since
	// we're not out of PostConstruct yet.
	me.foo();

	SingletonBean2 sb2 = (SingletonBean2) 
	    ctx.lookup("java:module/SingletonBean2");

	// Call read-lock async method and have it sleep for a little
	// while
	sb2.fooAsync(10);

	// Sleep for a short time to allow enough time for fooAsync() to
	// block
	try {
	    Thread.sleep(1000);
	} catch(Exception e) {
	    e.printStackTrace();
	}

	// Call a sychronous write-lock method with a .xml specified
	// timeout that's less than the fooAsync sleep time
	try {
	    sb2.foo();
	    throw new EJBException("Should have gotten timeout exception");
	} catch(ConcurrentAccessTimeoutException cate) {
	    System.out.println("Got expected timeout exception");
	}

	RemoteSingleton lookup1 = (RemoteSingleton)
	    ctx.lookup("java:comp/env/ejb/lookup1");
	System.out.println("lookup1 = " + lookup1);

	SingletonBean2 lookup2 = (SingletonBean2)
	    ctx.lookup("java:comp/env/ejb/lookup2");
	System.out.println("lookup2 = " + lookup2);

	// Now call a synchronous read-lock method.  This should just
	// proceed.
	sb2.foo2();

	sf.foo();

	System.out.println("Leaving SingletonBean::init()");
    }

    @AccessTimeout(value=10, unit=TimeUnit.DAYS)
    public void foo() {
	System.out.println("In SingletonBean::foo()");
    }

    @PreDestroy
    private void destroy() {
	System.out.println("In SingletonBean::destroy()");
    }

    @Schedule(second="1", minute="*", hour="*", persistent=false)
    private void mytimeout(Timer t) {
	System.out.println("In SingletonBean::mytimeout, info = " + t.getInfo() + "t = " + 
			   t.getSchedule());	

	try {
	    t.getHandle();
	    System.out.println("Successfully got handle for persistent timer");
	} catch(IllegalStateException e) {
	    throw new EJBException(e);
	}

	if( t.getInfo().equals("info") && t.isPersistent() && t.isCalendarTimer() ) {
	    gotTimeout = true;
	} else {
	    throw new EJBException("Timer metadata doesn't match descriptor " + t);
	}
    }

    private void mytimeout2() {
	System.out.println("In SingletonBean::mytimeout");
    }

    private void mytimeout1(Timer t) {
	System.out.println("In SingletonBean::mytimeout1");
    }

    public boolean getTestResult() {
	System.out.println("In SingletonBean::testPassed");
	return gotTimeout;
    }
}
