/*
 * Copyright 2002 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.sun.s1asdev.ejb.ejb31.aroundtimeout;


import javax.ejb.Stateless;
import javax.ejb.Schedule;
import javax.ejb.Timer;
import javax.ejb.EJBException;
import javax.interceptor.Interceptors;
import javax.interceptor.ExcludeDefaultInterceptors;
import javax.interceptor.ExcludeClassInterceptors;
import javax.interceptor.InvocationContext;


// Define some aroundtimeout via annotations and some via
// ejb-jar.xml.  Ejb-jar.xml bindings are additive and
// ordered after aroundtimeout at same level declared via
// annotations.
@Stateless
@Interceptors({InterceptorD.class})
public class SlessEJB5 implements Sless5
{
    private volatile static boolean aroundTimeoutCalled = false;
    boolean aroundAllCalled = false;

    private final static int EXPECTED = 3;

    // Called as a timeout and through interface. InderceptorD will 
    // set aroundAllCalled to true either way.
    @Schedule(second="*", minute="*", hour="*", info="SlessEJB5-abdc")
    public void abdc() {
        System.out.println("in SlessEJB5:abdc().  aroundTimeoutCalled = " + 
                           aroundTimeoutCalled);

        // a little extra checking to make sure AroundTimeout is invoked...
        if( !aroundTimeoutCalled ) {
            throw new EJBException("bean class aroundTimeout not called");
        }
        // Enough if it is set once to true. Otherwise a timer can be executed 
        // between verify() and direct call to this method through the interface
        // aroundTimeoutCalled = false;

        if( !aroundAllCalled ) {
            throw new EJBException("InderceptorD aroundAll not called");
        }
        aroundAllCalled = false;
    }

    // Interceptor E added at method level within ejb-jar.xml
    @ExcludeDefaultInterceptors
    @Interceptors({InterceptorF.class})
    @Schedule(second="*", minute="*", hour="*", info="SlessEJB5-dcfe")
    public void dcfe() {}

    @ExcludeClassInterceptors
    @ExcludeDefaultInterceptors
    @Schedule(second="*", minute="*", hour="*", info="SlessEJB5-nothing")
    public void nothing() {}

    // declared in ejb-jar.xml
    private Object aroundTimeout(InvocationContext ctx)
    {
        System.out.println("In SlessEJB5:aroundTimeout");
        aroundTimeoutCalled = true;
        Common.checkResults(ctx);
        try {
            return ctx.proceed();
        } catch(Exception e) {
            throw new EJBException(e);
        }
    }
    
    public void verify() {
        Common.checkResults("SlessEJB5", EXPECTED);
        aroundTimeoutCalled = true;
    }
}
    

