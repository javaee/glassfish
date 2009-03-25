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
@Startup
public class SingletonBean {
  

    @EJB private  SingletonBean me;
	

    @PostConstruct
    private void init() {

	System.out.println("In SingletonBean:init() me = " + me);
	
	try {
	    InitialContext ic = new InitialContext();

	    // Lookup simple form of portable JNDI name 
	    StatelessBean stateless = (StatelessBean) 
		ic.lookup("java:module/StatelessBean");

	    stateless.hello();

	    // Lookup fully-qualified form of portable JNDI name
	    StatelessBean stateless2 = (StatelessBean) 
		ic.lookup("java:module/StatelessBean!com.acme.StatelessBean");

	    stateless2.hello();

	} catch(NamingException ne) {
	    throw new EJBException(ne);
	}
    }

    public void hello() {
	System.out.println("In SingletonBean:hello()");
    }

    @PreDestroy
    private void destroy() {
	System.out.println("In SingletonBean:destroy()");
    }


}
