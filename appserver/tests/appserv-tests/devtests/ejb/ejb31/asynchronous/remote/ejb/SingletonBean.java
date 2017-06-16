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

import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

@Singleton
@Lock(LockType.READ)
public class SingletonBean implements RemoteAsync {

    @Resource
    private SessionContext sessionCtx;

    private AtomicInteger fireAndForgetCount = new AtomicInteger();

    private AtomicInteger processAsyncCount = new AtomicInteger();

    @PostConstruct
    public void init() {
        System.out.println("In SingletonBean::init()");

    }

    public void startTest() {
	System.out.println("in SingletonBean::startTest()");
	// reset state
	fireAndForgetCount = new AtomicInteger();
	return;
    }

    @Asynchronous
    public void fireAndForget() {
	System.out.println("In SingletonBean::fireAndForget()");
	fireAndForgetCount.incrementAndGet();
    }

    public int getFireAndForgetCount() {
	return fireAndForgetCount.get();
    }

    @Asynchronous
    public Future<String> helloAsync() {
	return new AsyncResult<String>("hello, world\n");
    }

    @Asynchronous
    public Future<Integer> processAsync(int sleepInterval, int numIntervals) 
       throws Exception {
	int me = processAsyncCount.incrementAndGet();
	boolean cancelled = false;
	int i = 0;
	while(i < numIntervals) {
	    i++;
	    if( sessionCtx.wasCancelCalled() ) {
		System.out.println("Cancelling processAsync " + me + " after " +
				   i + " intervals");
		cancelled = true;
		break;
	    }
	    try {
		System.out.println("Sleeping for " + i + "th time in processAsync " +
				   me);
		Thread.sleep(sleepInterval * 1000);
		System.out.println("Woke up for " + i + "th time in processAsync " +
				   me);
	    } catch(Exception e) {
		e.printStackTrace();
		throw new EJBException(e);
	    }
	}
	
	if( cancelled ) {
	    throw new Exception("Cancelled processAsync " + me);
	}

	return new AsyncResult<Integer>(numIntervals);
    }

    @PreDestroy
    public void destroy() {
        System.out.println("In SingletonBean::destroy()");
    }



}
