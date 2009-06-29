package com.acme;

import javax.ejb.Stateless;
import javax.ejb.*;
import javax.interceptor.Interceptors;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.annotation.Resource;

@Stateless
public class StatelessBean {
    
    @Resource 
    private SessionContext sessionCtx;

    @Resource(mappedName="java:module/foomanagedbean")
    private FooManagedBean foo;

    @Resource(mappedName="java:app/foomanagedbean")
    private FooManagedBean foo2;

    @PostConstruct
    private void init() {
	System.out.println("In StatelessBean:init()");
    }

    public void hello() {
	System.out.println("In StatelessBean::hello()");

	FooManagedBean fmb = (FooManagedBean) 
	    sessionCtx.lookup("java:module/foomanagedbean");

	// Ensure that each injected or looked up managed bean 
	// instance is unique
	Object fooThis = foo.getThis();
	Object foo2This = foo2.getThis();
	Object fmbThis = fmb.getThis();

	System.out.println("fooThis = " + fooThis);
	System.out.println("foo2This = " + foo2This);
	System.out.println("fmbThis = " + fmbThis);

	if( ( fooThis == foo2This ) || ( fooThis == fmbThis  ) ||
	    ( foo2This == fmbThis ) ) {
	    throw new EJBException("Managed bean instances not unique");
	}

    }

    @PreDestroy
    private void destroy() {
	System.out.println("In StatelessBean:destroy()");
    }


}
