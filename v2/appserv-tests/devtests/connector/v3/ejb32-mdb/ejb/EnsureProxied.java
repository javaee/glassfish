package com.sun.s1asdev.ejb.ejb32.mdb.ejb;

import javax.interceptor.AroundInvoke;
import javax.interceptor.InvocationContext;

/**
 * @author David Blevins
 */
public class EnsureProxied {

    @AroundInvoke
    public Object invoke(InvocationContext context) throws Exception {
        context.getContextData().put("data", " - intercepted");
        return context.proceed();
    }
}
