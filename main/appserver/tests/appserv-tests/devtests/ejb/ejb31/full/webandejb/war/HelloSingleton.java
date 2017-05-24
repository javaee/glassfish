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
import java.util.concurrent.TimeUnit;

@Singleton
@EJB(name="helloStateful", beanInterface=HelloStateful.class)
@Lock(LockType.WRITE)
@AccessTimeout(value=10, unit=TimeUnit.SECONDS)
public class HelloSingleton extends Super1  {

    @EJB 
    Hello hello;

    @EJB
    HelloStateless slsb;

    @EJB
    private HelloSingleton2 sing2;

    @Resource 
    private SessionContext sesCtx;
    
    private HelloSingleton me;

    @PostConstruct    
    //@TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
    private void init() {
	System.out.println("HelloSingleton::init()");
	me = sesCtx.getBusinessObject(HelloSingleton.class);

	TimerService ts = sesCtx.getTimerService();
	ts.createTimer(2000, "");
	System.out.println("Created timer in HelloSingleton");

	slsb.hello();

	slsb.helloAsync();

	//throw new EJBException("force PostConstruct failure");
    }

    @Timeout
    @Lock(LockType.WRITE)
    public void timeout(Timer t) {
	System.out.println("In HelloSingleton::timeout");
    }

    public void callSing2WithTxAndRollback() {
	// Call Singleton 2 with a transaction
	// If it's initialized lazily as a side-effect of this
	// invocation, the fact that we rollback our transaction
	// should not impact any transactional work performed in
	// its CMT/TX_REQUIRED constructor.
	try {
	    // Executes in its own transaction but throws a runtime exception.
	    sing2.hello1();
	} catch(EJBException e) {
	    // Catch the runtime exception so it doesn't affect the client
	    // tx (yet)
	}

	// Make sure we can still call the singleton after a
	// runtime exception
	sing2.hello2();

	sesCtx.setRollbackOnly();

	System.out.println("Exiting HelloSingleton::callSing2WithTxAndRollback");
    }

    public void wait(int seconds) {
	try {
	    System.out.println("In HelloSingleton::wait. Sleeping with lock...");
	    Thread.sleep(seconds * 1000);
	    System.out.println("Awake in HelloSingleton::wait. Releasing lock...");
	} catch(Exception e) {
	    System.out.println(e);
	}
    }

    @Asynchronous
    public void asyncWait(int seconds) {
	me.wait(seconds);
    }

    @AccessTimeout(value=5, unit=TimeUnit.MILLISECONDS)
    public String hello() {
	
	System.out.println("get invoked interface = " + 
			   sesCtx.getInvokedBusinessInterface());

	HelloStateful sful = (HelloStateful) sesCtx.lookup("helloStateful");
	sful.hello();
	hello.foo();
	sful.goodbye();

	return "hello, world!\n";
    }

    @Lock(LockType.READ)
    public void read() {
	System.out.println("In HelloSingleton::read()");
    }

    public void write() {
	System.out.println("In HelloSingleton::write()");
    }

    @Lock(LockType.READ)
    public void reentrantReadRead() {
	System.out.println("In HelloSingleton::ReentrantReadRead()");
	me.read();
    }

    @Lock(LockType.READ)
    public void reentrantReadWrite() {
	System.out.println("In HelloSingleton::ReentrantReadWrite()");
	try {
	    me.write();
	    throw new EJBException("Expected illegal loopback exception");
	} catch(IllegalLoopbackException ile) {
	    System.out.println("Successfully caused illegal loopback");
	}
    }

    public void reentrantWriteRead() {
	System.out.println("In HelloSingleton::ReentrantWriteRead()");
	me.read();
    }

    public void reentrantWriteWrite() {
	System.out.println("In HelloSingleton::ReentrantWriteWrite()");
	me.write();
    }

    @AccessTimeout(0)
    public void testNoWait() {
	System.out.println("In HelloSingleton::testNoWait");
    }

    @PreDestroy
    @TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
    private void destroy() {
	System.out.println("HelloSingleton::destroy()");
    }

}



 
/*
    @Lock(LockType.WRITE)
  class Super2 {

	@Lock(LockType.READ)
	public void super2() {}

	public void super22() {}

    }
*/
