/*
 * Copyright 2002 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.sun.s1asdev.ejb.ejb31.aroundtimeout;


import javax.ejb.Stateless;
import javax.ejb.Schedule;
import javax.ejb.EJBException;
import javax.interceptor.Interceptors;
import javax.interceptor.ExcludeDefaultInterceptors;
import javax.interceptor.ExcludeClassInterceptors;
import javax.interceptor.AroundTimeout;
import javax.interceptor.AroundInvoke;
import javax.interceptor.InvocationContext;

// No-interface view
@Stateless
@ExcludeDefaultInterceptors
@Interceptors({InterceptorD.class, InterceptorC.class})
public class SlessEJB7 
{

    private final static int EXPECTED = 1;
    private static String calls = "";

    // called as a timeout and through interface
    @Schedule(second="*", minute="*", hour="*", info="SlessEJB7-dc")
    public void dc() {}

    @AroundTimeout
    private Object aroundTimeout(InvocationContext ctx) throws Exception
    {
        String methodName = ctx.getMethod().getName();
        if (methodName.equals("verify")) {
            throw new EJBException("SlessEJB7:aroundTimeout is called for non-timeout method " + methodName);
        }
        Common.checkResults(ctx);
        Common.storeResult("SlessEJB7-dc");
        return null;
    }
    
    @AroundInvoke
    private Object aroundInvoke(InvocationContext ctx) throws Exception
    {
        String methodName = ctx.getMethod().getName();
        System.out.println("SlessEJB7:aroundInvoke called for " + methodName);

        return ctx.proceed();
    }
    
    public void verify() {
        Common.checkResults("SlessEJB7", EXPECTED);
    }
}
