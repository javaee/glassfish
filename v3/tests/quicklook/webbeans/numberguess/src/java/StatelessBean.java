package org.jboss.webbeans.examples.numberguess;

import javax.ejb.*;
import javax.annotation.*;

import javax.inject.Inject;

@Stateless
public class StatelessBean implements StatelessLocal {

    @Inject 
	private SingletonBean singleton;
    

    @PostConstruct
	public void init() {
	System.out.println("In StatelessBean::init()");
	System.out.println("singleton = " + singleton);
    }

    public void hello() {
	System.out.println("In StatelessBean::hello()");
    }

    @PreDestroy
	public void destroy() {
	System.out.println("In StatelessBean::destroy()");
    }

    

}