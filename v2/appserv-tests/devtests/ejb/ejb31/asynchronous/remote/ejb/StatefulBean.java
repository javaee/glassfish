package com.acme;

import javax.ejb.*;
import javax.annotation.*;

import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

@Stateful
public class StatefulBean implements RemoteAsync2, RemoteAsync3 {

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

    @Asynchronous
	public Future<String> throwException(String exception) throws CreateException {
	if( exception.equals("javax.ejb.CreateException") ) {
	    throw new CreateException();
	} else if ( exception.equals("javax.ejb.EJBException") ) {
	    throw new EJBException();
	}

	return new AsyncResult<String>("unsupported exception type");
    }

    @PreDestroy
    public void destroy() {
        System.out.println("In StatefulBean::destroy()");
    }



}
