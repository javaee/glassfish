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


// Define some interceptors via annotations and some via
// ejb-jar.xml.  Ejb-jar.xml bindings are additive and
// ordered after interceptors at same level declared via
// annotations.
@Stateless
@Interceptors({InterceptorD.class})
public class SlessEJB5 implements Sless5
{
    private boolean aroundInvokeCalled = false;

    public void abdc() {
        System.out.println("in SlessEJB5:abdc().  aroundInvokeCalled = " + 
                           aroundInvokeCalled);

        // a little extra checking to make sure aroundInvoke is invoked...
        if( !aroundInvokeCalled ) {
            throw new EJBException("bean class aroundInvoke not called");
        }
        aroundInvokeCalled = false;
    }

    // Interceptor E added at method level within ejb-jar.xml
    @ExcludeDefaultInterceptors
    @Interceptors({InterceptorF.class})
    public void dcfe() {}

    @ExcludeClassInterceptors
    @ExcludeDefaultInterceptors
    public void nothing() {}

    // declared in ejb-jar.xml
    private Object aroundInvoke(InvocationContext ctx)
    {
        System.out.println("In SlessEJB5:aroundInvoke");
        aroundInvokeCalled = true;
        Common.checkResults(ctx);
        try {
            return ctx.proceed();
        } catch(Exception e) {
            throw new EJBException(e);
        }
    }
    
}
    

