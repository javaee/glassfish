/*
 * Copyright 2002 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */

package com.sun.s1asdev.ejb.ejb30.interceptors.session;

import java.util.List;
import java.util.ArrayList;

import javax.interceptor.InvocationContext;
import javax.interceptor.AroundInvoke;
import javax.annotation.PostConstruct;

public class OverridingMethodsInterceptor
	extends BaseLevel2Interceptor {

    protected static final String OVERRIDING_INTERCEPTOR_NAME = "OverridingInterceptor";

    protected int aiCount = 0;
    protected int pcCount = 0;


    @PostConstruct
    protected void overridablePostConstructMethod(InvocationContext ctx)
    	throws RuntimeException {
        postConstructList.add(OVERRIDING_INTERCEPTOR_NAME);
        pcCount++;
        try {
            ctx.proceed();
        } catch(RuntimeException e) {
            throw e;
        } catch(Exception e) {
            throw new RuntimeException(e);
        }
    }


    @AroundInvoke
    protected Object overridableAroundInvokeMethod(InvocationContext ctx)
    	throws Throwable
    {
	aroundInvokeList.add(OVERRIDING_INTERCEPTOR_NAME);
        aiCount++;
        return ctx.proceed();
    }

    protected boolean isAICountOK() {
		return (0 == baseLevel2AICount)
			&& (aiCount == baseAICount)
			&& checkForCorrectSequence(aroundInvokeList);
	}

	protected boolean isPostConstructCallCounOK() {
		return (pcCount == basePCCount)
			&& (baseLevel2PCCount == 0)
			&& checkForCorrectSequence(postConstructList);
	}

    private boolean checkForCorrectSequence(List<String> list) {
	boolean result = list.size() == 2;
	if (result) {
	    BASE_INTERCEPTOR_NAME.equals(list.get(0));
	    OVERRIDING_INTERCEPTOR_NAME.equals(list.get(1));
	}
	for(String str : list) {
	    System.out.println("**OVERRIDING_INTERCEPTOR_TEST**: " + str);
	}
	System.out.println("**OVERRIDING_INTERCEPTOR_TEST**: " + result);


	return result;
    }


    String getName() {
       return OverridingMethodsInterceptor.class.getName();
    }
}
