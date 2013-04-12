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

@Stateless
// total ordering DCBA expressed in .xml
@Interceptors({InterceptorC.class, InterceptorD.class})
public class SlessEJB3 implements Sless3
{

    private final static int EXPECTED = 10;

    @ExcludeDefaultInterceptors
    @Schedule(second="*", minute="*", hour="*", info="SlessEJB3-dc")
    private void dc() {}

    @ExcludeClassInterceptors
    @Schedule(second="*", minute="*", hour="*", info="SlessEJB3-ba")
    protected void ba() {}

    @Schedule(second="*", minute="*", hour="*", info="SlessEJB3-dcba")
    void dcba() {}

    @ExcludeClassInterceptors
    @Interceptors({InterceptorE.class, InterceptorF.class})
    @Schedule(second="*", minute="*", hour="*", info="SlessEJB3-baef")
    private void baef() {}

    @ExcludeDefaultInterceptors
    @ExcludeClassInterceptors
    @Interceptors({InterceptorE.class, InterceptorF.class})
    @Schedule(second="*", minute="*", hour="*", info="SlessEJB3-ef")
    private void ef() {}

    @ExcludeDefaultInterceptors
    @Interceptors({InterceptorE.class, InterceptorF.class})
    @Schedule(second="*", minute="*", hour="*", info="SlessEJB3-dcef")
    private void dcef() {}
    
    @ExcludeDefaultInterceptors
    @ExcludeClassInterceptors
    @Schedule(second="*", minute="*", hour="*", info="SlessEJB3-nothing")
    private void nothing() {}

    @Interceptors({InterceptorE.class, InterceptorF.class})    
    @Schedule(second="*", minute="*", hour="*", info="SlessEJB3-dcbaef")
    private void dcbaef() {}

    // total ordering overridden in deployment descriptor
    @Interceptors({InterceptorE.class, InterceptorF.class})    
    @Schedule(second="*", minute="*", hour="*", info="SlessEJB3-abcdef")
    private void abcdef() {}

    // binding described in deployment descriptor
    @Schedule(second="*", minute="*", hour="*", info="SlessEJB3-dcf")
    private void dcf() {}

    // called only through interface
    @Interceptors({InterceptorA.class, InterceptorG.class})
    public void noaroundtimeout() {}

    @AroundTimeout
    private Object aroundTimeout(InvocationContext ctx) throws Exception
    {
        // Common will verify that noaroundtimeout() is not called.
        Common.checkResults(ctx);
        return null;
    }
    
    @AroundInvoke
    private Object aroundInvoke(InvocationContext ctx) throws Exception
    {
        String methodName = ctx.getMethod().getName();
        if (!methodName.equals("noaroundtimeout") && !methodName.equals("verify")) {
            throw new EJBException("SlessEJB3:aroundInvoke is called for timeout method " + methodName);
        }
        return ctx.proceed();
    }
    
    public void verify() {
        Common.checkResults("SlessEJB3", EXPECTED);
    }
}
