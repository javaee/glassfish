package com.acme;

import javax.annotation.*;

import javax.ejb.EJB;
import javax.annotation.Resource;
import javax.transaction.UserTransaction;
import org.omg.CORBA.ORB;
import javax.persistence.PersistenceContext;
import javax.persistence.EntityManager;

import javax.interceptor.InvocationContext;
import javax.interceptor.AroundInvoke;


public class InterceptorA {

    @EJB StatelessBean s;
    @Resource UserTransaction ut;

    @PostConstruct
    private void init(InvocationContext c) throws Exception {
	System.out.println("In InterceptorA::init() ");
	c.proceed();
    }


    @AroundInvoke
    private Object roundInvoke(InvocationContext c) throws Exception {
	System.out.println("In InterceptorA::aroundInvoke() ");
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