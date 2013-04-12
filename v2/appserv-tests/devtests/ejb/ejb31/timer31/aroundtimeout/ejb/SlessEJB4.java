/*
 * Copyright 2002 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.sun.s1asdev.ejb.ejb31.aroundtimeout;

import java.util.Collection;
import javax.ejb.Stateless;
import javax.ejb.Schedule;
import javax.ejb.Timer;
import javax.ejb.TimerService;
import javax.ejb.EJBException;
import javax.interceptor.Interceptors;
import javax.interceptor.ExcludeDefaultInterceptors;
import javax.interceptor.ExcludeClassInterceptors;
import javax.interceptor.InvocationContext;
import javax.naming.InitialContext;
import javax.annotation.Resource;

// Exclude default aroundtimeout, but re-add one of them at class-level
@Stateless
@ExcludeDefaultInterceptors
@Interceptors({InterceptorC.class, InterceptorB.class, InterceptorD.class})
public class SlessEJB4 implements Sless4
{
    @Resource TimerService timerSvc;

    private boolean aroundTimeoutCalled = false;

    private final static int EXPECTED = 5;

    // Called as a timeout and through interface. When called through interface
    // aroundTimeout should be still false, and exception is expected.
    @Schedule(second="*", minute="*", hour="*", info="SlessEJB4-cbd")
    public void cbd() {
        System.out.println("in SlessEJB4:cbd().  aroundTimeoutCalled = " + 
                           aroundTimeoutCalled);

        // a little extra checking to make sure aroundTimeout is invoked...
        if( !aroundTimeoutCalled ) {
            throw new EJBException("bean class aroundTimeout not called - may be correct - check the call stack");
        }
        aroundTimeoutCalled = false;
    }

    @ExcludeClassInterceptors
    @Interceptors({InterceptorE.class, InterceptorF.class})
    @Schedule(second="*", minute="*", hour="*", info="SlessEJB4-ef")
    public void ef() {}

    // explicitly add default aroundtimeout that were disabled at
    // class-level.
    @ExcludeClassInterceptors
    @Interceptors({InterceptorA.class, InterceptorB.class,
                   InterceptorE.class, InterceptorF.class})
    @Schedule(second="*", minute="*", hour="*", info="SlessEJB4-abef")
    public void abef(Timer t) {}

    // @ExcludeDefaultInterceptors is a no-op here since it 
    // was already excluded at class-level
    @ExcludeDefaultInterceptors
    @Interceptors({InterceptorE.class, InterceptorF.class})
    @Schedule(second="*", minute="*", hour="*", info="SlessEJB4-cbdef")
    public void cbdef() {}
    
    @ExcludeClassInterceptors
    @Schedule(second="*", minute="*", hour="*", info="SlessEJB4-nothing")
    public void nothing() {}

    // declared in ejb-jar.xml
    private Object aroundTimeout(InvocationContext ctx)
    {
        System.out.println("In SlessEJB4:aroundTimeout");
        aroundTimeoutCalled = true;
        Common.checkResults(ctx);
        try {
            return ctx.proceed();
        } catch(Exception e) {
            throw new EJBException(e);
        }
    }
    
    public void verify() {
        Common.checkResults("SlessEJB4", EXPECTED);
        Collection<Timer> timers = timerSvc.getTimers();
        for (Timer t : timers)
            t.cancel();
        aroundTimeoutCalled = false;
    }
}
    

