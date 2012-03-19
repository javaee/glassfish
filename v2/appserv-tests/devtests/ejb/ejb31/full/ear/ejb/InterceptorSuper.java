package com.acme;


import javax.interceptor.InvocationContext;
import javax.interceptor.AroundInvoke;


public class InterceptorSuper {

    @AroundInvoke
    protected Object roundInvoke(InvocationContext c) throws Exception {
	throw new RuntimeException("Should not reach here");
    }

}
