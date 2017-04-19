/*
 * Copyright 2002 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.sun.s1asdev.ejb.ejb30.interceptors.bindings;


import javax.ejb.Stateless;
import javax.ejb.EJBException;
import javax.interceptor.Interceptors;
import javax.interceptor.ExcludeDefaultInterceptors;
import javax.interceptor.ExcludeClassInterceptors;
import javax.interceptor.AroundInvoke;
import javax.interceptor.InvocationContext;


// Exclude default interceptors, but re-add one of them at class-level
@Stateless
@ExcludeDefaultInterceptors
@Interceptors({InterceptorC.class, InterceptorB.class, InterceptorD.class})
public class SlessEJB4 implements Sless4
{
    private boolean aroundInvokeCalled = false;

    public void cbd() {
        System.out.println("in SlessEJB4:cbd().  aroundInvokeCalled = " + 
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
    @ExcludeClassInterceptors
    @Interceptors({InterceptorA.class, InterceptorB.class,
                   InterceptorE.class, InterceptorF.class})
    public void abef(int i) {}

    // @ExcludeDefaultInterceptors is a no-op here since it 
    // was already excluded at class-level
    @ExcludeDefaultInterceptors
    @Interceptors({InterceptorE.class, InterceptorF.class})
    public void cbdef() {}
    
    @ExcludeClassInterceptors
    public void nothing() {}

    // declared in ejb-jar.xml
    private Object aroundInvoke(InvocationContext ctx)
    {
        System.out.println("In SlessEJB4:aroundInvoke");
        aroundInvokeCalled = true;
        Common.checkResults(ctx);
        try {
            return ctx.proceed();
        } catch(Exception e) {
            throw new EJBException(e);
        }
    }
    
}
    

