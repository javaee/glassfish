/*
 * Copyright 2002 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.sun.s1asdev.ejb.ejb31.aroundtimeout;

import javax.interceptor.InvocationContext;
import javax.annotation.PostConstruct;

public class InterceptorB {

    @PostConstruct
    private void postConstruct(InvocationContext ctx) {
        try {
            ctx.proceed();
        } catch(Exception e) {
            e.printStackTrace();
        }
    }

    // around-timeout declared in ejb-jar.xml
    Object aroundTimeout(InvocationContext ctx)
	        throws Exception {
        Common.aroundTimeoutCalled(ctx, "B");
        return ctx.proceed();
    }
   
}
