package com.acme;

import javax.annotation.*;

import javax.interceptor.InvocationContext;
import javax.interceptor.AroundInvoke;


public class InterceptorSuper {

    @PostConstruct
    private void init(InvocationContext c) throws Exception {
	System.out.println("In InterceptorSuper::init() ");
	c.proceed();
    }

    @AroundInvoke
    private Object roundInvoke(InvocationContext c) throws Exception {
	System.out.println("In InterceptorSuper::aroundInvoke() ");
	return c.proceed();
    }

    @PreDestroy
    private void destroy(InvocationContext c) throws Exception {
	System.out.println("In InterceptorSuper::destroy() ");
	c.proceed();
    }

}