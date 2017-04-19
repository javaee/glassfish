package com.acme;

import javax.ejb.*;
import javax.annotation.*;
import javax.interceptor.*;

import javax.inject.Inject;

@Singleton
@Startup
public class SingletonBeanA {

    Bar bar;

    @Inject 
    public SingletonBeanA(Bar bar) {
        this.bar = bar;
    }

    @PostConstruct
    public void init() {
        System.out.println("In SingletonBeanA::init()");
	if( bar == null ) {
	    throw new EJBException("bar is null");
	}
	System.out.println("bar inject = " + bar);
    }

    @PreDestroy
    public void destroy() {
        System.out.println("In SingletonBeanA::destroy()");
    }

    public Bar getBar() {
        return bar;
    }

}
