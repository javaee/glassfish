package com.acme;

import javax.ejb.*;
import javax.annotation.*;

import org.omg.CORBA.ORB;


@Singleton
    @EJB(name="java:app/env/AS2", beanName="HelloStateless", beanInterface=HelloRemote.class)
public class HelloSingleton implements Hello {

    @Resource SessionContext sessionCtx;

    @Resource(name="java:module/env/MORB2")
    private ORB orb;

    String appName;
    String moduleName;

    @PostConstruct    
    private void init() {
	System.out.println("HelloSingleton::init()");
	appName = (String) sessionCtx.lookup("java:comp/AppName");
	moduleName = (String) sessionCtx.lookup("java:comp/ModuleName");

	ORB orb1 = (ORB) sessionCtx.lookup("java:module/MORB1");
	ORB orb2 = (ORB) sessionCtx.lookup("java:module/env/MORB2");

	System.out.println("AppName = " + appName);
	System.out.println("ModuleName = " + moduleName);
    }

    public String hello() {
	
	System.out.println("HelloSingleton::hello()");
	return "hello, world!\n";
    }


    @PreDestroy
    private void destroy() {
	System.out.println("HelloSingleton::destroy()");
    }

}


