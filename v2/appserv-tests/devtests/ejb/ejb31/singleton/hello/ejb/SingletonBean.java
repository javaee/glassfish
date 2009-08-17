package com.acme;

import javax.ejb.*;
import javax.annotation.*;
import org.omg.CORBA.ORB;

@Singleton
@Remote(Hello.class)
public class SingletonBean {

    @Resource
    private ORB orb;

    @PostConstruct
    public void init() {
        System.out.println("In SingletonBean::init()");
        System.out.println("orb = " + orb);
	if( orb == null ) {
	    throw new EJBException("null ORB");
	}
    }
    
    public String hello() {
	System.out.println("In SingletonBean::hello()");
	return "hello, world!\n";
    }

    public void testError() {
	throw new Error("test java.lang.Error");
    }

    @PreDestroy
    public void destroy() {
        System.out.println("In SingletonBean::destroy()");
    }



}
