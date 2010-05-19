package com.acme;

import javax.ejb.*;
import javax.annotation.*;

import javax.inject.Inject;
import javax.inject.Named;
import javax.enterprise.event.Observes;

@Stateless
public class StatelessBean2 implements StatelessLocal2 {

    private Foo foo;

    @Inject
	public StatelessBean2(Foo f, Foo sr) {
	foo = f;
	System.out.println("In StatelessBean2()  foo = " + foo + 
			   " sr = " + sr);
    }

    @PostConstruct
	public void init() {
	System.out.println("In StatelessBean2::init()");
    }

    public void hello() {
	System.out.println("In StatelessBean2::hello() " +
			   foo);
    }

    @PreDestroy
	public void destroy() {
	System.out.println("In StatelessBean2::destroy()");
    }

    

}