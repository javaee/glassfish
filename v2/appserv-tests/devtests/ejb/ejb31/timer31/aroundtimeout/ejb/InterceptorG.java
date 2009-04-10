/*
 * Copyright 2002 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.sun.s1asdev.ejb.ejb31.aroundtimeout;

import javax.interceptor.AroundTimeout;
import javax.interceptor.AroundInvoke;
import javax.interceptor.InvocationContext;
import javax.annotation.PostConstruct;

public class InterceptorG {

    @PostConstruct
    private void postConstruct(InvocationContext ctx) {
        try {
            ctx.proceed();
        } catch(Exception e) {
            e.printStackTrace();
        }
    }

    @AroundTimeout
    Object aroundTimeout(InvocationContext ctx)
	        throws Exception {
        Common.aroundTimeoutCalled(ctx, "G");
        Object instance = ctx.getTarget();
        if( instance instanceof SlessEJB6 ) {
            ((SlessEJB6) instance).aroundTimeoutCalled = true;
            ((SlessEJB6) instance).aroundInvokeCalled = false;
        }
        return ctx.proceed();
    }
   
    @AroundInvoke
    Object aroundInvoke(InvocationContext ctx)
	        throws Exception {
        Object instance = ctx.getTarget();
        if( instance instanceof SlessEJB6 ) {
            ((SlessEJB6) instance).aroundTimeoutCalled = false;
            ((SlessEJB6) instance).aroundInvokeCalled = true;
        }
        return ctx.proceed();
    }
   
}
