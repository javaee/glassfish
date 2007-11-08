/*
 * Copyright 2002 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.sun.s1asdev.ejb.ejb30.interceptors.bindings;


import javax.ejb.Stateful;
import javax.interceptor.Interceptors;
import javax.interceptor.ExcludeDefaultInterceptors;
import javax.interceptor.ExcludeClassInterceptors;
import javax.interceptor.AroundInvoke;
import javax.interceptor.InvocationContext;
import javax.ejb.EJBException;

@Stateful
@Interceptors({InterceptorC.class, InterceptorD.class})
public class SfulEJB implements Sful
{

    private boolean aroundInvokeCalled = false;
    private boolean postConstructCalled = false;

    // postConstruct declared in ejb-jar.xml
    private void postConstruct() {
        System.out.println("In SfulEJB:postConstruct");
        postConstructCalled = true;
    }

    @ExcludeDefaultInterceptors
    public void cd() {
        System.out.println("in SfulEJB:cd().  postConstruct = " + 
                           postConstructCalled);

        if( !postConstructCalled ) {
            throw new EJBException("postConstruct wasn't called");
        }

        // a little extra checking to make sure aroundInvoke is invoked...
        if( !aroundInvokeCalled ) {
            throw new EJBException("bean class aroundInvoke not called");
        }
        aroundInvokeCalled = false;
    }

    @ExcludeClassInterceptors
    public void ab() {}

    public void abcd() {}

    @ExcludeClassInterceptors
    @Interceptors({InterceptorE.class, InterceptorF.class})
    public void abef() {}

    @ExcludeDefaultInterceptors
    @ExcludeClassInterceptors
    @Interceptors({InterceptorE.class, InterceptorF.class})
    public void ef() {}

    @ExcludeDefaultInterceptors
    @ExcludeClassInterceptors
    @Interceptors({InterceptorC.class, InterceptorE.class, InterceptorF.class})
    public void cef() {}

    @ExcludeDefaultInterceptors
    @ExcludeClassInterceptors
    @Interceptors({InterceptorC.class, InterceptorE.class, InterceptorF.class, InterceptorA.class})
    public void cefa() {}

    @ExcludeDefaultInterceptors
    @Interceptors({InterceptorE.class, InterceptorF.class})
    public void cdef() {}
    
    @ExcludeDefaultInterceptors
    @ExcludeClassInterceptors
    public void nothing() {}

    @Interceptors({InterceptorE.class, InterceptorF.class})    
    public void abcdef() {}

    // total ordering overridden in deployment descriptor
    @Interceptors({InterceptorE.class, InterceptorF.class})    
    public void acbdfe() {}

    // declared in ejb-jar.xml
    private Object aroundInvoke(InvocationContext ctx)
    {
        System.out.println("In SfulEJB:aroundInvoke");
        aroundInvokeCalled = true;
        Common.checkResults(ctx);
        try {
            return ctx.proceed();
        } catch(Exception e) {
            throw new EJBException(e);
        }
    }
    
}
