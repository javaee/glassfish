package com.acme;

import javax.ejb.*;
import javax.annotation.*;
import java.util.concurrent.Future;

@Singleton
@LocalBean
@Local( { Local1.class, Local2.class } )
@Remote( { Remote1.class, Remote2.class } )
public class SingletonMultiIntf {

    @Resource
    private SessionContext sessionCtx;

    @PostConstruct
    public void init() {
	System.out.println("In SingletonMultiIntf::init()");
    }

    public String hello() {
	System.out.println("In SingletonMultiIntf::hello() invoked by " +
			   sessionCtx.getInvokedBusinessInterface());
	return "hello, world\n";
    }

    @PreDestroy
    public void destroy() {
	System.out.println("In SingletonMultiIntf::destroy()");
    }


}