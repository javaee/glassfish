package com.acme;

import javax.ejb.*;
import javax.annotation.*;

import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

@Singleton
public class SingletonBean implements RemoteAsync {

    @Resource
    private SessionContext sessionCtx;

    //    private AtomicInteger fireAndForgetCount = new AtomicInteger();

    //    private AtomicInteger processAsyncCount = new AtomicInteger();

    @PostConstruct
    public void init() {
        System.out.println("In SingletonBean::init()");

    }

    public void startTest() {
	System.out.println("in SingletonBean::startTest()");
	// reset state
	return;
    }

    @PreDestroy
    public void destroy() {
        System.out.println("In SingletonBean::destroy()");
    }



}
