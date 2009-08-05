package com.acme;

import javax.ejb.*;
import javax.annotation.*;

@Stateful(mappedName="StatefulBeanNotSerialized")
@LocalBean
@AccessTimeout(0)
public class StatefulBeanNotSerialized extends StatefulBeanSuper implements StatefulCncRemote, StatefulCncLocal {

 @Resource
    private SessionContext sessionCtx;

    @PostConstruct
    public void init() {
        System.out.println("In StatefulBeanNotSerialized::init()");
    }

    @PreDestroy
    public void destroy() {
        System.out.println("In StatefulBeanNotSerialized::destroy()");
    }

    @Asynchronous
    public void sleep(int seconds) {
	System.out.println("In StatefulBeanNotSerialized::asyncSleep");
	try {
	    System.out.println("Sleeping for " + seconds + " seconds...");
	    Thread.sleep(seconds * 1000);
	    System.out.println("Woke up from sleep");
	} catch(Exception e) {
	    e.printStackTrace();
	    throw new EJBException(e);
	}
    }

     public String hello() {
	System.out.println("In StatefulBeanNotSerialized::hello");
	return "hello, world!\n";
    }

    public void attemptLoopback() {
	System.out.println("In StatefulBeanNotSerialized::attemptLoopback");
	StatefulCncSuperIntf me = sessionCtx.getBusinessObject(StatefulCncLocal.class);
	try {
	    me.hello();
	    throw new EJBException("Should have received concurrent access ex");
	} catch(ConcurrentAccessException cae) {
	    System.out.println("Successfully received concurent access exception");
	}
    }



}
