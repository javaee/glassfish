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

@Interceptors({
	com.sun.s1asdev.ejb.ejb30.interceptors.session.OverridingMethodsInterceptor.class,
	com.sun.s1asdev.ejb.ejb30.interceptors.session.DistinctMethodsInterceptor.class
})

@Stateless
public class DummyEJB
	extends DummyLevel2EJB
	implements Dummy
{
    int interceptorId;
    private boolean createCalled = false;
    private int beanAICount = 0;
    private int dummyEJBPostConstructCount = 0;

    DistinctMethodsInterceptor distinctInterceptor = null;
    OverridingMethodsInterceptor overridingInterceptor = null;

    private static final String distinctAIName = "com.sun.s1asdev.ejb.ejb30.interceptors.session.DistinctMethodsInterceptor";
    private static final String overridingAIName = "com.sun.s1asdev.ejb.ejb30.interceptors.session.OverridingMethodsInterceptor";

    @PostConstruct
    protected void overridablePostConstruct()
    {
	postConstructList.add("DummyEJB");
        createCalled = true;
        dummyEJBPostConstructCount++;
    }

    public String dummy() {
	    return "Dummy!!";
    }

    public void setInterceptorId(int val) {
        if( !createCalled ) {
            throw new EJBException("create was not called");
        }

        this.interceptorId = val;
    }

    public String isInterceptorCallCounOK() {

		boolean beanAIResult = (beanAICount == dummyBaseAICount)
			&& (beanAICount == dummyLevel2AICount)
			&& checkAroundInvokeSequence();

		return "" + distinctInterceptor.isAICountOK()
			+ " " + overridingInterceptor.isAICountOK()
			+ " " + beanAIResult
			+ " " +  checkAroundInvokeSequence();
	}


    public String isPostConstructCallCounOK() {

		boolean beanPCCountResult = (dummyEJBPostConstructCount == dummyBaseEJBPostConstructCount)
			&& (dummyLevel2EJBPostConstructCount == 0)
			&& checkPostConstructSequence();

		return "" + distinctInterceptor.isPostConstructCallCounOK()
			+ " " + overridingInterceptor.isPostConstructCallCounOK()
			+ " " + beanPCCountResult
			+ " " +  checkPostConstructSequence();
	}

    @AroundInvoke
    Object myOwnAroundInvoke(InvocationContext ctx)
        throws Exception {
		aroundInvokeList.add("DummyEJB");
		if (distinctInterceptor == null) {
			 distinctInterceptor = (DistinctMethodsInterceptor)
			 	ctx.getContextData().get(distinctAIName);
			 overridingInterceptor = (OverridingMethodsInterceptor)
			 	ctx.getContextData().get(overridingAIName);
		}

		beanAICount++;
        return ctx.proceed();
    }

    private boolean checkPostConstructSequence() {
	boolean result = postConstructList.size() == 2;
	if (result) {
	    "DummyBaseEJB".equals(postConstructList.get(0));
	    "DummyEJB".equals(postConstructList.get(1));
	}

	return result;
    }

    private boolean checkAroundInvokeSequence() {
	boolean result = aroundInvokeList.size() == 3;
	if (result) {
	    "DummyBaseEJB".equals(aroundInvokeList.get(0));
	    "DummyLevel2EJB".equals(aroundInvokeList.get(1));
	    "DummyEJB".equals(aroundInvokeList.get(2));
	}

	return result;
    }

}

