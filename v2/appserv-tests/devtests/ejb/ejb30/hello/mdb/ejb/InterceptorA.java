/*
 * Copyright 2002 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.sun.s1asdev.ejb.ejb30.hello.mdb;

import javax.interceptor.AroundInvoke;
import javax.interceptor.InvocationContext;

public class InterceptorA {

    @AroundInvoke
    Object aroundTimeout(InvocationContext ctx) throws Exception {
        MessageBean bean = (MessageBean)ctx.getTarget();
        java.lang.reflect.Method m = ctx.getMethod();
        System.out.println("GGGG intercepting : " + m);
        if (m != null)
            bean.mname = m.getName();
        return ctx.proceed();
    }
   
}
