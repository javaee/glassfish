package com.acme;


import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.annotation.Resource;

import javax.transaction.UserTransaction;

public class FooNonManagedBean {

    @Resource 
    private UserTransaction ut;

    @PostConstruct
    private void init() {
	if( ut == null ) {
	    throw new IllegalStateException("ut is null");
	}

	System.out.println("In FooNonManagedBean:init()");
    }

    public void hello() {
	System.out.println("In FooNonManagedBean::hello()");
    }

    @PreDestroy
    private void destroy() {
	System.out.println("In FooNonManagedBean:destroy()");
    }


}
