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
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicBoolean;


@Singleton
@ConcurrencyManagement(ConcurrencyManagementType.BEAN)
@LocalBean
@Remote(CacheRemote.class)
public class CacheBean {

    @EJB CacheBean me;
    @Resource SessionContext sesCtx;
    private AtomicInteger refreshCount = new AtomicInteger();

    private volatile boolean failure = false;
    private volatile boolean finishedInit = false;
    private String failureMsg;
    private volatile boolean shutdown = false;

       @PostConstruct
       private void init() {
              System.out.println("In Cache:init()");
	      me = sesCtx.getBusinessObject(CacheBean.class);
	      me.runAsync();
	      try {
		System.out.println("Blocking 2 secs in init");
		Thread.sleep(1500);
		System.out.println("Waking up from sleep in init...");
	    } catch(Exception e) {
		e.printStackTrace();
	    }
	      finishedInit = true;
       }

    @Asynchronous
    public void fooAsync() { return; }

    public int checkCache() {

	if( failure ) {
	    throw new EJBException(failureMsg);
	}

	int refreshes = refreshCount.intValue();
	if( refreshes == 0 ) {
	    throw new EJBException("no refreshes");
	}

	System.out.println("Successful cache check total refreshes = " + refreshes);
	return refreshes;
    }

       @Schedule(second="*/1", minute="*", hour="*", persistent=false)
       private void refresh() {
	   int count = refreshCount.incrementAndGet();
	   System.out.println("In Cache:refresh() num refreshes = " + count);
       }

       @PreDestroy
       private void destroy() {
	   shutdown = true;
              System.out.println("In Cache:destroy()");
       }

    @Asynchronous
    public void runAsync() {

	if( !finishedInit ) {
	    failure = true;
	    failureMsg = "Async called before init finished";
	}

	System.out.println("In Singleton::run()");

	while(!shutdown) {

	    try {
		System.out.println("Going to sleep...");
		Thread.sleep(5000);
		System.out.println("Waking up from sleep...");
	    } catch(Exception e) {
		e.printStackTrace();
	    }
	}

	System.out.println("Exiting Cache::run() due to shutdown");
	return;
    }
       

}
