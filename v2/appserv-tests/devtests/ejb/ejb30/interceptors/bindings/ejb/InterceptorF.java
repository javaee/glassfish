/*
 * Copyright 2002 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.sun.s1asdev.ejb.ejb30.interceptors.bindings;

import javax.interceptor.AroundInvoke;
import javax.interceptor.InvocationContext;
import javax.annotation.PostConstruct;

public class InterceptorF {

    @PostConstruct
    private void postConstruct(InvocationContext ctx) {
        try {
            ctx.proceed();
        } catch(Exception e) {
            e.printStackTrace();
        }
    }

    @AroundInvoke
    Object aroundInvoke(InvocationContext ctx)
	        throws Exception {
        Common.aroundInvokeCalled(ctx, "F");
        return ctx.proceed();
    }
   
}
