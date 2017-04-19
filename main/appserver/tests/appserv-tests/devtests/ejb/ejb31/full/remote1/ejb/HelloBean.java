package com.acme;

import javax.ejb.*;
import javax.annotation.*;
import java.util.concurrent.Future;

@Stateless(mappedName="HH")
@RemoteHome(HelloHome.class)
@Remote(Hello.class)
@TransactionManagement(TransactionManagementType.BEAN)
@EJB(name="java:global/HBGlobal", beanName="HelloBean", beanInterface=Hello.class)
public class HelloBean {

    @Resource(name="java:module/env/sesCtx")
    private SessionContext sessionCtx;

    @EJB(name="java:module/env/M1")
    private Hello m1;

    @EJB(name="java:module/M2")
    private HelloHome m2;

    @EJB(name="java:app/env/A1")
    private Hello a1;

    @EJB(name="java:app/A2")
    private HelloHome a2;

    @EJB(name="java:comp/env/C1")
    private Hello c1;

    @EJB(name="java:comp/C2")
    private HelloHome c2;


    @PostConstruct
    public void init() {
	System.out.println("In HelloBean::init()");
    }

    public String hello() {
	System.out.println("In HelloBean::hello()");
	return "hello, world\n";
    }

    @Asynchronous
    public Future<String> helloAsync() {
	System.out.println("In HelloBean::helloAsync()");
	return new AsyncResult<String>("helo, async world!\n");
    }

    @Asynchronous
    public Future<String> asyncBlock(int seconds) {
	System.out.println("In HelloBean::asyncBlock");
	sleep(seconds);
	return new AsyncResult<String>("blocked successfully");
    }

    @Asynchronous 
    public void fireAndForget() {
	System.out.println("In HelloBean::fireAndForget()");
	sleep(5);
    }
	
    @Asynchronous
    public Future<String> asyncThrowException(String exceptionType) {
	System.out.println("In HelloBean::asyncThrowException");
	throwException(exceptionType);
	return new AsyncResult<String>("should have thrown exception");
    }

    @Asynchronous
    public Future<String> asyncCancel(int seconds) throws Exception
    {
	System.out.println("In HelloBean::asyncCancel");
	sleep(seconds);
	if( sessionCtx.wasCancelCalled() ) {
	    throw new Exception("Canceled after " + seconds + " seconds");
	}
	return new AsyncResult<String>("asyncCancel() should have been cancelled");
    }

    public void throwException(String exceptionType) {
	if( exceptionType.equals("javax.ejb.EJBException") ) {
	    throw new EJBException(exceptionType);
	} else if( exceptionType.equals("javax.ejb.ConcurrentAccessException") ) {
	    throw new ConcurrentAccessException(exceptionType);
	} else if( exceptionType.equals("javax.ejb.ConcurrentAccessTimeoutException") ) {
	    throw new ConcurrentAccessTimeoutException(exceptionType);
	} else if( exceptionType.equals("javax.ejb.IllegalLoopbackException") ) {
	    throw new IllegalLoopbackException(exceptionType);
	}

	throw new IllegalArgumentException(exceptionType);
    }

    private void sleep(int seconds) {

	System.out.println("In HelloBean::sleeping for " + seconds + 
			   "seconds");
	try {
	    Thread.currentThread().sleep(seconds * 1000);
	    System.out.println("In HelloBean::woke up from " + seconds + 
			       "second sleep");
	} catch(Exception e) {
	    e.printStackTrace();
	}

    }


    @PreDestroy
    public void destroy() {
	System.out.println("In HelloBean::destroy()");
    }


}