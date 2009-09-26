package com.acme;

import javax.ejb.*;
import javax.annotation.*;

@Singleton
@Startup
public class SingletonBean {

    @EJB SingletonBean me;

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
