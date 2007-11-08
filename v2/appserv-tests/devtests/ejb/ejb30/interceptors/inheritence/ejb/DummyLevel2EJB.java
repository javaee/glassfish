/*
 * Copyright 2002 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */

package com.sun.s1asdev.ejb.ejb30.interceptors.session;

import javax.ejb.Stateless;
import javax.interceptor.Interceptors;
import javax.interceptor.InvocationContext;
import javax.interceptor.AroundInvoke;
import javax.ejb.EJBException;
import javax.annotation.PostConstruct;


public class DummyLevel2EJB
	extends DummyBaseEJB
{
    protected int dummyLevel2AICount = 0;
	protected int dummyLevel2EJBPostConstructCount = 0;


	@PostConstruct
    protected void overridablePostConstruct() {
		postConstructList.add("DummyLevel2EJB");
		dummyLevel2EJBPostConstructCount++;
		System.out.println("GGGG: DummyLevel2EJB.postConstruct ");
	}

	@AroundInvoke
	protected Object overridableAroundInvoke(InvocationContext ctx)
		throws Exception {
			aroundInvokeList.add("DummyLevel2EJB");
			dummyLevel2AICount++;
			return ctx.proceed();
	}
}
