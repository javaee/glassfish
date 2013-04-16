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

public class DistinctMethodsInterceptor extends BaseLevel2Interceptor {

    protected static final String DISTINCT_INTERCEPTOR_NAME = "DistinctInterceptor";

    protected int distinctAICount = 0;
    protected int distinctPCCount = 0;

    @PostConstruct
    private void distinctPostConstruct(InvocationContext ctx) throws RuntimeException {
	postConstructList.add(DISTINCT_INTERCEPTOR_NAME);
        distinctPCCount++;
        try {
            ctx.proceed();
        } catch(RuntimeException e) {
            throw e;
        } catch(Exception e) {
            throw new RuntimeException(e);
        }
    }

    @AroundInvoke
    private Object distinctMethodInterceptor(InvocationContext ctx)
            throws Throwable {
	aroundInvokeList.add(DISTINCT_INTERCEPTOR_NAME);
        distinctAICount++;
        return ctx.proceed();
    }

    protected boolean isAICountOK() {
        return (distinctAICount == baseAICount)
                && (distinctAICount == baseLevel2AICount)
			&& checkForCorrectSequence(aroundInvokeList);
	}

    protected boolean isPostConstructCallCounOK() {
        return (distinctPCCount == basePCCount)
                && (distinctPCCount == baseLevel2PCCount)
			&& checkForCorrectSequence(postConstructList);
    }

    private boolean checkForCorrectSequence(List<String> list) {
	boolean result = list.size() == 3;
	if (result) {
	    BASE_INTERCEPTOR_NAME.equals(list.get(0));
	    LEVEL2_INTERCEPTOR_NAME.equals(list.get(1));
	    DISTINCT_INTERCEPTOR_NAME.equals(list.get(2));
	}

	for(String str : list) {
	    System.out.println("**DISTINCT_INTERCEPTOR_TEST**: " + str);
	}
	System.out.println("**DISTINCT_INTERCEPTOR_TEST**: " + result);

	return result;
    }

    String getName() {
       return DistinctMethodsInterceptor.class.getName();
    }
}
