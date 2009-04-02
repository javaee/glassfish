package com.acme;

import javax.ejb.*;
import javax.annotation.*;

import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

@Stateful
public class StatefulBean implements RemoteAsync {

    @Resource
    private SessionContext sessionCtx;

    @PostConstruct
    public void init() {
        System.out.println("In StatefulBean::init()");
    }

    @Asynchronous
    public Future<String> helloAsync() {
	return new AsyncResult<String>("hello, world\n");
    }

    @Asynchronous
    @Remove
    public Future<String> removeAfterCalling() {
	System.out.println("In StatefulBean::removeAfterCalling()");
	return new AsyncResult<String>("removed");
    }

    @PreDestroy
    public void destroy() {
        System.out.println("In StatefulBean::destroy()");
    }



}
