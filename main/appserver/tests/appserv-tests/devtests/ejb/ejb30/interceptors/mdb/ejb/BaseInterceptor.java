package com.sun.s1asdev.ejb.ejb30.interceptors.mdb;

import javax.interceptor.InvocationContext;
import javax.interceptor.AroundInvoke;

public class BaseInterceptor {

    private static int interceptorCount = 0 ;

    static int getCount() {
	return interceptorCount;
    }

    @AroundInvoke
    public Object intercept(InvocationContext inv)
	throws Exception
    {
	System.out.println("[mdb] Interceptor invoked...");
	interceptorCount++;
	return inv.proceed();
    }
}

