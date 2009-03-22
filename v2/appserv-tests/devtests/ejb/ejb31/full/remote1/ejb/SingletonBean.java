package com.acme;

import javax.ejb.*;
import javax.annotation.*;

@Singleton
@Startup
public class SingletonBean {

    @EJB Hello hello;
    @EJB HelloHome helloHome;

    @PostConstruct
    public void init() {
        System.out.println("In SingletonBean::init()");

	try {
	    HelloRemote hr = helloHome.create();
	    System.out.println("HellohelloRemote.hello() says " + hr.hello());

	    System.out.println("Hello.hello() says " + hello.hello());
	} catch(Exception e) {
	    e.printStackTrace();
	}

    }

    @PreDestroy
    public void destroy() {
        System.out.println("In SingletonBean::destroy()");
    }



}
