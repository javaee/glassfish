package com.acme;

import javax.annotation.*;

import javax.ejb.EJB;
import javax.annotation.Resource;
import org.omg.CORBA.ORB;
import javax.persistence.PersistenceContext;
import javax.persistence.EntityManager;

import javax.interceptor.InvocationContext;
import javax.interceptor.AroundInvoke;


public class InterceptorA extends InterceptorSuper {

    @EJB StatelessBean s;
    @Resource ORB orb;

    @PostConstruct
    private void init(InvocationContext c) throws Exception {
	System.out.println("In InterceptorA::init() ");
	c.proceed();
    }


    @AroundInvoke
    public Object roundInvoke(InvocationContext c) throws Exception {
	System.out.println("In InterceptorA::roundInvoke() ");
	if( c.getParameters().length > 0 ) {
	    System.out.println("param 1 = " + c.getParameters()[0]);
	}
	return c.proceed();
    }

    @PreDestroy
    private void destroy(InvocationContext c) throws Exception {
	System.out.println("In Interceptor::destroy() ");
	c.proceed();
    }

}
