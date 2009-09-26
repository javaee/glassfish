package com.acme;

import javax.ejb.*;
import javax.annotation.*;

import javax.inject.Inject;

import javax.naming.InitialContext;

import javax.enterprise.inject.spi.BeanManager;

@Singleton
@Startup
public class SingletonBean implements SingletonRemote {

    @EJB
	private StatelessLocal statelessEE;
    
    @Resource SessionContext sesCtx;

    @PostConstruct
    public void init() {
        System.out.println("In SingletonBean::init()");
    }

    public void hello() {
	System.out.println("In SingletonBean::hello()");
	statelessEE.hello();
	
	BeanManager beanMgr = (BeanManager)
	    sesCtx.lookup("java:comp/BeanManager");
	
	System.out.println("Successfully retrieved bean manager " +
			   beanMgr + " for JCDI enabled app");
			   
	
    }

    @PreDestroy
    public void destroy() {
        System.out.println("In SingletonBean::destroy()");
    }



}
