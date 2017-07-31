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


public abstract class StatefulBeanSuper implements StatefulCncSuperIntf {

    private int count = 0;

    @AccessTimeout(-1)
     public String hello() {
	System.out.println("In StatefulBeanSuper::hello");
	return "hello, world!\n";
    }

    @AccessTimeout(value=100, unit=TimeUnit.NANOSECONDS)
    public String helloWait() {
	System.out.println("In StatefulBeanSuper::helloWait");
	return "hello, world!\n";
    }
    
    @Asynchronous
    public Future<String> helloAsync() {
	System.out.println("In StatefulBeanSuper::helloAsync");
	return new AsyncResult<String>("hello");
    }

    @Asynchronous
    public void fireAndForget() {
	System.out.println("In StatefulBeanSuper::fireAndForget");
    }

    @Asynchronous
    public void sleep(int seconds) {
	System.out.println("In StatefulBeanSuper::asyncSleep");
	try {
	    System.out.println("Sleeping for " + seconds + " seconds...");
	    Thread.sleep(seconds * 1000);
	    System.out.println("Woke up from sleep");
	} catch(Exception e) {
	    e.printStackTrace();
	    throw new EJBException(e);
	}
    }

    @Asynchronous
    @Remove
    public void sleepAndRemove(int seconds) {
	System.out.println("In StatefulBeanSuper::sleepAndRemove");
	try {
	    System.out.println("Sleeping for " + seconds + " seconds...");
	    Thread.sleep(seconds * 1000);
	    System.out.println("Woke up from sleep. I will now be removed...");
	} catch(Exception e) {
	    e.printStackTrace();
	    throw new EJBException(e);
	}
    }

    public void incrementCount(int seconds) {
	System.out.println("In StatefulBeanSuper::incrementCount(" + seconds + ")");
	// increment count but wait a bit to try force serialization
	sleep(seconds);
	count++;
	System.out.println("Count = " + count);
    }

    public int getCount() {
	return count;
    }
}
