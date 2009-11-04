package com.acme;

import javax.annotation.*;

import javax.interceptor.InvocationContext;
import javax.interceptor.AroundInvoke;
import java.util.Map;

import javax.ejb.EJBException;

public class InterceptorB extends InterceptorSuper {

    @PostConstruct
    private void init(InvocationContext c) throws Exception {
	System.out.println("In InterceptorB::init() ");
	c.proceed();
    }

    @Override
	public Object roundInvoke(InvocationContext c) throws Exception {
	throw new RuntimeException("Should not reach here");
    }

    @AroundInvoke
    private Object aroundInvoke(InvocationContext c) throws Exception {
	System.out.println("In InterceptorB::aroundInvoke() ");
	c.getContextData().put("foo", "bar");
	Object returnValue = c.proceed();
	
	String foobar = (String) c.getContextData().get("foobar");
	System.out.println("foobar from context data = " + foobar);
	if( foobar == null ) {
	    throw new EJBException("invalid context data");
	}
	
	c.getContextData().clear();
	
	return returnValue;
    }

    @PreDestroy
    private void destroy(InvocationContext c) throws Exception {
	System.out.println("In InterceptorB::destroy() ");
	c.proceed();
    }

}