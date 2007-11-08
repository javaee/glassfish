/*
 * Copyright 2002 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.sun.s1asdev.ejb.ejb30.interceptors.bindings;

import javax.ejb.Stateful;
import javax.interceptor.AroundInvoke;
import javax.ejb.EJBException;
import javax.interceptor.InvocationContext;
import javax.interceptor.Interceptors;
import javax.interceptor.ExcludeDefaultInterceptors;
import javax.interceptor.ExcludeClassInterceptors;

@Stateful
@Interceptors({InterceptorC.class, InterceptorD.class})
@ExcludeDefaultInterceptors
public class SfulEJB2 implements Sful2
{
    private boolean aroundInvokeCalled = false;

    public void cd() {
        System.out.println("in SfulEJB:cd().  aroundInvokeCalled = " + 
                           aroundInvokeCalled);

        // a little extra checking to make sure aroundInvoke is invoked...
        if( !aroundInvokeCalled ) {
            throw new EJBException("bean class aroundInvoke not called");
        }
        aroundInvokeCalled = false;
    }

    @ExcludeClassInterceptors
    @Interceptors({InterceptorE.class, InterceptorF.class})
    public void ef() {}

    // explicitly add default interceptors that were disabled at
    // class-level.
    @Interceptors({InterceptorA.class, InterceptorB.class,
                   InterceptorE.class, InterceptorF.class})
    @ExcludeClassInterceptors
    public void abef(int i) {}

    // @ExcludeDefaultInterceptors is a no-op here since it 
    // was already excluded at class-level
    @ExcludeDefaultInterceptors
    @Interceptors({InterceptorE.class, InterceptorF.class})
    public void cdef() {}
    
    @ExcludeClassInterceptors
    public void nothing() {}

    // declared in ejb-jar.xml
    private Object aroundInvoke(InvocationContext ctx)
    {
        System.out.println("In SfulEJB2:aroundInvoke");
        aroundInvokeCalled = true;
        Common.checkResults(ctx);
        try {
            return ctx.proceed();
        } catch(Exception e) {
            throw new EJBException(e);
        }
    }
    
}
