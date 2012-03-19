package com.acme;

import javax.annotation.*;

import javax.ejb.EJB;
import org.omg.CORBA.ORB;
import javax.annotation.Resource;
import javax.persistence.PersistenceContext;
import javax.persistence.EntityManager;

import javax.interceptor.InvocationContext;
import javax.interceptor.AroundInvoke;


public class InterceptorA extends InterceptorSuper {

    @EJB Hello s;
    @Resource ORB orb;

    @PostConstruct
    private void init(InvocationContext c) throws Exception {
	System.out.println("In InterceptorA::init() ");
	c.proceed();
    }


    @AroundInvoke
    public Object roundInvoke(InvocationContext c) throws Exception {
	System.out.println("In InterceptorA::roundInvoke() ");
	return c.proceed();
    }

    @PreDestroy
    private void destroy(InvocationContext c) throws Exception {
	System.out.println("In Interceptor::destroy() ");
	c.proceed();
    }

}
