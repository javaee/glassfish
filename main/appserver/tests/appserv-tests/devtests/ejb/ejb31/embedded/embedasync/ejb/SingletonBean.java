package com.acme;

import javax.ejb.*;
import javax.annotation.*;

import javax.naming.*;

import javax.enterprise.inject.spi.BeanManager;

@Singleton
@Startup
@LocalBean
    public class SingletonBean /* implements HelloRemote */ {

    @EJB SingletonBean me;

    @EJB StatefulBean sf;

    @Resource SessionContext sesCtx;

    private boolean gotAsyncCall = false;

    @PostConstruct
    public void init() {
        System.out.println("In SingletonBean::init()");
	System.out.println("Thread = " + Thread.currentThread());
	me.fooAsync();

	try {
		BeanManager beanMgr = (BeanManager)
		    new InitialContext().lookup("java:comp/BeanManager");
	System.out.println("Successfully retrieved bean manager " +
			   beanMgr + " for JCDI enabled app");
	} catch(Exception e) {
	    e.printStackTrace();
	    throw new EJBException(e);
	}
	

    }
    
    public String hello() {
	System.out.println("In SingletonBean::hello()");
	return "hello, world!\n";
    }

    @Asynchronous
    public void fooAsync() {
	System.out.println("In SingletonBean::fooAsync()");
	System.out.println("Thread = " + Thread.currentThread());
	gotAsyncCall = true;
    }

    public boolean getPassed() {
	return gotAsyncCall;
    }

    @PreDestroy
    public void destroy() {
        System.out.println("In SingletonBean::destroy()");
    }



}
