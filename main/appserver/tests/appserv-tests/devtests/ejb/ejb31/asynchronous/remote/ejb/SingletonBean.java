package com.acme;

import javax.ejb.*;
import javax.annotation.*;

import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

@Singleton
@Lock(LockType.READ)
public class SingletonBean implements RemoteAsync {

    @Resource
    private SessionContext sessionCtx;

    private AtomicInteger fireAndForgetCount = new AtomicInteger();

    private AtomicInteger processAsyncCount = new AtomicInteger();

    @PostConstruct
    public void init() {
        System.out.println("In SingletonBean::init()");

    }

    public void startTest() {
	System.out.println("in SingletonBean::startTest()");
	// reset state
	fireAndForgetCount = new AtomicInteger();
	return;
    }

    @Asynchronous
    public void fireAndForget() {
	System.out.println("In SingletonBean::fireAndForget()");
	fireAndForgetCount.incrementAndGet();
    }

    public int getFireAndForgetCount() {
	return fireAndForgetCount.get();
    }

    @Asynchronous
    public Future<String> helloAsync() {
	return new AsyncResult<String>("hello, world\n");
    }

    @Asynchronous
    public Future<Integer> processAsync(int sleepInterval, int numIntervals) 
       throws Exception {
	int me = processAsyncCount.incrementAndGet();
	boolean cancelled = false;
	int i = 0;
	while(i < numIntervals) {
	    i++;
	    if( sessionCtx.wasCancelCalled() ) {
		System.out.println("Cancelling processAsync " + me + " after " +
				   i + " intervals");
		cancelled = true;
		break;
	    }
	    try {
		System.out.println("Sleeping for " + i + "th time in processAsync " +
				   me);
		Thread.sleep(sleepInterval * 1000);
		System.out.println("Woke up for " + i + "th time in processAsync " +
				   me);
	    } catch(Exception e) {
		e.printStackTrace();
		throw new EJBException(e);
	    }
	}
	
	if( cancelled ) {
	    throw new Exception("Cancelled processAsync " + me);
	}

	return new AsyncResult<Integer>(numIntervals);
    }

    @PreDestroy
    public void destroy() {
        System.out.println("In SingletonBean::destroy()");
    }



}
