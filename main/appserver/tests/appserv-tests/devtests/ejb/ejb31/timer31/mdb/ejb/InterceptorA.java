/*
 * Copyright 2002 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.acme;

import javax.interceptor.AroundInvoke;
import javax.interceptor.AroundTimeout;
import javax.interceptor.InvocationContext;

public class InterceptorA {

    @AroundInvoke
    @AroundTimeout
    Object aroundTimeout(InvocationContext ctx) throws Exception {
        MessageBean bean = (MessageBean)ctx.getTarget();
        java.lang.reflect.Method m = ctx.getMethod();
        if (m != null)
            bean.mname = m.getName();
        return ctx.proceed();
    }
   
}
