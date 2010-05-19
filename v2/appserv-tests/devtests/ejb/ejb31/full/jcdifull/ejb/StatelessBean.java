package com.acme;

import javax.ejb.*;
import javax.annotation.*;

import javax.inject.Inject;
import javax.inject.Named;
import javax.enterprise.event.Observes;

@Stateless
@LocalBean
public class StatelessBean implements StatelessLocal {

    @Inject Foo foo;

    @PostConstruct
	public void init() {
	System.out.println("In StatelessBean::init()");
    }

    public void processSomeEvent(@Observes SomeEvent event) {
	System.out.println("In StatelessBean::processSomeEvent " +
			   event);
    }

    public void hello() {
	System.out.println("In StatelessBean::hello() " +
			   foo);
    }

    @PreDestroy
	public void destroy() {
	System.out.println("In StatelessBean::destroy()");
    }

    

}