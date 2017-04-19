package com.acme;

import javax.naming.InitialContext;
import javax.naming.NamingException;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.annotation.Resource;

import javax.ejb.Singleton;
import javax.ejb.Startup;
import javax.ejb.EJBException;
import javax.ejb.EJB;

@Singleton
public class SingletonBean {

    public boolean interceptorWasHere;

    private StatelessBean stateless;
    private StatelessBean stateless2;

    @PostConstruct
    private void init() {

	System.out.println("In SingletonBean:init() ");
	
	try {
	    InitialContext ic = new InitialContext();

	    // Lookup simple form of portable JNDI name 
	     stateless = (StatelessBean) 
		ic.lookup("java:module/StatelessBean");

	     stateless.hello();

	    // Lookup fully-qualified for of portable JNDI name
	    stateless2 = (StatelessBean) 
		ic.lookup("java:module/StatelessBean!com.acme.StatelessBean");
	  
	} catch(NamingException ne) {
	    throw new EJBException(ne);
	}
    }

    public void hello() {
	System.out.println("In SingletonBean:hello()");
	stateless.hello();
	stateless2.hello();
    }

    public void assertInterceptorBinding() {
	if( !interceptorWasHere ) {
	    throw new EJBException("interceptor was not here");
	}
    }

    @PreDestroy
    private void destroy() {
	System.out.println("In SingletonBean:destroy()");
    }


}
