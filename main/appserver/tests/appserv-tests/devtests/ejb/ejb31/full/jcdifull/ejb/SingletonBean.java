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
import javax.interceptor.*;

import javax.inject.Inject;

import javax.naming.InitialContext;

import javax.enterprise.inject.spi.BeanManager;

import javax.enterprise.event.Event;

import java.lang.reflect.Method;

import org.jboss.weld.examples.translator.*;

@Singleton
@Startup
public class SingletonBean implements SingletonRemote {

    @Inject
    private Event<SomeEvent> someEvent;
    
    @Inject Foo foo;

    @EJB
	private StatelessLocal statelessEE;

    @Inject
	private StatelessLocal2 sl2;

    @Inject private TranslatorController tc;

    @Resource(lookup="java:module/FooManagedBean")
    private FooManagedBean fmb;

    @Resource
    private BeanManager beanManagerInject;
    
    @Resource SessionContext sesCtx;

    @PostConstruct
    public void init() {
        System.out.println("In SingletonBean::init()");
	if( beanManagerInject == null ) {
	    throw new EJBException("BeanManager is null");
	}
	System.out.println("Bean manager inject = " + beanManagerInject);
	testIMCreateDestroyMO();


	System.out.println("Sending some event...");
	someEvent.fire( new SomeEvent(2) );
    }

    public void hello() {
	System.out.println("In SingletonBean::hello() " + foo);
	statelessEE.hello();
	sl2.hello();

	fmb.hello();
	
	BeanManager beanMgr = (BeanManager)
	    sesCtx.lookup("java:comp/BeanManager");


	System.out.println("Successfully retrieved bean manager " +
			   beanMgr + " for JCDI enabled app");

	/**
	StatefulBean sb = (StatefulBean) sesCtx.lookup("java:module/StatefulBean");
	System.out.println("In SingletonBean sb = " + sb);
	sb.hello();
	**/

    }

    @Schedule(second="*/10", minute="*", hour="*")
	private void timeout() {
	System.out.println("In SingletonBean::timeout() " + foo);

	System.out.println("tc.getText() = " + tc.getText());

	statelessEE.hello();
    }

    @PreDestroy
    public void destroy() {
        System.out.println("In SingletonBean::destroy()");
    }

    private void testIMCreateDestroyMO() {

	try {

	    // Test InjectionManager managed bean functionality
	    Object injectionMgr = new InitialContext().lookup("com.sun.enterprise.container.common.spi.util.InjectionManager");
	    Method createManagedMethod = injectionMgr.getClass().getMethod("createManagedObject", java.lang.Class.class);
	    System.out.println("create managed object method = " + createManagedMethod);
	    FooManagedBean f2 = (FooManagedBean) createManagedMethod.invoke(injectionMgr, FooManagedBean.class);
	    f2.hello();
	

	    Method destroyManagedMethod = injectionMgr.getClass().getMethod("destroyManagedObject", java.lang.Object.class);
	    System.out.println("destroy managed object method = " + destroyManagedMethod);
	    destroyManagedMethod.invoke(injectionMgr, f2);

	     FooNonManagedBean nonF = (FooNonManagedBean) createManagedMethod.invoke(injectionMgr, FooNonManagedBean.class);
	     System.out.println("FooNonManagedBean = " + nonF);
	     nonF.hello();
	     destroyManagedMethod.invoke(injectionMgr, nonF);


	} catch(Exception e) {
	    throw new EJBException(e);
	}


    }



}
