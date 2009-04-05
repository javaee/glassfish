package com.acme;

import javax.ejb.*;
import javax.annotation.*;

@Stateful(mappedName="StatefulBeanNotSerialized")
@LocalBean
@ConcurrencyManagement(ConcurrencyManagementType.CONCURRENCY_NOT_ALLOWED)
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
