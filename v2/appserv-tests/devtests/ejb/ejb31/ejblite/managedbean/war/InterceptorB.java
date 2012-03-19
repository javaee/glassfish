package com.acme;

import javax.annotation.*;

import javax.ejb.EJB;
import javax.annotation.Resource;
import javax.transaction.UserTransaction;
import javax.persistence.PersistenceContext;
import javax.persistence.EntityManager;

import javax.interceptor.InvocationContext;
import javax.interceptor.AroundInvoke;


public class InterceptorB {


    @PostConstruct
    private void init(InvocationContext c) throws Exception {
	System.out.println("In InterceptorB::init() ");
	((ManagedBeanSuper)c.getTarget()).newInterceptorInstance();
	c.proceed();
    }


    @AroundInvoke
    private Object roundInvoke(InvocationContext c) throws Exception {
	System.out.println("In InterceptorB::aroundInvoke() ");
	if( c.getMethod().getName().equals("getAroundInvokeSequence") ) {
	    String result = (String) c.proceed();
	    return "B" + result;
	} else {
	    return c.proceed();
	}
    }

    @PreDestroy
    private void destroy(InvocationContext c) throws Exception {
	System.out.println("In InterceptorB::destroy() ");
	c.proceed();
    }

}
