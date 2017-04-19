package com.acme;

import javax.ejb.*;
import javax.annotation.*;

import javax.naming.*;

import javax.enterprise.inject.spi.BeanManager;

@Singleton
@Startup
@LocalBean
public class SingletonBean {

    @EJB SingletonBean me;

    @Resource SessionContext sesCtx;

    private boolean gotAsyncCall = false;

    @PostConstruct
    public void init() {
        System.out.println("In SingletonBean::init()");
	System.out.println("Thread = " + Thread.currentThread());
	me.fooAsync();
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
