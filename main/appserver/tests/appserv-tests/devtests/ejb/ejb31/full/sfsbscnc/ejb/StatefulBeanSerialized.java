package com.acme;

import javax.ejb.*;
import javax.annotation.*;

@Stateful(mappedName="StatefulBeanSerialized")
@LocalBean
public class StatefulBeanSerialized extends StatefulBeanSuper implements StatefulCncRemote, StatefulCncLocal {

   @Resource
    private SessionContext sessionCtx;

    @PostConstruct
    public void init() {
        System.out.println("In StatefulBeanSerialized::init()");
    }

    public void attemptLoopback() {
	System.out.println("In StatefulBeanSerialized::attemptLoopback");
	StatefulCncSuperIntf me = sessionCtx.getBusinessObject(StatefulCncLocal.class);
	try {
	    me.hello();
	    throw new EJBException("Should have received loopback ex");
	} catch(IllegalLoopbackException ile) {
	    System.out.println("Successfully received loopback exception");
	}
    }

    @PreDestroy
    public void destroy() {
        System.out.println("In StatefulBeanSerialized::destroy()");
    }



}
