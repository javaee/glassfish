package com.acme;

import javax.annotation.*;

import javax.interceptor.InvocationContext;
import javax.interceptor.AroundInvoke;


public class InterceptorA {

    @PostConstruct
    private void init(InvocationContext c) throws Exception {
	System.err.println("In InterceptorA::init() ");
	c.proceed();
    }


    @AroundInvoke
    private Object roundInvoke(InvocationContext c) throws Exception {
	System.err.println("===> In InterceptorA::aroundInvoke() ");
        if ((c.getTarget() instanceof StatelessBean) && c.getMethod().getName().equals("hello")) {
            StatelessBean sb = (StatelessBean)c.getTarget();
            sb.interceptorCalled(0);
        }
	return c.proceed();
    }

    @PreDestroy
    private void destroy(InvocationContext c) throws Exception {
	System.err.println("In InterceptorA::destroy() ");
	c.proceed();
    }

}
