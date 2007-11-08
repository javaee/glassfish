/*
 * Copyright 2002 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */

package com.sun.s1asdev.ejb.ejb30.interceptors.session;

import java.util.List;
import java.util.ArrayList;

import javax.ejb.Stateless;
import javax.interceptor.Interceptors;
import javax.interceptor.InvocationContext;
import javax.interceptor.AroundInvoke;
import javax.ejb.EJBException;
import javax.annotation.PostConstruct;

public abstract class DummyBaseEJB
{
    protected List<String> postConstructList = new ArrayList<String>();
    protected List<String> aroundInvokeList = new ArrayList<String>();

    protected int dummyBaseAICount = 0;
    protected int dummyBaseEJBPostConstructCount = 0;

	@AroundInvoke
	private Object dummyBaseAroundInvoke(InvocationContext ctx)
		throws Exception {
			aroundInvokeList = new ArrayList<String>();
			aroundInvokeList.add("DummyBaseEJB");
			dummyBaseAICount++;
			return ctx.proceed();
	}


	@PostConstruct
    private void dummyBasePostConstruct() {
		postConstructList = new ArrayList<String>();
		postConstructList.add("DummyBaseEJB");
		dummyBaseEJBPostConstructCount++;
		System.out.println("GGGG: DummyLevel2EJB.postConstruct ");
	}

}
