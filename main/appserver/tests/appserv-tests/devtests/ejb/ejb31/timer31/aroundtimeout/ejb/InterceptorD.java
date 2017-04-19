/*
 * Copyright 2002 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.sun.s1asdev.ejb.ejb31.aroundtimeout;

import javax.interceptor.AroundTimeout;
import javax.interceptor.AroundInvoke;
import javax.interceptor.InvocationContext;
import javax.annotation.PostConstruct;

public class InterceptorD {

    @PostConstruct
    private void postConstruct(InvocationContext ctx) {
        try {
            ctx.proceed();
        } catch(Exception e) {
            e.printStackTrace();
        }
    }

    @AroundTimeout
    @AroundInvoke
    Object aroundAll(InvocationContext ctx)
	        throws Exception {
        Common.aroundTimeoutCalled(ctx, "D");
        Object instance = ctx.getTarget();
        if( instance instanceof SlessEJB5 ) {
            ((SlessEJB5) instance).aroundAllCalled = true;
        }

        return ctx.proceed();
    }
   
}
