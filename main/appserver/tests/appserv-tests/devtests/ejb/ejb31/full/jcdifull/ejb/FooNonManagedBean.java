package com.acme;


import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.annotation.Resource;
import org.omg.CORBA.ORB;

import javax.transaction.UserTransaction;

public class FooNonManagedBean {

    @Resource(name="java:module/env/fnmb_orb") ORB orb;

    @PostConstruct
    private void init() {
	System.out.println("In FooNonManagedBean:init()");
	System.out.println("orb = " + orb);
	if( orb == null ) {
	    throw new RuntimeException("null orb");
	}
    }

    public void hello() {
	System.out.println("In FooNonManagedBean::hello()");
    }

    @PreDestroy
    private void destroy() {
	System.out.println("In FooNonManagedBean:destroy()");
    }


}
