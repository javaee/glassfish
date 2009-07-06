package com.acme;

import javax.annotation.*;
import javax.interceptor.InvocationContext;
import javax.interceptor.AroundInvoke;
import javax.ejb.EJBException;

public class NonSerializableInterceptor {

    private int i;

    @PostConstruct
    private void init(InvocationContext c) throws Exception {
	System.out.println("In NonSerializableInterceptor::init() ");
	i = 10;
	c.proceed();
    }

    @AroundInvoke
	public Object doAI(InvocationContext c) throws Exception {

	System.out.println("In doAI , i = " + i);
	if( i != 10 ) {
	    throw new EJBException("Wrong value of interceptor state = " +
				   i);
	}
	return c.proceed();
    }

    @PreDestroy
    private void destroy(InvocationContext c) throws Exception {
	System.out.println("In NonSerializableInterceptor::destroy() ");
	c.proceed();
    }

}