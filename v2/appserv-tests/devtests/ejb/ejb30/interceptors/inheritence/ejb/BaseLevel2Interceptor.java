/*
 * Copyright 2002 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */

package com.sun.s1asdev.ejb.ejb30.interceptors.session;

import javax.interceptor.InvocationContext;
import javax.interceptor.AroundInvoke;
import javax.annotation.PostConstruct;

public class BaseLevel2Interceptor
   extends BaseInterceptor {

    protected static final String LEVEL2_INTERCEPTOR_NAME = "Level2Interceptor";

    protected int baseLevel2AICount = 0;
    protected int baseLevel2PCCount = 0;

    @PostConstruct
    protected void overridablePostConstructMethod(InvocationContext ctx)
    	throws RuntimeException {
        postConstructList.add(LEVEL2_INTERCEPTOR_NAME);
        baseLevel2PCCount++;
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
    	throws Throwable {
	aroundInvokeList.add(LEVEL2_INTERCEPTOR_NAME);
        baseLevel2AICount++;
        return ctx.proceed();
    }

    String getName() {
       return BaseLevel2Interceptor.class.getName();
    }

}
